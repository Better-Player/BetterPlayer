package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.AudioObject;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

/**
 * This command will allow the user to force-skip the currently playing item<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class ForceSkipCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		User sender = jda.getUserById(parameters.getSenderId());
		long guildId = parameters.getGuildId();
		
		BetterAudioManager betterAudioManager = betterPlayer.getBetterAudioManager();
		QueueManager qm = betterAudioManager.getQueueManager();
		
		//Get the track which is currently playing, we want to tell the user what they skipped
		AudioObject currentlyPlaying = betterAudioManager.getCurrentlyPlaying(guildId);
		
		//Increment the queue index
		qm.incrementQueueIndex(guildId);
		
		//Get the new current queue item
		QueueItem qi = qm.getCurrentQueueItem(guildId);
		
		//If the new queue item (qi) is null, that means there is no next track.
		//If that is the case, we don't play. Otherwhise we will play the next track
		if(qi != null) {
			betterAudioManager.loadTrack(qi.getIdentifier(), guildId);
		}
		
		//Inform the user what they skipped
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor("Force skipped " + currentlyPlaying.getName(), "https://google.com", sender.getEffectiveAvatarUrl())
				.setColor(Color.GRAY)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		//Send the embed
		senderChannel.sendMessage(eb.build()).queue();
	}
}
