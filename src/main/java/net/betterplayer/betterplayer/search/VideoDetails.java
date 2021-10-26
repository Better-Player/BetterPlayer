package net.betterplayer.betterplayer.search;

public class VideoDetails {
	private final String id, duration, thumbnailUrl, title, channel;
    
    public VideoDetails(String id, String duration, String thumbnailUrl, String title, String channel) {
    	this.id = id;
    	this.duration = duration;
    	this.thumbnailUrl = thumbnailUrl;
    	this.title = title;
    	this.channel = channel;
    }
    
    /**
     * YouTube video ID
     * @return Returns the YouTube video ID
     */ 
    public String getId() {
    	return this.id;
    }
    
    /**
     * The duration of the video in hh:mm:ss
     * @return Returns the duration of the video
     */
    public String getDuration() {
    	return this.duration
    			.replace("H", ":");
    }
    
    /**
     * Thumbnail URL of the video
     * @return Returns the thumbnail URL of the video
     */
    public String getThumbnailUrl() {
    	return this.thumbnailUrl;
    }
    
    /**
     * The title of the video
     * @return Returns the title of the video
     */
    public String getTitle() {
    	return this.title;
    }
    
    /**
     * The channel name (aka artist) of the video
     * @return Returns the channel name of the video
     */
    public String getChannel() {
    	return this.channel;
    }
}
