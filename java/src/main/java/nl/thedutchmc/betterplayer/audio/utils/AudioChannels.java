package nl.thedutchmc.betterplayer.audio.utils;

public enum AudioChannels {
	MONO(0),
	STEREO(3),
	LEFT_ONLY(1),
	RIGHT_ONLY(2);
	
	private int value;
	private AudioChannels(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
}
