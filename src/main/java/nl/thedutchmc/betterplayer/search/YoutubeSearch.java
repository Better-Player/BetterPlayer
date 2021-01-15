package nl.thedutchmc.betterplayer.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.Utils;
import nl.thedutchmc.httplib.Http;
import nl.thedutchmc.httplib.Http.RequestMethod;
import nl.thedutchmc.httplib.Http.ResponseObject;

public class YoutubeSearch {
	
	public VideoDetails searchViaFrontend(String[] searchTerms) {
		//Join the search terms together with '+' as delimiter.
		String q = String.join("+", searchTerms);
		
		//URL Parameters
		HashMap<String, String> params = new HashMap<>();
		params.put("q", q);
		
		//Request Headers.
		//TODO Make User-Agent a config option
		HashMap<String, String> headers = new HashMap<>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66");
		
		//Make a request to YouTube Music
		ResponseObject ro = null;
		try {
			ro = new Http(true).makeRequest(RequestMethod.GET, "https://music.youtube.com/search", params, null, null, headers);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		//Get the full webpage from the ResponseObject
		String webpageFull = ro.getMessage();
		
		//Replace \x (ASCII hex) encoding for \\u00 (Unicode hex), and unescape it
		//TODO implement StringEscapeUtils.unescapeJava in Utils class, so I don't have to include the commons3 lib
		String decoded = StringEscapeUtils.unescapeJava(webpageFull.replaceAll(Pattern.quote("\\x"), "\\\\u00"));
				
		//Split up the document. We only want the JSON Data		
		//This code needs to be updated if YouTube changes up their stuff
		String data = decoded
				.split(Pattern.quote("initialData.push({path: '/search',"))[1]
				.split(Pattern.quote("), data: '"))[1]
				.split(Pattern.quote("'});ytcfg.set({'YTMUSIC_INITIAL_DATA'"))[0];
		
		//Time to parse the JSON.
		//Code is pretty self-explanatory, and I'm too lazy to write comments for something that is pretty obvious
		//This code needs to be updated if YouTube changes up their stuff
		JSONObject resultJson = new JSONObject(data);
		JSONArray contentsChild = resultJson
				.getJSONObject("contents")
				.getJSONObject("sectionListRenderer")
				.getJSONArray("contents");
				
		for(Object o : contentsChild) {
			JSONObject oJson = (JSONObject) o;
			JSONArray musicShelfRendererContents = oJson
					.getJSONObject("musicShelfRenderer")
					.getJSONArray("contents");
			
			for(Object o1 : musicShelfRendererContents) {
				JSONObject o1Json = (JSONObject) o1;

				JSONObject renderer = o1Json.getJSONObject("musicResponsiveListItemRenderer");
				String id = renderer
						.getJSONObject("overlay")
						.getJSONObject("musicItemThumbnailOverlayRenderer")
						.getJSONObject("content")
						.getJSONObject("musicPlayButtonRenderer")
						.getJSONObject("playNavigationEndpoint")
						.getJSONObject("watchEndpoint")
						.getString("videoId");

				JSONArray textParts = renderer
						.getJSONArray("flexColumns")
						.getJSONObject(0)
						.getJSONObject("musicResponsiveListItemFlexColumnRenderer")
						.getJSONObject("text")
						.getJSONArray("runs");
				String title = textParts.getJSONObject(0).getString("text");

				textParts = renderer
						.getJSONArray("flexColumns")
						.getJSONObject(1)
						.getJSONObject("musicResponsiveListItemFlexColumnRenderer")
						.getJSONObject("text")
						.getJSONArray("runs");
				String channel = textParts.getJSONObject(2).getString("text");
				String duration = textParts.getJSONObject(6).getString("text");

				JSONArray thumbnails = renderer
						.getJSONObject("thumbnail")
						.getJSONObject("musicThumbnailRenderer")
						.getJSONObject("thumbnail")
						.getJSONArray("thumbnails");
				String thumbnailUrl = thumbnails.getJSONObject(0).getString("url");
				
				return new VideoDetails(id, duration, thumbnailUrl, title, channel);		
			}
		}
		return null;
	}
	
	public VideoDetails searchViaApi(String apiKey, String[] searchTerms, TextChannel senderChannel) {
		String query = String.join(" ", searchTerms);
		
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("key", apiKey);
		urlParameters.put("snippet", "snippet");
		urlParameters.put("videoCategoryId", "10"); //Only get music
		urlParameters.put("type", "video");
		urlParameters.put("q", query);
		urlParameters.put("maxResults", "1");
		
		
		/*
		 * We no longer search via the API. Not until YT increases the quota, at least. It's too expensive in terms of quota.
		 */
		ResponseObject ro = null;
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
			return null;
		} catch(IOException e) {
			BetterPlayer.logError("IOException occured when querying YouTube! Use --debug for more details");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return null;
		}
		
		if(ro.getResponseCode() != 200) {
			BetterPlayer.logDebug("Got non-200 status code when querying YouTube!");
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return null;
		}
		
		JSONObject response = new JSONObject(ro.getMessage());
		JSONArray items = response.getJSONArray("items");
		
		String videoId = "";
		for(Object oItem : items) {
			JSONObject jsonItem = (JSONObject) oItem;
			videoId = jsonItem.getJSONObject("id").getString("videoId");
			break;
		}
		
		//Get details about the video found
		urlParameters.clear();
		urlParameters.put("key", apiKey);
		urlParameters.put("part", "snippet,contentDetails");
		urlParameters.put("id", videoId);
		
		ro = null;
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
			return null;
		} catch (IOException e) {
			BetterPlayer.logError("IOException occured when querying YouTube for video details! Use --debug for more details");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return null;
		}
		
		if(ro.getResponseCode() != 200) {
			BetterPlayer.logDebug("Got non-200 status code when querying YouTube!");
			senderChannel.sendMessage("An unknown error occured. Please try again later!").queue();
			return null;
		}
		
		response = new JSONObject(ro.getMessage());
		items = response.getJSONArray("items");
		for(Object oItem : items) {
			JSONObject jsonItem = (JSONObject) oItem;
			JSONObject snippet = jsonItem.getJSONObject("snippet");
			
			String title = snippet.getString("title");
			String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
			String channel = snippet.getString("channelTitle");
			
			JSONObject contentDetails = jsonItem.getJSONObject("contentDetails");
			String duration = contentDetails.getString("duration").replace("PT", "").replace("M", ":").replace("S", "");
			
			return new VideoDetails(videoId, duration, thumbnailUrl, title, channel);
			
		}
		
		return null;
	}
}
