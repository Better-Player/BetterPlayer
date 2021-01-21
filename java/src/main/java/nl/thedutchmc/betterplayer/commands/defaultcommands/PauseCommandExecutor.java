package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

/**
 * This command provides the user with a way to pause BetterPlayer<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class PauseCommandExecutor implements CommandExecutor {

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
