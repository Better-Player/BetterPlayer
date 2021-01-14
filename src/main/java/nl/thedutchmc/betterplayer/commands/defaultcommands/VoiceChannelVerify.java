package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class VoiceChannelVerify {

	public boolean verify(BetterPlayer betterPlayer, CommandParameters parameters) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		Guild g = jda.getGuildById(parameters.getGuildId());
		User sender = jda.getUserById(parameters.getSenderId());
		
		boolean inVc = false;
		boolean botInVc = false;
		
		VoiceChannel botVc = null;
		VoiceChannel userVc = null;
		
		for(VoiceChannel vc : g.getVoiceChannels()) {
			for(Member m : vc.getMembers()) {
				if(m.getUser().equals(sender)) {
					inVc = true;
					userVc = vc;
				}
				
				if(m.getUser().equals(jda.getSelfUser())) {
					botInVc = true;
					botVc = vc;
				}
			}
		}
		
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		if(!inVc) {
			senderChannel.sendMessage("You are not connected to a voice channel").queue();
			return false;
		}
		
		if(!botInVc) {
			senderChannel.sendMessage("BetterPlayer is not connected to a voice channel!").queue();
			return false;
		}
		
		if(!botVc.equals(userVc)) {
			senderChannel.sendMessage("You are not in the same voice channel as BetterPlayer!").queue();
			return false;
		}
		
		return true;
	}
	
}
