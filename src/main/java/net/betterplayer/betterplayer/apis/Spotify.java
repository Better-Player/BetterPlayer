package net.betterplayer.betterplayer.apis;

import com.google.gson.Gson;
import dev.array21.httplib.Http;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.apis.exceptions.SpotifyApiException;
import net.betterplayer.betterplayer.apis.gson.SpotifyTokenResponse;
import net.betterplayer.betterplayer.apis.gson.SpotifyTrackResponse;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.search.VideoDetails;
import org.checkerframework.checker.nullness.Opt;

import javax.print.attribute.standard.Media;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

public class Spotify {

    private static final String baseUrl = "https://api.spotify.com/v1";
    private static final Gson gson = new Gson();
    private static final Http http = new Http();

    /**
     * Get details about a Spotify track. This requires that {@link ConfigManifest#getSpotifyClientId()} and {@link ConfigManifest#getSpotifyClientSecret()} be set
     * @param trackId The ID of the track
     * @return The Track's information
     * @throws IOException
     * @throws SpotifyApiException
     */
    public static Optional<SpotifyTrackResponse> getTrack(String trackId) throws IOException {
        HashMap<String, String> headers = getDefaultHeaders();

        Http.ResponseObject ro = http.makeRequest(Http.RequestMethod.GET, String.format("%s/tracks/%s", baseUrl, trackId), null, null, null, headers);
        if(ro.getResponseCode() != 200) {
            if(ro.getResponseCode() == 404) {
                return Optional.empty();
            }

            throw new SpotifyApiException(String.format("Failed to fetch track: %s", ro.getConnectionMessage()));
        }

        SpotifyTrackResponse r = gson.fromJson(ro.getMessage(), SpotifyTrackResponse.class);
        return Optional.of(r);
    }

    private static HashMap<String, String> getDefaultHeaders() throws IOException {
        final HashMap<String, String> r = new HashMap<>();
        r.put("Authorization", String.format("Bearer %s", getLoginToken()));

        return r;
    }

    private static String getLoginToken() throws IOException {
        ConfigManifest cfg = BetterPlayer.getBetterPlayer().getConfig();

        HashMap<String, String> headers = new HashMap<>();

        String authString = String.format("%s:%s", cfg.getSpotifyClientId(), cfg.getSpotifyClientSecret());
        headers.put("Authorization", String.format("Basic %s", new String(Base64.getEncoder().encode(authString.getBytes()), StandardCharsets.UTF_8)));
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        Http.ResponseObject ro = http.makeRequest(Http.RequestMethod.POST, "https://accounts.spotify.com/api/token", null, Http.MediaFormat.X_WWW_FORM_URLENCODED, "grant_type=client_credentials", headers);
        if(ro.getResponseCode() != 200) {
            throw new SpotifyApiException(String.format("Unable to fetch login token: %s", ro.getConnectionMessage()));
        }

        SpotifyTokenResponse r = gson.fromJson(ro.getMessage(), SpotifyTokenResponse.class);

        return r.getAccessToken();
    }
}
