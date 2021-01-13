package nl.thedutchmc.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {
	
	private final BetterAudioManager betterAudioManager;
	
	public TrackScheduler(BetterAudioManager audioManager) {
		this.betterAudioManager = audioManager;
	}
	
	
	@Override
	public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack currentTrack, AudioTrackEndReason endReason) {
		
	}
	
	public void queue(AudioTrack audioTrack, long guildId) {
		AudioPlayer audioPlayer = betterAudioManager.getAudioPlayer(guildId);
		audioPlayer.playTrack(audioTrack);
	}
}
