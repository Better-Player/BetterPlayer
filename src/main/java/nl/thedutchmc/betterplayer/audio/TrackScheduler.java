package nl.thedutchmc.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;

public class TrackScheduler extends AudioEventAdapter {
	
	private final BetterAudioManager betterAudioManager;
	
	public TrackScheduler(BetterAudioManager audioManager) {
		this.betterAudioManager = audioManager;
	}
	
	@Override
	public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack currentTrack, AudioTrackEndReason endReason) {
		long guildId = betterAudioManager.getGuildId(audioPlayer);		
		QueueManager qm = betterAudioManager.getQueueManager();
		qm.incrementQueueIndex(guildId);
		QueueItem qi = qm.getCurrentQueueItem(guildId);
		
		if(qi == null) {
			return;
		}
		
		betterAudioManager.loadTrack(qi.getIdentifier(), guildId);
	}
	
	public void queue(AudioTrack audioTrack, long guildId) {
		AudioPlayer audioPlayer = betterAudioManager.getAudioPlayer(guildId);
		audioPlayer.playTrack(audioTrack);
	}
}
