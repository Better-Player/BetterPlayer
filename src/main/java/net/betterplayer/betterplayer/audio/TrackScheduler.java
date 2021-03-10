package net.betterplayer.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;

public class TrackScheduler extends AudioEventAdapter {
	
	private final BetterAudioManager betterAudioManager;
	
	public TrackScheduler(BetterAudioManager audioManager) {
		this.betterAudioManager = audioManager;
	}
	
	@Override
	public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack currentTrack, AudioTrackEndReason endReason) {
		if(endReason == AudioTrackEndReason.FINISHED) {
			
			long guildId = betterAudioManager.getGuildId(audioPlayer);		
			QueueManager qm = betterAudioManager.getQueueManager();
			
			//qm.incrementQueueIndex(guildId);
			QueueItem qi = qm.pollQueue(guildId);
			
			if(qi == null) {
				this.betterAudioManager.setPlaying(guildId, false);
				return;
			}
			
			betterAudioManager.loadTrack(qi.getIdentifier(), guildId);
		}
	}
	
	public void queue(AudioTrack audioTrack, long guildId) {
		AudioPlayer audioPlayer = betterAudioManager.getAudioPlayer(guildId);
		audioPlayer.playTrack(audioTrack);
	}
}
