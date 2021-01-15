package nl.thedutchmc.betterplayer.audio.queue;

public class QueueItem {

	private String trackName, identifier, trackArtist;
	
	public QueueItem(String identifier, String trackName, String trackArtist) {
		this.identifier = identifier;
		this.trackName = trackName;
		this.trackArtist = trackArtist;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	
	public String getTrackName() {
		return this.trackName;
	}
	
	public String getTrackArtist() {
		return this.trackArtist;
	}
}
