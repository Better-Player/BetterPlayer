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

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import nl.thedutchmc.betterplayer.JdaHandler;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;

public class BetterAudioManager {

	private final AudioPlayerManager playerManager;
	private final TrackScheduler trackScheduler;
	private final QueueManager queueManager;
	private final JdaHandler jdaHandler;
	
	private HashMap<Long, AudioPlayer> audioPlayers = new HashMap<>();
	private List<VoiceChannel> connectedChannels = new ArrayList<>();
	
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
		
		if(!audioPlayers.containsKey(guildId)) {
			audioPlayers.put(guildId, audioPlayer);
		}
	}
	
	public void joinAudioChannel(long channelId) {
		VoiceChannel targetChannel = jdaHandler.getJda().getVoiceChannelById(channelId);
		
		init(targetChannel.getGuild().getIdLong());
		
		AudioManager am = targetChannel.getGuild().getAudioManager();
		am.openAudioConnection(targetChannel);
		am.setSelfDeafened(false);
		am.setSendingHandler(new AudioPlayerSendHandler(audioPlayers.get(targetChannel.getGuild().getIdLong())));
		
		connectedChannels.add(targetChannel);		
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
		
		audioPlayers.remove(voiceChannel.getGuild().getIdLong());
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
	
	public boolean isPlaying(long guildId) {
		AudioPlayer ap = audioPlayers.get(guildId);		
		return ap.getPlayingTrack() != null;
	}
	
	public List<VoiceChannel> getConnectedVoiceChannels() {
		return this.connectedChannels;
	}
	
	public void loadTrack(String identifier, long guildId) {
		
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public AudioPlayer getAudioPlayer(long guildId) {
		return this.audioPlayers.get(guildId);
	}
}
