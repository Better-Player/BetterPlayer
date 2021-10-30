package net.betterplayer.betterplayer.apis.gson;

import com.google.gson.annotations.SerializedName;

public class YouTubeVideoResponse {

    private VideoItem[] items;

    public VideoItem[] getVideoItems() {
        return items;
    }

    public class VideoItem {
        private String id;
        private Snippet snippet;
        private ContentDetails contentDetails;

        public String getId() {
            return id;
        }

        public String getDefaultThumbnailUrl() {
            return snippet.thumbnails.defaultThumbnail.url;
        }

        public String getChannelTitle() {
            return this.snippet.channelTitle;
        }

        public String getTitle() {
            return this.snippet.title;
        }

        public String getDuration() {
            return this.contentDetails.duration;
        }
    }

    private class ContentDetails {
        private String duration;
    }

    private class Snippet {
        private String title;
        private String channelTitle;
        private Thumbnails thumbnails;
    }

    private class Thumbnails {
        @SerializedName("default")
        private Thumbnail defaultThumbnail;
    }

    private class Thumbnail {
        private String url;
    }
}
