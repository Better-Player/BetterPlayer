package net.betterplayer.betterplayer.apis.gson;

public class SpotifyPlaylistResponse {

    private Image[] images;

    public String getImage() {
        return this.images[0].url;
    }

    private class Image {
        private String url;
    }
}
