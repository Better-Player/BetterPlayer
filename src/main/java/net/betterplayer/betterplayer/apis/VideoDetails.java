package net.betterplayer.betterplayer.apis;

public record VideoDetails(String id, String duration, String thumbnail, String title, String channel) {

    /**
     * The duration of the video in hh:mm:ss
     * @return Returns the duration of the video
     */
    public String getDuration() {
        return this.duration
                .replace("H", ":");
    }
}
