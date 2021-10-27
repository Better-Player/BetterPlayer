package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Optional;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command allows users to move a track at index 'n' to the first place, or to index 'k'
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "move", description = "Move a track to a position in queue, or to first place", aliases = {"mv"})
public class MoveCommandExecutor implements CommandExecutor {

	public MoveCommandExecutor(ConfigManifest botConfig) {}
	
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
			int realIndex = indexToMove +1;
						
			Optional<QueueItem> oItemToMove = qm.peekQueueAtIndex(guildId, realIndex);
			if(oItemToMove.isEmpty()) {
				senderChannel.sendMessage("The track you want to move does not exist").queue();
				return;
			}
			QueueItem itemToMove = oItemToMove.get();

			qm.addToQueueFront(guildId, itemToMove);
			
			//+1 because we've just added an item to the queue, meaning all old indexes
			//have now incremented by one
			qm.removeFromQueue(guildId, realIndex+1);
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Moved " + itemToMove.trackName() + " to first in queue!")
					.setColor(Color.GRAY)
					.addField("Artist", itemToMove.artistName(), true)
					.addField("Position in queue", "1", true)
					.addBlankField(true)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

			senderChannel.sendMessageEmbeds(eb.build()).queue();
		} else if(args.length == 2) {
			if(!(Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel) || Utils.verifyPositiveInteger(parameters.getArgs()[1], senderChannel))) {
				return;
			}
			
			//Index the user gives us
			int indexFrom = Integer.valueOf(parameters.getArgs()[0]);
			int indexTo = Integer.valueOf(parameters.getArgs()[1]);
			
			QueueManager qm = betterPlayer.getBetterAudioManager().getQueueManager();
			
			//Calculate the real index
			int realFrom = indexFrom -1;
			int realTo = indexTo -1;
			
			//Get the item we should move
			Optional<QueueItem> oItemToMove = qm.peekQueueAtIndex(guildId, realFrom);
			if(oItemToMove.isEmpty()) {
				senderChannel.sendMessage("The track you want to move does not exist").queue();
				return;
			}
			QueueItem itemToMove = oItemToMove.get();

			Optional<LinkedList<QueueItem>> oFullQueue = qm.getFullQueue(guildId);
			if(oFullQueue.isEmpty()) {
				senderChannel.sendMessage("No Queue exists for this server").queue();
				return;
			}
			LinkedList<QueueItem> fullQueue = oFullQueue.get();

			//Remove the item from it's current position
			fullQueue.remove(realFrom);
			
			//Insert it in it's new position
			fullQueue.add(realTo, itemToMove);
			
			//Apply the new queue
			java.util.Queue<QueueItem> newQueue = new LinkedList<>(fullQueue);
			qm.setQueue(guildId, newQueue);
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Moved " + itemToMove.trackName() + "!")
					.setColor(Color.GRAY)
					.addField("Artist", itemToMove.artistName(), true)
					.addField("Original position", String.valueOf(indexFrom), true)
					.addField("New position", String.valueOf(indexTo), true)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessageEmbeds(eb.build()).queue();
		}
	}	
}