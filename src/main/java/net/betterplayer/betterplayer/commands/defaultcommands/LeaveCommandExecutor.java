package net.betterplayer.betterplayer.commands.defaultcommands;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;

/**
 * This command allows the user to have the bot leave a voice channel.<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class LeaveCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		long guildId = parameters.getGuildId();
		
		//Iterate over the voice channels BetterPlayer is connected to
		VoiceChannel vcToLeave = null;
		for(VoiceChannel vc : betterPlayer.getBetterAudioManager().getConnectedVoiceChannels()) {
			if(vc.getGuild().getIdLong() == guildId) {
				vcToLeave = vc;
				break;
			}
		}
		
		//When we leave, we also want to clear the queue
		betterPlayer.getBetterAudioManager().getQueueManager().clearQueue(guildId);
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel tc = jda.getTextChannelById(parameters.getChannelId());
		
		//If vcToLeave is not equal to null, BetterPlayer was connected to a voice channel in the guild
		//So leave it, and tell the user that BetterPlayer left
		if(vcToLeave != null) {
			betterPlayer.getBetterAudioManager().leaveAudioChannel(vcToLeave, true);
			tc.sendMessage("Successfully left channel: **" + vcToLeave.getName() + "**").queue();
		} else {
			//BetterPlayer is not connected to a voice channel in the sender's guild, inform them
			tc.sendMessage("I'm not connected to a voice channel!").queue();
		}
	}
}
