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

/**
 * This command provides the user with a way to shuffle the queue<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class ShuffleCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		//Verify that the user is connected to the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		long guildId = parameters.getGuildId();
		
		QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
		
		//Get the current queue index and the full queue
		int currentIndex = qm.getQueueIndex(guildId);
		List<QueueItem> queue = qm.getFullQueue(guildId);
		
		List<QueueItem> newQueue = new LinkedList<>();
		List<QueueItem> queueToShuffle = new ArrayList<>();
		
		//Iterate over everything we've already played in the queue + the item that is currently playing
		for(int i = 0; i < currentIndex+1; i++) {
			newQueue.add(queue.get(i));
		}
		
		//Iterate over the part of the queue that has not yet been player
		for(int i = currentIndex +1; i < queue.size(); i++) {
			queueToShuffle.add(queue.get(i));
		}
		
		//Shuffle the part of the queue that is yet to be played
		Collections.shuffle(queueToShuffle, new Random());
		
		//Merge the two lists back together
		newQueue.addAll(queueToShuffle);
		
		//Set the new queue
		qm.setQueue(guildId, newQueue);
		
		//Inform the user
		senderChannel.sendMessage("The queue has been shuffled!").queue();
	}

}
