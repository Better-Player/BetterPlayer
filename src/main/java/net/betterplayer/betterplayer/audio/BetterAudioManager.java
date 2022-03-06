package net.betterplayer.betterplayer.audio;

import java.util.*;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.JdaHandler;
import net.betterplayer.betterplayer.annotations.Nullable;
import net.betterplayer.betterplayer.audio.io.AudioSender;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.swing.text.html.Option;

public class BetterAudioManager {

	private final AudioPlayerManager playerManager;
	private final TrackScheduler trackScheduler;
	private final QueueManager queueManager;
	private final JdaHandler jdaHandler;
	
	private final HashMap<Long, AudioPlayer> audioPlayers = new HashMap<>();
	private final List<VoiceChannel> connectedChannels = new ArrayList<>();
	private final HashMap<Long, Boolean> guildsPlaying = new HashMap<>();
	private final HashMap<Long, Long> boundTextChannels = new HashMap<>();
	
	public BetterAudioManager(BetterPlayer betterPlayer) {
		BetterPlayer.logInfo("Loading audio manager");
		
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		
		queueManager = new QueueManager();
		trackScheduler = new TrackScheduler(this);
		
		this.jdaHandler = betterPlayer.getJdaHandler();
	}
	
	public void init(long guildId) {
		AudioPlayer audioPlayer = playerManager.createPlayer();
		audioPlayer.addListener(trackScheduler);
		
		audioPlayer.setVolume(50);
		
		if(!audioPlayers.containsKey(guildId)) {
			audioPlayers.put(guildId, audioPlayer);
		}
	}
	
	public enum SkipAction {
		QUEUE_END,
		NEXT_TRACK,
		OK
	}

	/**
	 * Get the ID of the TextChannel BetterPlayer is bound to in a Guild
	 * @param guildId The ID of the guild
	 * @return The ID of the text channel
	 */
	public Optional<Long> getBoundTextChannel(long guildId) {
		return Optional.ofNullable(this.boundTextChannels.get(guildId));
	}

	/**
	 * Skip N seconds of the current track. If the new position is higher than the track duration, then the next track will be queued
	 * @param guildId The ID of the guild
	 * @param n N seconds to skip
	 * @return Returns the action performed. Empty if no audio player exists for the provided guildId, or if no track is currently playing for the provided guildId
	 */
	public Optional<SkipAction> skipSeconds(long guildId, long n) {
		AudioPlayer ap = this.audioPlayers.get(guildId);
		if(ap == null) {
			return Optional.empty();
		}
		
		AudioTrack at = ap.getPlayingTrack();
		if(at == null) {
			return Optional.empty();
		}
		
		long pos = at.getPosition();
		long newPos = pos + (n * 1000);
		
		long dur = at.getDuration();
		if(newPos >= dur) {
			QueueManager qm = this.queueManager;
			
			Optional<QueueItem> oqi = qm.pollQueue(guildId);
			if(oqi.isEmpty()) {
				this.setPlaying(guildId, false);
				return Optional.of(SkipAction.QUEUE_END);
			}
			QueueItem qi = oqi.get();
			
			qm.setNowPlaying(guildId, qi);
			this.loadTrack(qi.trackIdentifier(), guildId);
			return Optional.of(SkipAction.NEXT_TRACK);
		}
		
		at.setPosition(newPos);
		return Optional.of(SkipAction.OK);
	}

	/**
	 * Check if a Guild has an AudioPlayer
	 * @param guildId The ID of the Guild to check
	 * @return Returns true if the Guild has an audio player
	 */
	public boolean hasAudioPlayer(long guildId) {
		return audioPlayers.containsKey(guildId);
	}

	/**
	 * Join a VoiceChannel
	 * @param voiceChannelId The ID of the VoiceChannel
	 * @param senderChannelId The ID of the TextChannel from which this action was initiated
	 */
	public void joinAudioChannel(long voiceChannelId, long senderChannelId) {
		VoiceChannel targetChannel = jdaHandler.getJda().getVoiceChannelById(voiceChannelId);
		
		this.init(targetChannel.getGuild().getIdLong());
		
		AudioManager am = targetChannel.getGuild().getAudioManager();
		am.openAudioConnection(targetChannel);
		am.setSelfDeafened(false);
		am.setSendingHandler(new AudioSender(audioPlayers.get(targetChannel.getGuild().getIdLong())));
		
		connectedChannels.add(targetChannel);
		boundTextChannels.put(targetChannel.getGuild().getIdLong(), senderChannelId);
	}

	/**
	 * Leave a VoiceChannel
	 * @param voiceChannel The VoiceChannel to leave
	 * @param removeFromList Should the VoiceChannel be removed from the list of connected channels
	 */
	public void leaveAudioChannel(VoiceChannel voiceChannel, boolean removeFromList) {
		voiceChannel.getGuild().getAudioManager().closeAudioConnection();
		
		int indexToRemove = 0;
		boolean found = false;
		for(int i = 0; i < connectedChannels.size(); i++) {
			if(connectedChannels.get(i).equals(voiceChannel)) {
				found = true;
				indexToRemove = i;
				break;
			}
		}
		
		if(found && removeFromList) {
			connectedChannels.remove(indexToRemove);
		}
		
		long guildId = voiceChannel.getGuild().getIdLong();
		
		//Delete this guild from the queue
		queueManager.clearQueue(guildId);
		
		//Remove the audio player for this guild
		audioPlayers.remove(guildId);
		
		//Remove the boundChannel
		boundTextChannels.remove(guildId);
	}

	/**
	 * Set the pause state
	 * @param guildId The ID of the Guild to set the pause state for
	 * @param pauseState The pause state. True indicates a paused state
	 * @return Returns true if the action was successful
	 */
	public boolean setPauseState(long guildId, boolean pauseState) {
		AudioPlayer audioPlayer = audioPlayers.get(guildId);
		if(audioPlayer == null) {
			return false;
		}
		
		audioPlayer.setPaused(pauseState);
		return true;
	}

	/**
	 * Get the current pause state for a Guild
	 * @param guildId The ID of the Guild to check for
 	 * @return Returns true if the guild currently has a paused state. If the Guild has no audio player, true will also be returned
	 */
	public boolean getPauseState(long guildId) {
		AudioPlayer audioPlayer = audioPlayers.get(guildId);
		if(audioPlayer == null) {
			return true;
		}
		
		return audioPlayer.isPaused();
	}

	/**
	 * Get the QueueManager
	 * @return Returns the QueueManager
	 */
	public QueueManager getQueueManager() {
		return this.queueManager;
	}

	/**
	 * Get the AudioObject which is currently playing
	 * @param guildId The ID of the Guild to get the AudioObject for
	 * @return Returns the AudioObject. Empty if the Guild has no AudioPlayer or is currently not playing anything
	 */
	public Optional<AudioObject> getCurrentlyPlaying(long guildId) {
		Optional<QueueItem> onp = queueManager.getNowPlaying(guildId);
		if(onp.isEmpty()) {
			return Optional.empty();
		}
		QueueItem np = onp.get();
		AudioPlayer ap = audioPlayers.get(guildId);
		if(ap == null) {
			return Optional.empty();
		}
		AudioTrack at = ap.getPlayingTrack();

		AudioObject ao = new AudioObject(at, ap, np.trackName(), np.artistName());
		return Optional.of(ao);
	}

	/**
	 * Get the ID of a Guild from an AudioPlayer
	 * @param audioPlayer The AudioPlayer
	 * @return Returns the ID of the Guild
	 */
	public long getGuildId(AudioPlayer audioPlayer) {
		for(Map.Entry<Long, AudioPlayer> entry : audioPlayers.entrySet()) {
			if(entry.getValue().equals(audioPlayer)) {
				return entry.getKey();
			}
		}
		
		return -1L;
	}
	
	/**
	 * Get if BetterPlayer is currently playing something for the specified guild
	 * @param guildId The ID of the guild to check
	 * @return Returns true if it is playing, false if it is not
	 */
	public boolean isPlaying(long guildId) {
		/* Returns true if:
		* - guildsPlaying has the guild in it
		* - the value in guildsPlaying for this guild is true
		*/
		return (guildsPlaying.containsKey(guildId) && guildsPlaying.get(guildId));
	}

	/**
	 * Set the playing state for a Guild
	 * @param guildId The ID of the guild
	 * @param playing True if the Guild is currently in a playing state
	 */
	public void setPlaying(long guildId, boolean playing) {
		guildsPlaying.put(guildId, playing);
	}

	/**
	 * Get the VoiceChannels that are currently connected
	 * @return A List of VoiceChannels currently connected
	 */
	public List<VoiceChannel> getConnectedVoiceChannels() {
		return this.connectedChannels;
	}

	/**
	 * Load a Track
	 * @param identifier The track identifier, the Youtube video ID
	 * @param guildId The ID of the guild to load the track for
	 */
	public void loadTrack(String identifier, long guildId) {
		guildsPlaying.put(guildId, true);
		
		this.playerManager.loadItem(identifier, new AudioLoadResultHandler() {
			
			@Override
			public void trackLoaded(AudioTrack audioTrack) {
				trackScheduler.queue(audioTrack, guildId);
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {}
			
			@Override
			public void noMatches() {
				BetterAudioManager.this.loadFailed("Unable to load track, skipping!", guildId);
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				BetterAudioManager.this.loadFailed(Utils.getStackTrace(exception), guildId);
			}
		});
	}

	/**
	 * Inform the user when a load has failed
	 * @param message The message to send
	 * @param guildId The ID of the Guild for which the loading has failed
	 */
	private void loadFailed(String message, long guildId) {
		long senderChannelId = boundTextChannels.get(guildId);
		TextChannel senderChannel = jdaHandler.getJda().getTextChannelById(senderChannelId);
		
		//Skip to the next song
		Optional<QueueItem> oqi = queueManager.pollQueue(guildId);
		if(oqi.isEmpty()) {
			return;
		}

		QueueItem qi = oqi.get();

		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Unable to load track")
				.setDescription(message)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		senderChannel.sendMessageEmbeds(eb.build()).queue();
		
		//Play the next song!
		loadTrack(qi.trackIdentifier(), guildId);
	}

	/**
	 * Get the AudioPlayer for a Guild
	 * @param guildId The ID of the Guild
	 * @return Returns the AudioPlayer. Empty if the Guild has no AudioPlayer
	 */
	public Optional<AudioPlayer> getAudioPlayer(long guildId) {
		return Optional.ofNullable(this.audioPlayers.get(guildId));
	}

	/**
	 * Get the JDA instance
	 * @return the JDA instance
	 */
	public JDA getJda() {
		return this.jdaHandler.getJda();
	}
}
