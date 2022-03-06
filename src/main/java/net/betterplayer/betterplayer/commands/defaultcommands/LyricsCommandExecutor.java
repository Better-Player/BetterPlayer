package net.betterplayer.betterplayer.commands.defaultcommands;

/*
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import dev.array21.httplib.Http;
import dev.array21.httplib.Http.RequestMethod;
import dev.array21.httplib.Http.ResponseObject;
*/
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
/*
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
*/
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
/*
import net.betterplayer.betterplayer.apis.gson.KsoftLyricsResponse;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
*/
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command will provide lyrics for the currently playing song, if any are available<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "lyrics", description = "**[DEPRECATED]** Get the lyrics for the song which is currently playing", aliases = {"l"})
public class LyricsCommandExecutor implements CommandExecutor {

	//private final ConfigManifest botConfig;
	//private final String KSOFT_LYRICS_SEARCH = "https://api.ksoft.si/lyrics/search";

	public LyricsCommandExecutor(ConfigManifest botConfig) {
	//	this.botConfig = botConfig;
	}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

		// The endpoint has been deprecated
		senderChannel.sendMessage("This command is no longer available").queue();

		/*
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		if(!bam.isPlaying(parameters.getGuildId())) {
			senderChannel.sendMessage("BetterPlayer is currently not playing anything.").queue();
			return;
		}
		
		QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
		Optional<QueueItem> oCurrentlyPlaying = qm.getNowPlaying(parameters.getGuildId());
		if(oCurrentlyPlaying.isEmpty()) {
			senderChannel.sendMessage("BetterPlayer is currently not playing anything.").queue();
			return;
		}
		QueueItem currentlyPlaying = oCurrentlyPlaying.get();

		String searchQuery = currentlyPlaying.artistName() + currentlyPlaying.trackName();
				
		//Verify that the administrator provided a KSoft API token
		if(this.botConfig.getKsoftApiToken() == null) {
			senderChannel.sendMessage("Unable to get the lyrics: This instance of BetterPlayer does not have a KSoft API Token configured. Please contact the administrator of this BetterPlayer instance!").queue();
			return;
		}
		
		//Request headers
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + this.botConfig.getKsoftApiToken().replaceAll("\n", ""));
		
		//Request parameters
		HashMap<String, String> urlParams = new HashMap<>();
		urlParams.put("q", searchQuery);
		
		//Make the HTTP request
		ResponseObject apiResponse;
		try {
			apiResponse = new Http(BetterPlayer.DEBUG).makeRequest(RequestMethod.GET, KSOFT_LYRICS_SEARCH, urlParams, null, null, headers);
		} catch(IOException e) {
			senderChannel.sendMessage("Something went wrong fetching the lyrics. Please try again later").queue();
			
			BetterPlayer.logError(String.format("Somethign went wrong fetching lyrics with search query '%s' from the KSoft API", searchQuery));
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			return;
		}
		
		//If we got a non-200 status code, we cannot continue
		if(apiResponse.getResponseCode() != 200) {
			senderChannel.sendMessage("Received a non 200 status code while fetching the lyrics. Please contact the administrator of this BetterPlayer instance").queue();
			
			BetterPlayer.logError(String.format("Received status code %d while fetching lyrics with search query %s", apiResponse.getResponseCode(), searchQuery));
			BetterPlayer.logDebug(apiResponse.getConnectionMessage());
			
			return;
		}
		
		//Deserialize
		final Gson gson = new Gson();
		KsoftLyricsResponse lyricsResponse = gson.fromJson(apiResponse.getMessage(), KsoftLyricsResponse.class);
		
		//Get the lyrics data
		KsoftLyricsResponse.Data[] lyricsData = lyricsResponse.getData();
		
		//If there length is 0, the API returned no results
		if(lyricsData.length == 0) {
			senderChannel.sendMessage("No lyrics were found for this track. Please try another track!").queue();
			return;
		}
		
		//We're only really interested in the first of the provided results
		KsoftLyricsResponse.Data firstData = lyricsData[0];
		String lyrics = firstData.getLyrics();
		
		//Discord only allows a description to have 2048 characters.
		//So split our lyrics string every 2000 characters
		String[] lyricsSplitEvery2000Chars = lyrics.split("(?<=\\G.{2000})");
		
		//Iterate over every part of the lyrics
		for(int i = 0; i < lyricsSplitEvery2000Chars.length; i++) {
			
			//Base items for the embed
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(BetterPlayer.GRAY);
			eb.setDescription(lyricsSplitEvery2000Chars[i]);
			eb.setFooter("Brought to you by BetterPlayer. Lyrics powered by KSoft.Si", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			//On the first message we want to set some extra bits
			// - Song title
			// - Song thumbnail
			if(i == 0) {
				eb.setTitle(firstData.getArtist() + " - " + firstData.getName());
				
				//Split on a /, and then get the file extension (e.g "jpg")
				String[] thumbnailUrlParts = firstData.getAlbumArt().split(Pattern.quote("/"));
				String fileName = thumbnailUrlParts[thumbnailUrlParts.length -1];
				String extension = fileName.split(Pattern.quote("."))[1];
				
				//Set the thumbnail, including extension
				eb.setThumbnail("attachment://thumbnail." + extension);
				
				//Get the thumbnail as a file
				URL url;
				try {
					url = new URL(firstData.getAlbumArt());
				} catch (MalformedURLException e) {
					senderChannel.sendMessage("Something went wrong fetching the lyrics. Please try again later").queue();
					
					BetterPlayer.logError("Something went wrong setting the thumbnail for lyrics.");
					BetterPlayer.logDebug(Utils.getStackTrace(e));
					
					return;
				}
				
				BufferedImage bufImg;
				try {
					bufImg = ImageIO.read(url);
				} catch (IOException e) {
					senderChannel.sendMessage("Something went wrong fetching the lyrics. Please try again later").queue();
					
					BetterPlayer.logError("Something went wrong setting the thumbnail for lyrics.");
					BetterPlayer.logDebug(Utils.getStackTrace(e));
					
					return;
				}
				
				File tmpFile = new File(fileName);
				tmpFile.deleteOnExit();
				try {
					ImageIO.write(bufImg, extension, tmpFile);
				} catch (IOException e) {
					senderChannel.sendMessage("Something went wrong fetching the lyrics. Please try again later").queue();
					
					BetterPlayer.logError("Something went wrong setting the thumbnail for lyrics.");
					BetterPlayer.logDebug(Utils.getStackTrace(e));
					
					return;
				}
				
				//Attach the file to the message
				senderChannel.sendFile(tmpFile, "thumbnail." + extension).setEmbeds(eb.build()).queue();
				try {
					Thread.sleep(5000);
				} catch(InterruptedException e) {
					BetterPlayer.logDebug(Utils.getStackTrace(e));
				}
				
				tmpFile.delete();
			} else {
				senderChannel.sendMessageEmbeds(eb.build()).queue();
			}
		}
		 */
	}
}
