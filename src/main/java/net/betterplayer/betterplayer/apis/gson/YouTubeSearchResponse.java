package net.betterplayer.betterplayer.apis.gson;

public class YouTubeSearchResponse {

    private SearchItem[] items;

    public String[] getIds() {
        String[] s = new String[this.items.length];
        for(int i = 0; i < this.items.length; i++) {
            s[i] = this.items[i].id.videoId;
        }

        return s;
    }

    private class SearchItem {
        private Id id;
    }

    private class Id {
        private String videoId;
    }

}
