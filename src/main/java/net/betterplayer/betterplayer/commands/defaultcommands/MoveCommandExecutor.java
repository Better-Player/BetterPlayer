package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;

public class MoveCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
	
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		long guildId = parameters.getGuildId();
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide a queue index to move!").queue();
			return;
		}
		
		String[] args = parameters.getArgs();
		
		if(args.length == 1) {
			if(!Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel)) {
				return;
			}
			
			int indexToMove = Integer.valueOf(parameters.getArgs()[0]) -1;
			if(indexToMove == 1) {
				senderChannel.sendMessage("This item is already first in the queue!").queue();
				return;
			}
			
			QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
			int realIndex = qm.getQueueIndex(guildId) + indexToMove +1;
			int currentQueueIndex = qm.getQueueIndex(guildId);
			
			QueueItem itemToMove = qm.getQueueItemAtIndex(guildId, realIndex);
						
			List<QueueItem> upcomingQueue = qm.getFullQueue(guildId).subList(currentQueueIndex+1, qm.getFullQueue(guildId).size()); 
			upcomingQueue.remove(itemToMove);
			upcomingQueue.add(0, itemToMove);
			
			List<QueueItem> newQueue = new LinkedList<>();
			newQueue.addAll(qm.getFullQueue(guildId).subList(0, currentQueueIndex+1));
			newQueue.addAll(upcomingQueue);
			
			qm.setQueue(guildId, newQueue);
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Moved " + itemToMove.getTrackName() + " to first in queue!")
					.setColor(Color.GRAY)
					.addField("Artist", itemToMove.getTrackArtist(), true)
					.addField("Position in queue", "1", true)
					.addBlankField(true)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

			senderChannel.sendMessage(eb.build()).queue();
		} else if(args.length == 2) {
			if(!(Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel) || Utils.verifyPositiveInteger(parameters.getArgs()[1], senderChannel))) {
				return;
			}
			
			//Index the user gives us
			int indexFrom = Integer.valueOf(parameters.getArgs()[0]);
			int indexTo = Integer.valueOf(parameters.getArgs()[1]);
			
			QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
			
			//Calculate the real index
			int realFrom = qm.getQueueIndex(guildId) + indexFrom;
			int realTo = qm.getQueueIndex(guildId) + indexTo;
			
			//Get the item we should move
			QueueItem itemToMove = qm.getQueueItemAtIndex(guildId, realFrom);
			
			List<QueueItem> fullQueue = qm.getFullQueue(guildId);

			//Remove the item from it's current position
			fullQueue.remove(realFrom);
			
			//Insert it in it's new position
			fullQueue.add(realTo, itemToMove);
			
			//Apply the new queue
			qm.setQueue(guildId, fullQueue);
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Moved " + itemToMove.getTrackName() + "!")
					.setColor(Color.GRAY)
					.addField("Artist", itemToMove.getTrackArtist(), true)
					.addField("Original position", String.valueOf(indexFrom), true)
					.addField("New position", String.valueOf(indexTo), true)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessage(eb.build()).queue();
		}
	}	
}