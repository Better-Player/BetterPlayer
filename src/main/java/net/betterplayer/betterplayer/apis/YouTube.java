package net.betterplayer.betterplayer.apis;

import com.google.gson.Gson;
import dev.array21.httplib.Http;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.apis.exceptions.YouTubeApiException;
import net.betterplayer.betterplayer.apis.gson.YouTubeMusicFrontendSearchResponse;
import net.betterplayer.betterplayer.apis.gson.YouTubePlaylistItemsResponse;
import net.betterplayer.betterplayer.apis.gson.YouTubeSearchResponse;
import net.betterplayer.betterplayer.apis.gson.YouTubeVideoResponse;
import net.betterplayer.betterplayer.config.ConfigManifest;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class YouTube {

    private static final Http http = new Http();
    private static final Gson gson = new Gson();

    /**
     * Get the YouTube API key
     * @return The YouTube API key
     */
    private static String getApiKey() {
        ConfigManifest man = BetterPlayer.getBetterPlayer().getConfig();
        return man.getGoogleApiKey();
    }

    /**
     * Perform a video search via YouTube Music's Frontend
     * @param searchTerms The search terms to use
     * @return Returns the details of the first result. Empty if no results were returned
     * @throws IOException When an IOException occurs
     * @throws YouTubeApiException When the API or the frontend returns a non-200 status code
     */
    public static Optional<VideoDetails> frontendSearch(String[] searchTerms) throws IOException {
        String q = String.join("+", searchTerms);

        HashMap<String, String> params = new HashMap<>();
        params.put("q", q);

        //TODO Make User-Agent a config option
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66");

        Http.ResponseObject ro = http.makeRequest(Http.RequestMethod.GET, "https://music.youtube.com/search", params, null, null, headers);
        if(ro.getResponseCode() != 200) {
            throw new YouTubeApiException(ro.getConnectionMessage());
        }

        String webpageFull = ro.getMessage();

        //Replace \x (ASCII hex) encoding for \\u00 (Unicode hex), and unescape it
        String decoded = StringEscapeUtils.unescapeJava(webpageFull.replaceAll(Pattern.quote("\\x"), "\\\\u00"));

        //Split up the document. We only want the JSON Data
        String data = decoded
                .split(Pattern.quote("initialData.push({path: '/search',"))[1]
                .split(Pattern.quote("), data: '"))[1]
                .split(Pattern.quote("'});ytcfg.set({'YTMUSIC_INITIAL_DATA'"))[0];

        YouTubeMusicFrontendSearchResponse searchResults = gson.fromJson(data, YouTubeMusicFrontendSearchResponse.class);
        String id = searchResults.getVideoId();
        if(id == null) {
            return Optional.empty();
        }

        return YouTube.getVideoDetails(id);
    }

    /**
     * Get items in a playlist
     * @param playlistId The ID of the playlist
     * @return Returns a List of VideoDetails. Empty if the playlist does not exist
     * @throws IOException When an IOException occurs
     * @throws YouTubeApiException When the API or the frontend returns a non-200 status code
     */
    public static Optional<List<VideoDetails>> getPlaylistitems(String playlistId) throws IOException {
        return getPlaylistItems(playlistId, null);
    }

    /**
     * Get items in a playlist
     * @param playlistId The ID of the playlist
     * @param nextPageToken The next page token
     * @return Returns a List of VideoDetails. Empty if the playlist does not exist
     * @throws IOException When an IOException occurs
     * @throws YouTubeApiException When the API or the frontend returns a non-200 status code
     */
    private static Optional<List<VideoDetails>> getPlaylistItems(String playlistId, String nextPageToken) throws IOException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("key", getApiKey());
        urlParameters.put("playlistId", playlistId);
        urlParameters.put("maxResults", "50");
        urlParameters.put("part", "snippet");

        if(nextPageToken != null) {
            urlParameters.put("pageToken", nextPageToken);
        }

        Http.ResponseObject ro = http.makeRequest(Http.RequestMethod.GET, "https://www.googleapis.com/youtube/v3/playlistItems", urlParameters, null, null, null);

        if(ro.getResponseCode() != 200) {
            if(ro.getResponseCode() == 404) {
                BetterPlayer.logDebug("Got 404 for playlistItems");
                return Optional.empty();
            }

            throw new YouTubeApiException(ro.getConnectionMessage());
        }

        YouTubePlaylistItemsResponse r = gson.fromJson(ro.getMessage(), YouTubePlaylistItemsResponse.class);
        String[] itemIds = r.getVideoIds();
        List<VideoDetails> result = new LinkedList<>();

        for(String itemId : itemIds) {
            Optional<VideoDetails> vd = getVideoDetails(itemId);
            if(vd.isEmpty()) {
                continue;
            }

            result.add(vd.get());
        }

        if(r.getNextPageToken() != null) {
            Optional<List<VideoDetails>> nextPage = getPlaylistItems(playlistId, r.getNextPageToken());
            if(nextPage.isPresent()) {
                result.addAll(nextPage.get());
            }
        }

        return Optional.of(result);
    }

    /**
     * Perform a search via the API
     * @param searchTerms The search terms
     * @return Returns the first result. Empty if no results were returned
     * @throws IOException When an IOException occurs
     * @throws YouTubeApiException When the API or the frontend returns a non-200 status code
     */
    public static Optional<VideoDetails> apiSearch(String[] searchTerms) throws IOException {
        String query = String.join(" ", searchTerms);

        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("key", getApiKey());
        urlParameters.put("snippet", "snippet");
        urlParameters.put("videoCategoryId", "10"); //Only get music
        urlParameters.put("type", "video");
        urlParameters.put("q", query);
        urlParameters.put("maxResults", "1");

        Http.ResponseObject ro = http.makeRequest(Http.RequestMethod.GET, "https://www.googleapis.com/youtube/v3/search", urlParameters, null, null, null);

        if(ro.getResponseCode() != 200) {
            throw new YouTubeApiException(ro.getConnectionMessage());
        }

        YouTubeSearchResponse r = gson.fromJson(ro.getMessage(), YouTubeSearchResponse.class);
        String[] results = r.getIds();
        if(results.length == 0) {
            return Optional.empty();
        }

        return getVideoDetails(results[0]);
    }

    /**
     * Get details about a video
     * @param videoId The ID of the video
     * @return Returns the details of the video. Empty if the video does not exist
     * @throws IOException When an IOException occurs
     * @throws YouTubeApiException When the API or the frontend returns a non-200 status code
     */
    public static Optional<VideoDetails> getVideoDetails(String videoId) throws IOException {
        HashMap<String, String> urlParameters = new HashMap<>();
        urlParameters.put("key", getApiKey());
        urlParameters.put("part", "snippet,contentDetails");
        urlParameters.put("id", videoId);

        Http.ResponseObject ro = http.makeRequest(Http.RequestMethod.GET, "https://www.googleapis.com/youtube/v3/videos", urlParameters, null, null, null);

        if(ro.getResponseCode() != 200) {
            if(ro.getResponseCode() == 404) {
                return Optional.empty();
            }

            throw new YouTubeApiException(ro.getConnectionMessage());
        }

        YouTubeVideoResponse r = gson.fromJson(ro.getMessage(), YouTubeVideoResponse.class);
        YouTubeVideoResponse.VideoItem[] items = r.getVideoItems();

        if(items.length == 0) {
            return Optional.empty();
        }

        YouTubeVideoResponse.VideoItem item = items[0];
        return Optional.of(new VideoDetails(
                item.getId(),
                item.getDuration().replace("PT", "").replace("M", ":").replace("S", ""),
                item.getDefaultThumbnailUrl(),
                item.getTitle(),
                item.getChannelTitle()));
    }
}