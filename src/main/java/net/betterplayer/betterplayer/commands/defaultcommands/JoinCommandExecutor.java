package net.betterplayer.betterplayer.commands.defaultcommands;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;

/**
 * This command will allow the user to have BetterPlayer to join the channel they are currently in.<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer<br>
 * <br>
 * This command is also called from PlayCommandExecutor.
 */
public class JoinCommandExecutor implements CommandExecutor {

	private boolean fromOtherExecutor = false;
	
	public JoinCommandExecutor() {}
	
	/**
	 * If fromOtherExecutor is set to true, this execution will not provide any output to the user.
	 * @param fromOtherExecutor
	 */
	public JoinCommandExecutor(boolean fromOtherExecutor) {
		this.fromOtherExecutor = fromOtherExecutor;
	}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		User sender = jda.getUserById(parameters.getSenderId());

		//Iterate over all voice channels of the guild.
		VoiceChannel vcConnected = null;
		vcLoop: for(VoiceChannel vc : jda.getGuildById(parameters.getGuildId()).getVoiceChannels()) {
			
			//Iterate over all members in the voice channel
			for(Member m : vc.getMembers()) {
				
				//If the user in the channel is the same as the command sender, we've found the channel to connect to
				if(m.getUser().equals(sender)) {
					vcConnected = vc;					
					break vcLoop;
				}
			}
		}
		
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		//If vcConnected is not equal to null, that means we found the channel to join
		if(vcConnected != null) {
			
			if(!VoiceChannelVerify.verifyVoiceChannelPermissions(vcConnected, senderChannel)) {
				return;
			}
			
			//If this command is triggered by another command we do not provide output to the user
			if(!fromOtherExecutor) {
				senderChannel.sendMessage("Joining channel: **" + vcConnected.getName() + "**").queue();
			}
			
			//Temporary
			//EchoHandler eh = new EchoHandler(betterPlayer, senderChannel.getGuild().getIdLong());
			//AudioManager am = jda.getGuildById(parameters.getGuildId()).getAudioManager();
			//am.setReceivingHandler(eh);
			//am.setSendingHandler(eh);
			//am.openAudioConnection(vcConnected);
			
			//Join the voice channel
			betterPlayer.getBetterAudioManager().joinAudioChannel(vcConnected.getIdLong(), senderChannel.getIdLong());

		} else {
			//We did not find a voice channel. That means that the user is not in one, or we can't see it
			senderChannel.sendMessage("You are not connected to a voice channel, or BetterPlayer does not have access to the channel!").queue();
		}
	}
}
