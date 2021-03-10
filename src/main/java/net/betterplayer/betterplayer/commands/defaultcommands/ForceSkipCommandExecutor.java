package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.betterplayer.betterplayer.audio.AudioObject;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;

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
				
		//Get the new current queue item
		QueueItem qi = qm.pollQueue(guildId);
		
		//If the new queue item (qi) is null, that means there is no next track.
		//If that is the case, we don't play. Otherwhise we will play the next track
		if(qi != null) {
			betterAudioManager.loadTrack(qi.getIdentifier(), guildId);
		} else {
			betterAudioManager.getAudioPlayer(guildId).destroy();
			betterAudioManager.setPlaying(parameters.getGuildId(), false);
		}
		
		if(currentlyPlaying == null) {
			senderChannel.sendMessage("Nothing is currently playing!").queue();
			return;
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
