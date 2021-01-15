package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.Utils;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;
import nl.thedutchmc.betterplayer.search.YoutubeSearch;
import nl.thedutchmc.httplib.Http;
import nl.thedutchmc.httplib.Http.RequestMethod;
import nl.thedutchmc.httplib.Http.ResponseObject;
import nl.thedutchmc.betterplayer.search.*;

public class PlayCommandExecutor implements CommandExecutor {

	private String apiKey;
	
	public PlayCommandExecutor(String apiKey) {
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
		
		/*
		String query = String.join(" ", parameters.getArgs());
		
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("key", apiKey);
		urlParameters.put("snippet", "snippet");
		urlParameters.put("videoCategoryId", "10"); //Only get music
		urlParameters.put("type", "video");
		urlParameters.put("q", query);
		urlParameters.put("maxResults", "1");
		*/
		
		/*
		 * We no longer search via the API. Not until YT increases the quota, at least. It's too expensive in terms of quota.
		 */
		/*ResponseObject ro = null;
		try {
			ro = new Http(BetterPlayer.isDebug()).makeRequest(
					RequestMethod.GET, 
					"https://www.googleapis.com/youtube/v3/search", 
					urlParameters,
					null, 
					null,
					new HashMap<>());
		} catch(MalformedURLException e) {
			BetterPlayer.logError("MalformedURLException occured when querying YouTube! Use --debug for more details");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return;
		} catch(IOException e) {
			BetterPlayer.logError("IOException occured when querying YouTube! Use --debug for more details");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return;
		}
		
		if(ro.getResponseCode() != 200) {
			BetterPlayer.logDebug("Got non-200 status code when querying YouTube!");
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return;
		}
		
		JSONObject response = new JSONObject(ro.getMessage());
		JSONArray items = response.getJSONArray("items");
		
		String videoId = "";
		for(Object oItem : items) {
			JSONObject jsonItem = (JSONObject) oItem;
			videoId = jsonItem.getJSONObject("id").getString("videoId");
			break;
		}*/
		
		//Perform a search via YT's frontend
		VideoDetails details = new YoutubeSearch().search(parameters.getArgs());
		
		/*///Get details about the video found
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("key", apiKey);
		urlParameters.put("part", "snippet,contentDetails");
		urlParameters.put("id", videoId);
		
		ResponseObject ro = null;
		try {
			ro = new Http(BetterPlayer.isDebug()).makeRequest(
					RequestMethod.GET,
					"https://www.googleapis.com/youtube/v3/videos", 
					urlParameters,
					null,
					null, 
					new HashMap<>());
		} catch (MalformedURLException e) {
			BetterPlayer.logError("MalformedURLException occured when querying YouTube for video details! Use --debug for more details");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return;
		} catch (IOException e) {
			BetterPlayer.logError("IOException occured when querying YouTube for video details! Use --debug for more details");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return;
		}
		
		if(ro.getResponseCode() != 200) {
			BetterPlayer.logDebug("Got non-200 status code when querying YouTube!");
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return;
		}
		
		JSONObject response = new JSONObject(ro.getMessage());
		JSONArray items = response.getJSONArray("items");
		for(Object oItem : items) {
			JSONObject jsonItem = (JSONObject) oItem;
			JSONObject snippet = jsonItem.getJSONObject("snippet");
			*/
			//String title = snippet.getString("title");
			//String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
			User author = jda.getUserById(parameters.getSenderId());
			//String channel = snippet.getString("channelTitle");
			
			//JSONObject contentDetails = jsonItem.getJSONObject("contentDetails");
			//String duration = contentDetails.getString("duration").replace("PT", "").replace("M", ":").replace("S", "");
			
			QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
			
			QueueItem qi = new QueueItem(details.Id, details.Title);
			qm.addToQueue(qi, parameters.getGuildId());
			
			//System.out.println(betterPlayer.getBetterAudioManager().isPlaying(parameters.getGuildId()));
			
			if(!betterPlayer.getBetterAudioManager().isPlaying(parameters.getGuildId())) {
				betterPlayer.getBetterAudioManager().loadTrack(details.Id, parameters.getGuildId());
			}
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle(details.Title)
					.setThumbnail(details.Thumbnail)
					.setColor(Color.GRAY)
					.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
					.addField("Channel", details.Channel, true)
					.addField("Duration", details.Duration, true)
					.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessage(eb.build()).queue();
			//break;
	}
}
