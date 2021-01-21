package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class VoiceChannelVerify {

	/**
	 * Verify that the user is in the same voice channel as BetterPlayer
	 * @param betterPlayer BetterPlayer object
	 * @param parameter CommandParameters
	 * @param joinIfNotConnected Should BetterPlayer join the voice channel if it isn't already connected
	 * @return Returns true if BetterPlayer and the user are in the same voice channel, false otherwise
	 */
	public boolean verify(BetterPlayer betterPlayer, CommandParameters parameters, boolean joinIfNotConnected) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		User sender = jda.getUserById(parameters.getSenderId());
				
		VoiceChannel botVc = null;
		VoiceChannel userVc = null;
		
		//Iterate over all voice channels of the guild
		for(VoiceChannel vc : jda.getGuildById(parameters.getGuildId()).getVoiceChannels()) {
			
			//Iterate over all members of the voice channel
			for(Member m : vc.getMembers()) {
				
				//If the user in the voice channel equals the sender, set the userVc equal to the current vc we're iterating over
				if(m.getUser().equals(sender)) {
					userVc = vc;
				}
				
				//If the user in the voice channel is BetterPlayer, set the botVc equal to the current vc we're iterating over
				if(m.getUser().equals(jda.getSelfUser())) {
					botVc = vc;
				}
			}
		}
		
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		//If userVc is null, that means the user is not connected to a voice channel.
		//Inform them and return
		if(userVc == null) {
			senderChannel.sendMessage("You are not connected to a voice channel").queue();
			return false;
		}
		
		//If botVc is null, that means the bot is not in a voice channel
		if(botVc == null) {
			
			//if joinIfNotConnected is true, we should join the channel
			if(joinIfNotConnected) {
				betterPlayer.getBetterAudioManager().joinAudioChannel(userVc.getIdLong());
				return true;
			} else {
				//Inform the user that BetterPlayer is not connected to a voice channel
				senderChannel.sendMessage("BetterPlayer is not connected to a voice channel!").queue();
				return false;
			}
		}
		
		//Both the user and BetterPlayer are in a voice channel, but not in the same one.
		//Inform the user and return
		if(!botVc.equals(userVc)) {
			senderChannel.sendMessage("You are not in the same voice channel as BetterPlayer!").queue();
			return false;
		}
		
		//All checks passed--The user and BetterPlayer are in the same voice channel!
		return true;
	}
}
