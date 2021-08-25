package net.betterplayer.betterplayer.commands.defaultcommands;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command provides the user with a way to pause BetterPlayer<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "pause", description = "Pause BetterPlayer")
public class PauseCommandExecutor implements CommandExecutor {

	public PauseCommandExecutor(ConfigManifest botConfig) {}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		//Verify if the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		TextChannel senderChannel = betterPlayer.getJdaHandler().getJda().getTextChannelById(parameters.getChannelId());

		//Check if the player is already paused. If so, inform the user and return
		if(bam.getPauseState(parameters.getGuildId())) {
			senderChannel.sendMessage("BetterPlayer is already paused!").queue();
			return;
		}
		
		//Pause the player and inform the user
		bam.setPauseState(parameters.getGuildId(), true);
		senderChannel.sendMessage("Pausing!").queue();
	}
}
