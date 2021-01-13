package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class LeaveCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		long guildId = parameters.getGuildId();
		
		VoiceChannel vcToLeave = null;
		boolean vcFound = false;
		for(VoiceChannel vc : betterPlayer.getBetterAudioManager().getConnectedVoiceChannels()) {
			if(vc.getGuild().getIdLong() == guildId) {
				vcFound = true;
				vcToLeave = vc;
				break;
			}
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel tc = jda.getTextChannelById(parameters.getChannelId());
		
		if(vcFound) {
			betterPlayer.getBetterAudioManager().leaveAudioChannel(vcToLeave);
			tc.sendMessage("Successfully the channel: " + vcToLeave.getName()).queue();
		} else {
			tc.sendMessage("I'm not connected to a voice channel!").queue();
		}
	}
}
