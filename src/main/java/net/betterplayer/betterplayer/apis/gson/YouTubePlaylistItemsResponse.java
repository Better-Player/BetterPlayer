package net.betterplayer.betterplayer.apis.gson;

public class YouTubePlaylistItemsResponse {

    private String nextPageToken;
    private PlaylistItem[] items;

    public String getNextPageToken() {
        return this.nextPageToken;
    }

    public String[] getVideoIds() {
        String[] s = new String[this.items.length];
        for(int i = 0; i < this.items.length; i++) {
            s[i] = this.items[i].snippet.resourceId.videoId;
        }

        return s;
    }

    private class PlaylistItem {
        private Snippet snippet;
    }

    private class Snippet {
        private ResourceId resourceId;
    }

    private class ResourceId {
        String videoId;
    }
}
