package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class PauseCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters)) {
			return;
		}
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		TextChannel senderChannel = betterPlayer.getJdaHandler().getJda().getTextChannelById(parameters.getChannelId());

		if(bam.getPauseState(parameters.getGuildId())) {
			senderChannel.sendMessage("BetterPlayer is already paused!").queue();
			return;
		}
		
		bam.setPauseState(parameters.getGuildId(), true);
		senderChannel.sendMessage("Pausing!").queue();
	}
}
