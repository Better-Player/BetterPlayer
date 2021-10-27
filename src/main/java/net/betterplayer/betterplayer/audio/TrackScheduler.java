package net.betterplayer.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;

import java.util.Optional;

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

			Optional<QueueItem> oPreviousTrack = qm.getNowPlaying(guildId);
			if(oPreviousTrack.isEmpty()) {
				BetterPlayer.logError("oPreviousTrack is Empty. This should not be possible on track end");
				return;
			}
			QueueItem previousTrack = oPreviousTrack.get();

			Optional<QueueItem> oNextTrack = qm.pollQueue(guildId);
			if(oNextTrack.isEmpty()) {
				this.betterAudioManager.setPlaying(guildId, false);
				return;
			}
			QueueItem nextTrack = oNextTrack.get();

			qm.setNowPlaying(guildId, nextTrack);
			betterAudioManager.loadTrack(nextTrack.trackIdentifier(), guildId);

			if(qm.isLoopMode(guildId)) {
				qm.addToQueue(guildId, previousTrack);
			}
		}
	}
	
	public void queue(AudioTrack audioTrack, long guildId) {
		Optional<AudioPlayer> oAudioPlayer = betterAudioManager.getAudioPlayer(guildId);
		if(oAudioPlayer.isEmpty()) {
			BetterPlayer.logError(String.format("Guild '%d' has no AudioPlayer, but queue() was called for it anyways.", guildId));
			return;
		}

		AudioPlayer audioPlayer = oAudioPlayer.get();
		audioPlayer.playTrack(audioTrack);
	}
}
