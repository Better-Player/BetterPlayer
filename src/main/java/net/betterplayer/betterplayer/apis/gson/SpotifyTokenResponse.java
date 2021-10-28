package net.betterplayer.betterplayer.apis.gson;

import com.google.gson.annotations.SerializedName;

public class SpotifyTokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    public String getAccessToken() {
        return this.accessToken;
    }
}
