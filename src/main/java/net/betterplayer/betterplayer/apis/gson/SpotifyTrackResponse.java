package net.betterplayer.betterplayer.apis.gson;

public class SpotifyTrackResponse {

    private String name;
    private Artist[] artists;

    public String getName() {
        return this.name;
    }

    public String getArtistName() {
        return this.artists[0].name;
    }

    private class Artist {
        private String name;
    }
}
