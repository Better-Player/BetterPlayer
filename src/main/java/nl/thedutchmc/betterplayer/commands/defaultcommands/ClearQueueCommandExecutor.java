package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class ClearQueueCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		long guildId = parameters.getGuildId();
		
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		
		if(bam.getQueueManager().getFullQueue(guildId) != null && bam.getQueueManager().getFullQueue(guildId).size() == 0) {
			senderChannel.sendMessage("The queue is already empty!").queue();
			return;
		}
		
		bam.setPauseState(guildId, true);
		bam.getQueueManager().clearQueue(guildId);
		
		senderChannel.sendMessage("The queue has been cleared!").queue();
	}
}
