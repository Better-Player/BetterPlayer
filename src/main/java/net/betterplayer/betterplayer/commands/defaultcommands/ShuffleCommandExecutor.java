package net.betterplayer.betterplayer.commands.defaultcommands;

import java.util.*;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command provides the user with a way to shuffle the queue<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "shuffle", description = "Shuffle the queue", aliases = {"s"})
public class ShuffleCommandExecutor implements CommandExecutor {

	public ShuffleCommandExecutor(ConfigManifest botConfig) {}
	
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
		Optional<LinkedList<QueueItem>> oQueue = qm.getFullQueue(guildId);
		if(oQueue.isEmpty()) {
			senderChannel.sendMessage("No Queue exists for this server").queue();
			return;
		}
		LinkedList<QueueItem> queue = oQueue.get();

		//Shuffle the part of the queue that is yet to be played
		Collections.shuffle(queue, new Random());

		//Set the new queue
		java.util.Queue<QueueItem> newQueue = new LinkedList<>(queue);
		qm.setQueue(guildId, newQueue);
		
		//Inform the user
		senderChannel.sendMessage("The queue has been shuffled!").queue();
	}
}