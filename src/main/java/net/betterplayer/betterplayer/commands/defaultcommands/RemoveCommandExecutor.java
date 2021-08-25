package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;

/**
 * This command provides the user with a way to remove an item from the queue.<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "remove", description = "Delete an item from the queue, by index shown by $queue", aliases = {"rm", "delete", "del"})
public class RemoveCommandExecutor implements CommandExecutor {

	public RemoveCommandExecutor(ConfigManifest botConfig) {}
	
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
				
		//Get the queue item we want to remove, since we want to inform the user what they removed
		QueueItem itemToRemove = qm.peekQueueAtIndex(guildId, queueIndex);
		
		//If the itemToRemove is null, that means the index the user provided is more than the length of the queue
		if(itemToRemove == null) {
			senderChannel.sendMessage("No song found at that position in the queue!").queue();
			return;
		}
		
		if(queueIndex >= qm.getQueueSize(guildId)) {
			senderChannel.sendMessage("Invalid queue index!").queue();
			return;
		}
		
		//Attempt to delete the item from the queue
		qm.removeFromQueue(guildId, queueIndex);
		
		//Inform the user what they removed
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor("Removed from the queue", "https://google.com", jda.getUserById(parameters.getSenderId()).getEffectiveAvatarUrl())
				.setColor(Color.GRAY)
				.setTitle(itemToRemove.getTrackArtist() + " - " + itemToRemove.getTrackName())
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		//Send the embed
		senderChannel.sendMessageEmbeds(eb.build()).queue();
	}
}
