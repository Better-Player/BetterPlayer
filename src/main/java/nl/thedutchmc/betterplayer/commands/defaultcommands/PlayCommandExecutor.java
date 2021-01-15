package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;
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
		
		VideoDetails details = null;
		if(useApi) {
			details = new YoutubeSearch().searchViaApi(apiKey, parameters.getArgs(), senderChannel);
		} else {
			details = new YoutubeSearch().searchViaFrontend(parameters.getArgs());
		}
		
		QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
		
		QueueItem qi = new QueueItem(details.getId(), details.getTitle(), details.getChannel());
		qm.addToQueue(qi, parameters.getGuildId());
		
		//System.out.println(betterPlayer.getBetterAudioManager().isPlaying(parameters.getGuildId()));
		User author = jda.getUserById(parameters.getSenderId());

		if(!betterPlayer.getBetterAudioManager().isPlaying(parameters.getGuildId())) {
			betterPlayer.getBetterAudioManager().loadTrack(details.getId(), parameters.getGuildId());
		}
		
		int queuePos = qm.getFullQueue(parameters.getGuildId()).size() - qm.getQueueIndex(parameters.getGuildId());
		
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle(details.getTitle())
				.setThumbnail(details.getThumbnailUrl())
				.setColor(Color.GRAY)
				.setAuthor("Adding to the queue", "https://google.com", author.getEffectiveAvatarUrl())
				.addField("Channel", details.getChannel(), true)
				.addField("Duration", details.getDuration(), true)
				.addField("Position in queue", String.valueOf(queuePos), true)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		senderChannel.sendMessage(eb.build()).queue();
		//break;
	}
}
