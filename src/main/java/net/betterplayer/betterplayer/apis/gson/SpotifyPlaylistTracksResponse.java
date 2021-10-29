package net.betterplayer.betterplayer.apis.gson;

import net.betterplayer.betterplayer.annotations.Nullable;

public class SpotifyPlaylistTracksResponse {
    private PlaylistItem[] items;
    private String next;

    public SpotifyTrackResponse[] getTracks() {
        SpotifyTrackResponse[] tracks = new SpotifyTrackResponse[this.items.length];
        for(int i = 0; i < this.items.length; i++) {
            tracks[i] = this.items[i].track;
        }

        return tracks;
    }

    @Nullable
    public String getNext() {
        return this.next;
    }

    private class PlaylistItem {
        private SpotifyTrackResponse track;
    }
}
