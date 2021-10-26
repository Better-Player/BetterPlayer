package net.betterplayer.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioObject {

	private final AudioTrack track;
	private final AudioPlayer player;
	private final String name;
	private final String artist;
	
	public AudioObject(AudioTrack track, AudioPlayer player, String name, String artist) {
		this.track = track;
		this.player = player;
		this.name = name;
		this.artist = artist;
		
	}
	
	public AudioTrack getAudioTrack() {
		return this.track;
	}
	
	public AudioPlayer getAudioPlayer() {
		return this.player;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getArtist() {
		return this.artist;
	}
}
