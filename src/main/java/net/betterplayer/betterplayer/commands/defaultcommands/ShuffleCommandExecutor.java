package net.betterplayer.betterplayer.commands.defaultcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;

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
		List<QueueItem> queue = qm.getFullQueue(guildId);
			
		//Shuffle the part of the queue that is yet to be played
		Collections.shuffle(queue, new Random());

		//Set the new queue
		java.util.Queue<QueueItem> newQueue = new LinkedList<QueueItem>(queue); 
		qm.setQueue(guildId, newQueue);
		
		//Inform the user
		senderChannel.sendMessage("The queue has been shuffled!").queue();
	}

}