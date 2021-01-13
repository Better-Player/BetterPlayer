package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class JoinCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		
		User sender = jda.getUserById(parameters.getSenderId());
		
		VoiceChannel vcConnected = null;
		
		Guild g = jda.getGuildById(parameters.getGuildId());
		vcLoop: for(VoiceChannel vc : g.getVoiceChannels()) {
			for(Member m : vc.getMembers()) {
				if(m.getUser().equals(sender)) {
					vcConnected = vc;
					break vcLoop;
				}
			}
		}
		
		TextChannel tc = jda.getTextChannelById(parameters.getChannelId());
		if(vcConnected != null) {
			tc.sendMessage("Joining channel: " + vcConnected.getName()).queue();
			
			BetterAudioManager bam = betterPlayer.getBetterAudioManager();
			bam.joinAudioChannel(vcConnected.getIdLong());
			
		} else {
			tc.sendMessage("You are not connected to a voice channel!").queue();
			return;
		}
	}
}
