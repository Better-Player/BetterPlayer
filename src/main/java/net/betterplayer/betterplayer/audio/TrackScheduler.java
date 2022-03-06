package net.betterplayer.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

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

			addTrackIfLoop(guildId);
			tryPlayNext(guildId);

		} else if(endReason == AudioTrackEndReason.LOAD_FAILED) {
			long guildId = betterAudioManager.getGuildId(audioPlayer);
			QueueManager qm = betterAudioManager.getQueueManager();

			// Skip to the next track
			addTrackIfLoop(guildId);
			tryPlayNext(guildId);

			// CHeck if we can inform the user
			Optional<Long> maybeBoundChannel = this.betterAudioManager.getBoundTextChannel(guildId);
			if(maybeBoundChannel.isPresent()) {
				// Notify that the track could not be played
				TextChannel boundChannel = this.betterAudioManager.getJda().getTextChannelById(maybeBoundChannel.get());
				if (boundChannel != null && boundChannel.canTalk()) {
					// The bound channel exists and we can talk in it
					Optional<QueueItem> maybeNowPlaying = qm.getNowPlaying(guildId);
					if(maybeNowPlaying.isPresent()) {
						QueueItem nowPlaying = maybeNowPlaying.get();
						EmbedBuilder embedBuilder = new EmbedBuilder()
								.setTitle("Failed to play track")
								.setDescription(String.format("Unfortunately, BetterPlayer was not able to play '%s' by '%s'.", nowPlaying.trackName(), nowPlaying.artistName()))
								.setColor(BetterPlayer.GRAY)
								.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
						boundChannel.sendMessageEmbeds(embedBuilder.build()).queue();
					}
				}
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

	/**
	 * Try to play the next track in queue, if there is any
	 * @param guildId The ID of the guild
	 */
	private void tryPlayNext(long guildId) {
		QueueManager qm = betterAudioManager.getQueueManager();

		Optional<QueueItem> oNextTrack = qm.pollQueue(guildId);
		if(oNextTrack.isEmpty()) {
			this.betterAudioManager.setPlaying(guildId, false);
			return;
		}
		QueueItem nextTrack = oNextTrack.get();

		qm.setNowPlaying(guildId, nextTrack);
		betterAudioManager.loadTrack(nextTrack.trackIdentifier(), guildId);
	}

	/**
	 * Add the track that has ended to the back of the queue, if loop mode is enabled
	 * @param guildId The ID of the guild
	 */
	private void addTrackIfLoop(long guildId) {
		QueueManager qm = betterAudioManager.getQueueManager();

		if(qm.isLoopMode(guildId)) {
			Optional<QueueItem> oPreviousTrack = qm.getNowPlaying(guildId);
			if(oPreviousTrack.isEmpty()) {
				BetterPlayer.logError("oPreviousTrack is Empty. This should not be possible on track end");
				return;
			}
			QueueItem previousTrack = oPreviousTrack.get();

			qm.addToQueue(guildId, previousTrack);
		}
	}
}