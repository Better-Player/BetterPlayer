package net.betterplayer.betterplayer.commands.defaultcommands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.apis.Spotify;
import net.betterplayer.betterplayer.apis.exceptions.SpotifyApiException;
import net.betterplayer.betterplayer.apis.gson.SpotifyTrackResponse;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.search.VideoDetails;
import net.betterplayer.betterplayer.search.YoutubeSearch;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.w3c.dom.Text;

/**
 * Play command. This will allow the user to play a song via a search query, a YouTube video URL or a YouTube playlist URL
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "play", description = "Play a YouTube video, playlist, or search for a video", aliases = {"p"})
public class PlayCommandExecutor implements CommandExecutor {

	private final ConfigManifest config;
	
	public PlayCommandExecutor(ConfigManifest config) {
		this.config = config;
	}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		//Check if the user is in a voice channel, if BetterPlayer is not in the channel then it should connect.
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, true)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

		//Check if the user provided arguments, they need to.
		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide a search query for me to play!").queue();
			return;
		}
		
		JoinCommandExecutor jce = new JoinCommandExecutor(true);
		jce.fireCommand(betterPlayer, parameters);
		
		//Check if the user supplied a url rather than search terms
		String arg0 = parameters.getArgs()[0];
		if(arg0.contains("https://")) {

			// YouTube video or playlist
			if(arg0.contains("youtube.com")) {
				playYoutube(parameters, betterPlayer);
			}

			// Spotify track or playlist
			if(arg0.contains("open.spotify.com")) {

				// Track
				if(arg0.contains("track")) {
					playSpotifyTrack(parameters, betterPlayer);
				}

				//TODO playlist
			}
		} else {
			// Via regular search terms
			youtubeSearch(betterPlayer, parameters.getArgs(), parameters, senderChannel);
		}
	}

	private void playSpotifyTrack(CommandParameters parameters, BetterPlayer betterPlayer) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

		String arg0 = parameters.getArgs()[0];
		String[] split = arg0.split(Pattern.quote("/"));
		if(split.length == 0) {
			senderChannel.sendMessage("Hm, that URL appears to be invalid").queue();
			return;
		}

		String trackIdWithParams = split[split.length - 1];
		String[] trackIdSplit = trackIdWithParams.split(Pattern.quote("?"));
		String trackId = trackIdSplit[0];

		ConfigManifest cfgManifest = betterPlayer.getConfig();
		if(cfgManifest.getSpotifyClientId() == null || cfgManifest.getSpotifyClientSecret() == null) {
			BetterPlayer.logError("Unable to process Spotify link. The required credentials are not provided");
			senderChannel.sendMessage("Spotify links are not supported by this instance of BetterPlayer, please contact it's administrator").queue();
			return;
		}

		Optional<SpotifyTrackResponse> oTrack;
		try {
			oTrack = Spotify.getTrack(trackId);
		} catch (IOException | SpotifyApiException e) {
			BetterPlayer.logError(e.getMessage());
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			senderChannel.sendMessage("Something went wrong. Please try again later").queue();
			return;
		}

		if(oTrack.isEmpty()) {
			senderChannel.sendMessage("Hm, I can't find that song, are you sure you copied the correct URL?").queue();
			return;
		}

		SpotifyTrackResponse track = oTrack.get();
		youtubeSearch(betterPlayer, new String[] { track.getArtistName(), track.getName() }, parameters, senderChannel);
	}

	private void youtubeSearch(BetterPlayer betterPlayer, String[] searchTerms, CommandParameters parameters, TextChannel senderChannel) {
		if(this.config.isUseGoogleApiForSearch()) {
			VideoDetails details = new YoutubeSearch().searchViaApi(this.config.getGoogleApiKey(), searchTerms, senderChannel);
			processVideoDetails(betterPlayer, parameters, details, true);
		} else {
			VideoDetails details = new YoutubeSearch().searchViaFrontend(this.config.getGoogleApiKey(), searchTerms, senderChannel);
			processVideoDetails(betterPlayer, parameters, details, true);
		}
	}

	private void playYoutube(CommandParameters parameters, BetterPlayer betterPlayer) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		String arg0 = parameters.getArgs()[0];

		Map<String, String> urlQueryParameters = null;
		try {
			urlQueryParameters = Utils.splitQuery(new URL(arg0));
		} catch (UnsupportedEncodingException | MalformedURLException e) {
			senderChannel.sendMessage("That URL is invalid!").queue();
		}

		if(urlQueryParameters.containsKey("v")) {
			String videoId = urlQueryParameters.get("v");

			VideoDetails vd = new YoutubeSearch().getVideoDetails(this.config.getGoogleApiKey(), videoId, senderChannel);

			if(vd != null) {
				processVideoDetails(betterPlayer, parameters, vd, true);
			} else {
				senderChannel.sendMessage("Unknown video!").queue();
			}

			return;

		} else if(urlQueryParameters.containsKey("list") && !urlQueryParameters.containsKey("v")) {
			String listId = urlQueryParameters.get("list");
			playYoutubePlaylist(parameters, betterPlayer, listId);
			return;
		}
	}

	private void playYoutubePlaylist(CommandParameters parameters, BetterPlayer betterPlayer, String listId) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

		// Your Likes playlist
		if(listId.equals("LL")) {
			senderChannel.sendMessage("Unfortunately BetterPlayer cannot play your 'Your Likes' playlist, as it is automatically generated and only visible to you.").queue();
			return;
		}

		// Watch Later playlist
		if(listId.equals("WL")) {
			senderChannel.sendMessage("Unfortunately BetterPlayer cannot play your 'Watch Later' playlist, as it is automatically generated and only visible to you.").queue();
			return;
		}

		senderChannel.sendMessage("Loading playlist... (this might take a couple of seconds)").queue();

		List<VideoDetails> vds = new YoutubeSearch().searchPlaylistViaApi(this.config.getGoogleApiKey(), listId, senderChannel, null);
		if(vds != null && vds.size() >= 1) {
			for(VideoDetails details : vds) {
				processVideoDetails(betterPlayer, parameters, details, false);
			}

			User author = jda.getUserById(parameters.getSenderId());
			QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
			if(!qm.hasQueue(parameters.getGuildId())) {
				qm.createQueue(parameters.getGuildId());
			}

			Optional<Integer> oPosInQueue = qm.getQueueSize(parameters.getGuildId());
			if(oPosInQueue.isEmpty()) {
				BetterPlayer.logError("No queue exists for the current Guild. This should not happen");
				senderChannel.sendMessage("Something went wrong. Please try again later").queue();
				return;
			}
			int posInQueue = oPosInQueue.get();

			VideoDetails videoDetails = vds.get(0);
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Added " + vds.size() + " tracks to the queue!")
					.setThumbnail(videoDetails.getThumbnailUrl())
					.setColor(BetterPlayer.GRAY)
					.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
					.addField("Position in queue", String.valueOf(posInQueue +1), true)
					.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

			senderChannel.sendMessageEmbeds(eb.build()).queue();
		} else {
			senderChannel.sendMessage("Unknown playlist or the playlist contains no videos!").queue();
		}
	}
		
	/**
	 * Process video details and play the video
	 * @param betterPlayer BetterPlayer instance
	 * @param parameters CommandParameters object
	 * @param videoDetails VideoDetails object
	 * @param announce Should messages be send to the sender's TextChannel
	 */
	private void processVideoDetails(BetterPlayer betterPlayer, CommandParameters parameters, VideoDetails videoDetails, boolean announce) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		User author = jda.getUserById(parameters.getSenderId());
		long guildId = parameters.getGuildId();
		
		QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
		if(!qm.hasQueue(parameters.getGuildId())) {
			qm.createQueue(parameters.getGuildId());
		}
		
		//Check if VideoDetails and it's parameters are not null
		//If any of them are that means no results were found and we can stop
		if(videoDetails == null || videoDetails.getId() == null || videoDetails.getTitle() == null || videoDetails.getChannel() == null) {
			senderChannel.sendMessage("No results found! Try another search term").queue();
			return;
		}
		
		//Create a QueueItem for the video
		QueueItem qi = new QueueItem(videoDetails.getTitle(), videoDetails.getId(), videoDetails.getChannel());
		
		if(!betterPlayer.getBetterAudioManager().isPlaying(guildId)) {
			qm.setNowPlaying(guildId, qi);
		} else {
			qm.addToQueue(guildId, qi);
		}
		
		//Check if we're already playing something for this guild
		//If not, we want to play something		
		if(!betterPlayer.getBetterAudioManager().isPlaying(guildId)) {			
						
			//Check if the guild has an AudioPlayer, if not, create it
			if(!betterPlayer.getBetterAudioManager().hasAudioPlayer(guildId)) {				
				betterPlayer.getBetterAudioManager().init(guildId);
			}
			
			//Load the track
			betterPlayer.getBetterAudioManager().loadTrack(videoDetails.getId(), guildId);
		}
		
		//Check if the guild currently has a pause state of true--meaning it's paused
		//if so, unpause
		if(betterPlayer.getBetterAudioManager().getPauseState(guildId)) {
			betterPlayer.getBetterAudioManager().setPauseState(guildId, false);
		}
		
		//Get the size of the queue, we display this as position in queue to the user.
		//we don't have to do +1 here, because the length is not 0-based.
		Optional<Integer> oPosInQueue = qm.getQueueSize(parameters.getGuildId());
		if(oPosInQueue.isEmpty()) {
			BetterPlayer.logError("No queue exists for the current Guild. This should not happen");
			senderChannel.sendMessage("Something went wrong. Please try again later").queue();
			return;
		}
		int posInQueue = oPosInQueue.get();
		
		//If announce is true, then we want to send a message to the senderChannel
		if(announce) {
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle(videoDetails.getTitle())
					.setThumbnail(videoDetails.getThumbnailUrl())
					.setColor(BetterPlayer.GRAY)
					.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
					.addField("Channel", videoDetails.getChannel(), true)
					.addField("Duration", videoDetails.getDuration(), true)
					.addField("Position in queue", String.valueOf(posInQueue +1), true)
					.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessageEmbeds(eb.build()).queue();
		}
	}
}
