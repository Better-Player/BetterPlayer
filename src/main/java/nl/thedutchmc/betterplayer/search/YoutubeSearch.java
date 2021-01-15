package nl.thedutchmc.betterplayer.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import nl.thedutchmc.httplib.Http;
import nl.thedutchmc.httplib.Http.RequestMethod;
import nl.thedutchmc.httplib.Http.ResponseObject;

public class YoutubeSearch {
	
	public VideoDetails search(String[] searchTerms) {
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
				
		//String videoId = "";
		VideoDetails details = new VideoDetails();
		outerJsonParseLoop: for(Object o : contentsChild) {
			JSONObject oJson = (JSONObject) o;
			JSONArray musicShelfRendererContents = oJson
					.getJSONObject("musicShelfRenderer")
					.getJSONArray("contents");
			
			for(Object o1 : musicShelfRendererContents) {
				JSONObject o1Json = (JSONObject) o1;

				JSONObject renderer = o1Json.getJSONObject("musicResponsiveListItemRenderer");
				details.Id = renderer
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
				details.Title = textParts.getJSONObject(0).getString("text");

				textParts = renderer
						.getJSONArray("flexColumns")
						.getJSONObject(1)
						.getJSONObject("musicResponsiveListItemFlexColumnRenderer")
						.getJSONObject("text")
						.getJSONArray("runs");
				details.Channel = textParts.getJSONObject(2).getString("text");
				details.Duration = textParts.getJSONObject(6).getString("text");

				JSONArray thumbnails = renderer
						.getJSONObject("thumbnail")
						.getJSONObject("musicThumbnailRenderer")
						.getJSONObject("thumbnail")
						.getJSONArray("thumbnails");
				details.Thumbnail = thumbnails.getJSONObject(0).getString("url");
				break outerJsonParseLoop;
			}
		}
			
		return details;
	}
}
