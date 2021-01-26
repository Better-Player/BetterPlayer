package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.search.VideoDetails;
import net.betterplayer.betterplayer.search.YoutubeSearch;

public class PlayCommandExecutor implements CommandExecutor {

	private boolean useApi;
	private String apiKey;
	
	public PlayCommandExecutor(boolean useApi, String apiKey) {
		this.useApi = useApi;
		this.apiKey = apiKey;
	}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, true)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide a search query for me to play!").queue();
			return;
		}
		
		JoinCommandExecutor jce = new JoinCommandExecutor(true);
		jce.fireCommand(betterPlayer, parameters);
		
		//Check if the user supplied a url rather than search terms
		String arg0 = parameters.getArgs()[0];
		if(arg0.contains("https://")) {
			Map<String, String> urlQueryParameters = null;
			try {
				urlQueryParameters = Utils.splitQuery(new URL(arg0));
			} catch (UnsupportedEncodingException e) {
				senderChannel.sendMessage("That URL is invalid!").queue();
			} catch (MalformedURLException e) {
				senderChannel.sendMessage("That URL is invalid!").queue();
			}
			
			if(urlQueryParameters.containsKey("list")) {
				String listId = urlQueryParameters.get("list");
				
				senderChannel.sendMessage("Loading playlist... (this might take a couple of seconds)").queue();						
				
				List<VideoDetails> vds = new YoutubeSearch().searchPlaylistViaApi(apiKey, listId, senderChannel, null);
				
				if(vds != null && vds.size() >= 1) {					
					for(VideoDetails details : vds) {								
						processVideoDetails(betterPlayer, parameters, details, false);
					}
					
					User author = jda.getUserById(parameters.getSenderId());
					QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
					int queuePos = qm.getFullQueue(parameters.getGuildId()).size() - qm.getQueueIndex(parameters.getGuildId() -1);

					VideoDetails videoDetails = vds.get(0);
					EmbedBuilder eb = new EmbedBuilder()
							.setTitle("Added " + vds.size() + " tracks to the queue!")
							.setThumbnail(videoDetails.getThumbnailUrl())
							.setColor(Color.GRAY)
							.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
							.addField("Position in queue", String.valueOf(queuePos), true)
							.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
					
					senderChannel.sendMessage(eb.build()).queue();
				} else {	
					senderChannel.sendMessage("Unknown playlist or the playlist contains no videos!").queue();
				}
				
				return;
			} else if(urlQueryParameters.containsKey("v")) {
				String videoId = urlQueryParameters.get("v");
				
				VideoDetails vd = new YoutubeSearch().getVideoDetails(apiKey, videoId, senderChannel);
				
				if(vd != null) {
					processVideoDetails(betterPlayer, parameters, vd, true);
				} else {
					senderChannel.sendMessage("Unknown video!").queue();
				}
				
				return;
			}
		} else if(useApi) {
			VideoDetails details = new YoutubeSearch().searchViaApi(apiKey, parameters.getArgs(), senderChannel);
			System.out.println("api");
			processVideoDetails(betterPlayer, parameters, details, true);
		} else {
			VideoDetails details = new YoutubeSearch().searchViaFrontend(parameters.getArgs());
			processVideoDetails(betterPlayer, parameters, details, true);
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
		
		//Check if VideoDetails and it's parameters are not null
		//If any of them are that means no results were found and we can stop
		if(videoDetails == null || videoDetails.getId() == null || videoDetails.getTitle() == null || videoDetails.getChannel() == null) {
			senderChannel.sendMessage("No results found! Try another search term").queue();
			return;
		}
		
		//Create a QueueItem for the video
		QueueItem qi = new QueueItem(videoDetails.getId(), videoDetails.getTitle(), videoDetails.getChannel());
		
		//Add the item to the queue
		qm.addToQueue(qi, parameters.getGuildId());
		
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
		
		//Calculate the position in the queue
		//If the queue is null, then the queue position is 0
		//Otherwhise the queue position is the full queue's size, minus the current queue index.
		int queuePos;
		if(qm.getFullQueue(guildId) == null) {
			queuePos = 0;
		} else {
			queuePos = qm.getFullQueue(guildId).size() - qm.getQueueIndex(guildId);
		}
		
		//If announce is true, then we want to send a message to the senderChannel
		if(announce) {
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle(videoDetails.getTitle())
					.setThumbnail(videoDetails.getThumbnailUrl())
					.setColor(Color.GRAY)
					.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
					.addField("Channel", videoDetails.getChannel(), true)
					.addField("Duration", videoDetails.getDuration(), true)
					.addField("Position in queue", String.valueOf(queuePos), true)
					.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessage(eb.build()).queue();
		}
	}
}
