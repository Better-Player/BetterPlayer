package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class ShuffleCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		long guildId = parameters.getGuildId();
		
		QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
		
		int currentIndex = qm.getQueueIndex(guildId);
		List<QueueItem> queue = qm.getFullQueue(guildId);
		
		List<QueueItem> newQueue = new LinkedList<>();
		List<QueueItem> queueToShuffle = new ArrayList<>();
		
		for(int i = 0; i < currentIndex+1; i++) {
			newQueue.add(queue.get(i));
		}
		
		for(int i = currentIndex +1; i < queue.size(); i++) {
			queueToShuffle.add(queue.get(i));
		}
		
		Collections.shuffle(queueToShuffle, new Random());
		newQueue.addAll(queueToShuffle);
		
		qm.setQueue(guildId, newQueue);
		
		senderChannel.sendMessage("The queue has been shuffled!").queue();
	}

}
