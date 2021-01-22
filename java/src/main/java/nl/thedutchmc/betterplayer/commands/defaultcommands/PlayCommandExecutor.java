package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.List;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;
import nl.thedutchmc.betterplayer.search.*;

public class PlayCommandExecutor implements CommandExecutor {

	private boolean useApi;
	private String apiKey;
	
	public PlayCommandExecutor(boolean useApi, String apiKey) {
		this.useApi = useApi;
		this.apiKey = apiKey;
	}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, true)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide a search query for me to play!").queue();
			return;
		}
		
		JoinCommandExecutor jce = new JoinCommandExecutor(true);
		jce.fireCommand(betterPlayer, parameters);
		
		//Check if the user supplied a url rather than search terms
		String arg0 = parameters.getArgs()[0];
		if(arg0.contains("https://")) {
			String[] urlParts = arg0.split(Pattern.quote("?"));
			
			if(urlParts.length >= 1) {
				String urlParams = urlParts[1];
				String[] urlParamsSplit = urlParams.split(Pattern.quote("&"));
				
				for(String param : urlParamsSplit) {
					if(param.contains("list")) {
						senderChannel.sendMessage("Loading playlist... (this might take a couple of seconds)").queue();
						
						List<VideoDetails> vds = new YoutubeSearch().searchPlaylistViaApi(apiKey, param.split("=")[1], senderChannel, null);
						
						if(vds != null && vds.size() >= 1) {					
							for(VideoDetails details : vds) {								
								processVideoDetails(betterPlayer, parameters, details, false);
							}
							
							User author = jda.getUserById(parameters.getSenderId());
							QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
							int queuePos = qm.getFullQueue(parameters.getGuildId()).size() - qm.getQueueIndex(parameters.getGuildId() -1);
	
							VideoDetails videoDetails = vds.get(0);
							EmbedBuilder eb = new EmbedBuilder()
									.setTitle("Added " + vds.size() + " tracks to the queue!")
									.setThumbnail(videoDetails.getThumbnailUrl())
									.setColor(Color.GRAY)
									.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
									.addField("Position in queue", String.valueOf(queuePos), true)
									.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
							
							senderChannel.sendMessage(eb.build()).queue();
						} else {	
							senderChannel.sendMessage("Unknown playlist or the playlist contains no videos!").queue();
						}
						
						return;
					} else if(param.contains("v")) {
						VideoDetails vd = new YoutubeSearch().getVideoDetails(apiKey, param.split("=")[1], senderChannel);
						
						if(vd != null) {
							processVideoDetails(betterPlayer, parameters, vd, true);
						} else {
							senderChannel.sendMessage("Unknown video!").queue();
						}
						
						return;
					}
				}
			} else {
				senderChannel.sendMessage("Invalid playlist link!").queue();
			}
		} else if(useApi) {
			VideoDetails details = new YoutubeSearch().searchViaApi(apiKey, parameters.getArgs(), senderChannel);
			
			processVideoDetails(betterPlayer, parameters, details, true);
		} else {
			VideoDetails details = new YoutubeSearch().searchViaFrontend(parameters.getArgs());
			processVideoDetails(betterPlayer, parameters, details, true);
		}
	}
		
	private void processVideoDetails(BetterPlayer betterPlayer, CommandParameters parameters, VideoDetails videoDetails, boolean announce) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
		QueueItem qi = new QueueItem(videoDetails.getId(), videoDetails.getTitle(), videoDetails.getChannel());
		qm.addToQueue(qi, parameters.getGuildId());
		
		User author = jda.getUserById(parameters.getSenderId());
		
		if(!betterPlayer.getBetterAudioManager().isPlaying(parameters.getGuildId())) {			
			betterPlayer.getBetterAudioManager().loadTrack(videoDetails.getId(), parameters.getGuildId());
		}
		
		int queuePos;
		if(qm.getFullQueue(parameters.getGuildId()) == null) {
			queuePos = 0;
		} else {
			queuePos = qm.getFullQueue(parameters.getGuildId()).size() - qm.getQueueIndex(parameters.getGuildId());
		}
		
		if(announce) {
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle(videoDetails.getTitle())
					.setThumbnail(videoDetails.getThumbnailUrl())
					.setColor(Color.GRAY)
					.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
					.addField("Channel", videoDetails.getChannel(), true)
					.addField("Duration", videoDetails.getDuration(), true)
					.addField("Position in queue", String.valueOf(queuePos), true)
					.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessage(eb.build()).queue();
		}
	}
}
