package net.betterplayer.betterplayer.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import dev.array21.httplib.Http;
import dev.array21.httplib.Http.RequestMethod;
import dev.array21.httplib.Http.ResponseObject;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.apis.gson.YTMFrontendSearch;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.entities.TextChannel;

public class YoutubeSearch {
	
	/**
	 * Perform a search via the YouTube Music 'front-end'
	 * @param searchTerms The search terms to search for
	 * @return Returns a VideoDetails object of the video found. Null if nothing was found
	 */
	public VideoDetails searchViaFrontend(String apiKey, String[] searchTerms, TextChannel senderChannel) {
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
			ro = new Http(BetterPlayer.DEBUG).makeRequest(RequestMethod.GET, "https://music.youtube.com/search", params, null, null, headers);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			//TODO error handling
		} catch (IOException e) {
			e.printStackTrace();
			//TODO error handling
		}
				
		//Get the full webpage from the ResponseObject
		String webpageFull = ro.getMessage();
		
		//Replace \x (ASCII hex) encoding for \\u00 (Unicode hex), and unescape it
		String decoded = StringEscapeUtils.unescapeJava(webpageFull.replaceAll(Pattern.quote("\\x"), "\\\\u00"));
				
		//Split up the document. We only want the JSON Data		
		//This code needs to be updated if YouTube changes up their stuff
		String data = decoded
				.split(Pattern.quote("initialData.push({path: '/search',"))[1]
				.split(Pattern.quote("), data: '"))[1]
				.split(Pattern.quote("'});ytcfg.set({'YTMUSIC_INITIAL_DATA'"))[0];
		
		YTMFrontendSearch searchResults = new Gson().fromJson(data, YTMFrontendSearch.class);
		String id = searchResults.getVideoId();
		return this.getVideoDetails(apiKey, id, senderChannel);
	}
	
	/**
	 * Perform a search for a playlist via the API.
	 * @param apiKey The api key to use
	 * @param playlistId The playlist ID to search for
	 * @param senderChannel The TextChannel to send potential error messages to
	 * @param nextPageToken Used for recursion. If a playlist has multiple pages, you can specify the page to get with this parameter
	 * @return Returns a List of VideoDetail objects
	 */
	public List<VideoDetails> searchPlaylistViaApi(String apiKey, String playlistId, TextChannel senderChannel, String nextPageToken) {
		
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("key", apiKey);
		urlParameters.put("playlistId", playlistId);
		urlParameters.put("maxResults", "50");
		urlParameters.put("part", "snippet");
		
		if(nextPageToken != null) {
			urlParameters.put("pageToken", nextPageToken);
		}
		
		ResponseObject ro = null;
		try {
			ro = new Http(BetterPlayer.DEBUG).makeRequest(RequestMethod.GET, "https://www.googleapis.com/youtube/v3/playlistItems", urlParameters, null, null, new HashMap<>());
		} catch(MalformedURLException e) {
			e.printStackTrace();
			//TODO error handling
		} catch(IOException e) {
			e.printStackTrace();
			//TODO error handling
		}
		
		if(ro.getResponseCode() != 200) {
			BetterPlayer.logError(ro.getConnectionMessage());
			return null;
		}
			
		List<VideoDetails> result = new LinkedList<>();
		
		JSONObject response = new JSONObject(ro.getMessage());
		
		if(response.has("nextPageToken")) {
			String nextPageTokenInRequest = response.getString("nextPageToken");
			result.addAll(searchPlaylistViaApi(apiKey, playlistId, senderChannel, nextPageTokenInRequest));
		}
		
		JSONArray items = response.getJSONArray("items");
		for(Object o : items) {
			JSONObject item = (JSONObject) o;			
			JSONObject snippet = item.getJSONObject("snippet");
			
			String videoId = snippet.getJSONObject("resourceId").getString("videoId");

			VideoDetails vd = getVideoDetails(apiKey, videoId, senderChannel);
			result.add(vd);
		}
		
		return result;
	}
	
	/**
	 * Perform a search via the YouTube API<br>
	 * <br>
	 * <strong>Performing this search costs 100 quota points! Consider searching via the front-end!</strong>
	 * @param apiKey The API key to use
	 * @param searchTerms The search terms to search for
	 * @param senderChannel The TextChannel to send potential errors to
	 * @return Returns a VideoDetails object of the video found. Null if nothing was found
	 */
	public VideoDetails searchViaApi(String apiKey, String[] searchTerms, TextChannel senderChannel) {
		String query = String.join(" ", searchTerms);
		
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("key", apiKey);
		urlParameters.put("snippet", "snippet");
		urlParameters.put("videoCategoryId", "10"); //Only get music
		urlParameters.put("type", "video");
		urlParameters.put("q", query);
		urlParameters.put("maxResults", "1");
		
		ResponseObject ro = null;
		try {
			ro = new Http(BetterPlayer.DEBUG).makeRequest(
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
		
		return getVideoDetails(apiKey, videoId, senderChannel);
	}
	
	/**
	 * Get the details of a video via the API
	 * @param apiKey The API key to use
	 * @param videoId The videoId of the video to get details for
	 * @param senderChannel The TextChannel to send errors to.+
	 * @return
	 */
	public VideoDetails getVideoDetails(String apiKey, String videoId, TextChannel senderChannel) {
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("key", apiKey);
		urlParameters.put("part", "snippet,contentDetails");
		urlParameters.put("id", videoId);
		
		ResponseObject ro = null;
		try {
			ro = new Http(BetterPlayer.DEBUG).makeRequest(
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
		
		JSONObject response = new JSONObject(ro.getMessage());
		JSONArray items = response.getJSONArray("items");
		for(Object oItem : items) {
			JSONObject jsonItem = (JSONObject) oItem;
			JSONObject snippet = jsonItem.getJSONObject("snippet");
			
			String title = snippet.getString("title");
			String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
			String channel =  Utils.fixArtistName(snippet.getString("channelTitle"));
			
			JSONObject contentDetails = jsonItem.getJSONObject("contentDetails");
			String duration = contentDetails.getString("duration").replace("PT", "").replace("M", ":").replace("S", "");
			
			return new VideoDetails(videoId, duration, thumbnailUrl, title, channel);
		}
		
		return null;
	}
}