package nl.thedutchmc.betterplayer.search;

public class VideoDetails {
	private String id, duration, thumbnailUrl, title, channel;
    
    public VideoDetails(String id, String duration, String thumbnailUrl, String title, String channel) {
    	this.id = id;
    	this.duration = duration;
    	this.thumbnailUrl = thumbnailUrl;
    	this.title = title;
    	this.channel = channel;
    }
    
    public String getId() {
    	return this.id;
    }
    
    public String getDuration() {
    	return this.duration;
    }
    
    public String getThumbnailUrl() {
    	return this.thumbnailUrl;
    }
    
    public String getTitle() {
    	return this.title;
    }
    
    public String getChannel() {
    	return this.channel;
    }
}
