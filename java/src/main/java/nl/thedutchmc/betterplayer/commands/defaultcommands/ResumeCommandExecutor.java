package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

/**
 * This command provides the user with a way to resume a paused player<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class ResumeCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		TextChannel senderChannel = betterPlayer.getJdaHandler().getJda().getTextChannelById(parameters.getChannelId());

		//Check if the player is paused. If not, inform the user and return
		if(!bam.getPauseState(parameters.getGuildId())) {
			senderChannel.sendMessage("BetterPlayer is not paused!").queue();
			return;
		}
		
		//Unpause the player and inform the user
		bam.setPauseState(parameters.getGuildId(), false);
		senderChannel.sendMessage("Resuming!").queue();
	}
}
