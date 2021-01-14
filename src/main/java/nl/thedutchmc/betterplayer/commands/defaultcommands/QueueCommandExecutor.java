package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.AudioObject;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class QueueCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		QueueManager qm = bam.getQueueManager();
		
		Guild g = jda.getGuildById(parameters.getGuildId());
		
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Color.GRAY)
				.setTitle("Queue for " + g.getName());
		
		int queueIndex = qm.getQueueIndex(parameters.getGuildId());
		List<QueueItem> queue = qm.getFullQueue(parameters.getGuildId());
		
		if(queueIndex < 0) {
			senderChannel.sendMessage("The queue is empty.").queue();
			return;
		}
		
		AudioObject currentlyPlaying = bam.getCurrentlyPlaying(parameters.getGuildId());
		
		eb.appendDescription("__Now Playing:__\n");
		eb.appendDescription(currentlyPlaying.getName() + "\n\n");
		
		if(queue.size() != 1) {
			eb.appendDescription("__Up Next:__\n");
			for(int i = (queueIndex +1); i < queue.size(); i++) {
				QueueItem qi = queue.get(i);
				eb.appendDescription("**" + i + ".** " + qi.getTrackName() + "\n");
			}
		}

		eb.addField((queue.size() - queueIndex) + " song(s) in the queue.", "", true);
		eb.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

		senderChannel.sendMessage(eb.build()).queue();
	}
}
