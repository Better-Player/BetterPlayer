package nl.thedutchmc.betterplayer.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import nl.thedutchmc.betterplayer.JdaHandler;
import nl.thedutchmc.betterplayer.Utils;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;

public class BetterAudioManager {

	private final AudioPlayerManager playerManager;
	private final TrackScheduler trackScheduler;
	private final QueueManager queueManager;
	private final JdaHandler jdaHandler;
	
	private HashMap<Long, AudioPlayer> audioPlayers = new HashMap<>();
	private List<VoiceChannel> connectedChannels = new ArrayList<>();
	private HashMap<Long, Boolean> guildsPlaying = new HashMap<>();
	private HashMap<Long, Long> boundTextChannels = new HashMap<>();
	
	public BetterAudioManager(JdaHandler jdaHandler) {
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		
		queueManager = new QueueManager(this);
		trackScheduler = new TrackScheduler(this);
		
		this.jdaHandler = jdaHandler;
	}
	
	public void init(long guildId) {
		AudioPlayer audioPlayer = playerManager.createPlayer();
		audioPlayer.addListener(trackScheduler);
		
		audioPlayer.setVolume(15);
		
		if(!audioPlayers.containsKey(guildId)) {
			audioPlayers.put(guildId, audioPlayer);
		}
	}
	
	public boolean hasAudioPlayer(long guildId) {
		return audioPlayers.containsKey(guildId);
	}
	
	public void joinAudioChannel(long voiceChannelId, long senderChannelId) {
		VoiceChannel targetChannel = jdaHandler.getJda().getVoiceChannelById(voiceChannelId);
		
		this.init(targetChannel.getGuild().getIdLong());
		
		AudioManager am = targetChannel.getGuild().getAudioManager();
		am.openAudioConnection(targetChannel);
		am.setSelfDeafened(false);
		am.setSendingHandler(new AudioPlayerSendHandler(audioPlayers.get(targetChannel.getGuild().getIdLong())));
		
		connectedChannels.add(targetChannel);
		boundTextChannels.put(targetChannel.getGuild().getIdLong(), senderChannelId);
	}
	
	public void leaveAudioChannel(VoiceChannel voiceChannel) {
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
		
		if(found) {
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
	
	public boolean setPauseState(long guildId, boolean pauseState) {
		AudioPlayer audioPlayer = audioPlayers.get(guildId);
		if(audioPlayer == null) {
			return false;
		}
		
		audioPlayer.setPaused(pauseState);
		return true;
	}
	
	public boolean getPauseState(long guildId) {
		AudioPlayer audioPlayer = audioPlayers.get(guildId);
		if(audioPlayer == null) {
			return true;
		}
		
		return audioPlayer.isPaused();
	}
	
	public QueueManager getQueueManager() {
		return this.queueManager;
	}
	
	public AudioObject getCurrentlyPlaying(long guildId) {
		QueueItem qi = queueManager.getCurrentQueueItem(guildId);
		if(qi == null) return null;
		
		AudioPlayer ap = audioPlayers.get(guildId);
		
		if(ap == null) return null;
		AudioTrack at = ap.getPlayingTrack();
		
		AudioObject ao = new AudioObject(at, ap, qi.getTrackName(), qi.getTrackArtist());
		return ao;
	}
	
	public long getGuildId(AudioPlayer audioPlayer) {
		
		long result = 0L;
		for(Map.Entry<Long, AudioPlayer> entry : audioPlayers.entrySet()) {
			if(entry.getValue().equals(audioPlayer)) {
				return entry.getKey();
			}
		}
		
		return result;
	}
	
	/**
	 * Get if BetterPlayer is currently playing something for the specified guild
	 * @param guildId The ID of the guild to check
	 * @return Returns true if it is playing, false if it is not
	 */
	public boolean isPlaying(long guildId) {
		/*Returns true if:
		* - guildsPlaying has the guild in it
		* - the value in guildsPlaying for this guild is true
		*/
		return (guildsPlaying.containsKey(guildId) && guildsPlaying.get(guildId));
	}
	
	public void setPlaying(long guildId, boolean playing) {
		guildsPlaying.put(guildId, playing);
	}
	
	public List<VoiceChannel> getConnectedVoiceChannels() {
		return this.connectedChannels;
	}
	
	public void loadTrack(String identifier, long guildId) {
		guildsPlaying.put(guildId, true);
		
		this.playerManager.loadItem(identifier, new AudioLoadResultHandler() {
			
			@Override
			public void trackLoaded(AudioTrack audioTrack) {
				trackScheduler.queue(audioTrack, guildId);
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				// TODO Auto-generated method stub
				
			}
			
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
	
	private void loadFailed(String message, long guildId) {
		long senderChannelId = boundTextChannels.get(guildId);
		TextChannel senderChannel = jdaHandler.getJda().getTextChannelById(senderChannelId);
		
		//Skip to the next song
		queueManager.incrementQueueIndex(guildId);
		QueueItem qi = queueManager.getCurrentQueueItem(guildId);
		
		if(qi == null) {
			return;
		}
						
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Unable to load track")
				.setDescription(message)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		senderChannel.sendMessage(eb.build()).queue();
		
		//Play the next song!
		loadTrack(qi.getIdentifier(), guildId);
	}
	
	public AudioPlayer getAudioPlayer(long guildId) {
		return this.audioPlayers.get(guildId);
	}
}
