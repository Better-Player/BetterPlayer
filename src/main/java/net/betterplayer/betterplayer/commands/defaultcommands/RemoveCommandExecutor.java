package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.utils.Utils;

/**
 * This command provides the user with a way to remove an item from the queue.<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class RemoveCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		long guildId = parameters.getGuildId();
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		//Check if the user provided a queue index for us to remove
		//If not, inform the user and return
		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide a queue index to remove!").queue();
			return;
		}
		
		if(!Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel)) {
			return;
		}
		
		int queueIndex = Integer.valueOf(parameters.getArgs()[0]) -1;
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		QueueManager qm = bam.getQueueManager();
		
		//Get the current queue Index, and calculate the queueIndex to remove
		//We have to add the current queue index to the index the user provided,
		//because we don't remove an item from the queue after it is done playing, but we do show it that way to the user
		int currentQueueIndex = qm.getQueueIndex(guildId);
		int queueItemToRemove = queueIndex + currentQueueIndex +1;
		
		//Get the queue item we want to remove, since we want to inform the user what they removed
		QueueItem itemToRemove = qm.getQueueItemAtIndex(guildId, queueItemToRemove);
		
		//If the itemToRemove is null, that means the index the user provided is more than the length of the queue
		if(itemToRemove == null) {
			senderChannel.sendMessage("No song found at that position in the queue!").queue();
			return;
		}
		
		//Attempt to delete the item from the queue
		boolean success = qm.deleteItemFromQueue(guildId, queueItemToRemove);
		
		//If the deletion did not succeed, inform the user and return
		if(!success) {
			senderChannel.sendMessage("Invalid queue index!").queue();
			return;
		}
		
		//Inform the user what they removed
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor("Removed from the queue", "https://google.com", jda.getUserById(parameters.getSenderId()).getEffectiveAvatarUrl())
				.setColor(Color.GRAY)
				.setTitle(itemToRemove.getTrackArtist() + " - " + itemToRemove.getTrackName())
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		//Send the embed
		senderChannel.sendMessage(eb.build()).queue();
	}
}
