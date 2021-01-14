package nl.thedutchmc.betterplayer.audio.queue;

public class QueueItem {

	private String trackName, identifier;
	
	public QueueItem(String identifier, String trackName) {
		this.identifier = identifier;
		this.trackName = trackName;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	
	public String getTrackName() {
		return this.trackName;
	}
	
}
