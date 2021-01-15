package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class RemoveCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		long guildId = parameters.getGuildId();
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide a queue index to remove!").queue();
			return;
		}
		
		int queueIndex = Integer.valueOf(parameters.getArgs()[0]);
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		QueueManager qm = bam.getQueueManager();
		
		int currentQueueIndex = qm.getQueueIndex(guildId);
		int queueItemToRemove = queueIndex + currentQueueIndex;
		
		QueueItem itemToRemove = qm.getQueueItemAtIndex(guildId, queueItemToRemove);
		
		if(itemToRemove == null) {
			senderChannel.sendMessage("Invalid queue index!").queue();
			return;
		}
		
		boolean success = qm.deleteItemFromQueue(guildId, queueItemToRemove);
		if(!success) {
			senderChannel.sendMessage("Invalid queue index!").queue();
			return;
		}
		
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor("Removed from the queue", "https://google.com", jda.getUserById(parameters.getSenderId()).getEffectiveAvatarUrl())
				.setColor(Color.GRAY)
				.setTitle(itemToRemove.getTrackArtist() + " - " + itemToRemove.getTrackName())
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		senderChannel.sendMessage(eb.build()).queue();
	}
}
