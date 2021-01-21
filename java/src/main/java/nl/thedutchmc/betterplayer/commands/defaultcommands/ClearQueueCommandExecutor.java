package nl.thedutchmc.betterplayer.commands.defaultcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

/**
 * This command will provide the user with a way to clear the entire queue for the guild<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class ClearQueueCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {		

		//Verify that the user is in the same channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		long guildId = parameters.getGuildId();
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		
		/*Check if the queue is empty (if it is, it can be null or it's size can be 0)
		* - It is null when the bot has not yet joined a voice channel for this guild, so it has never created a queue for the guild
		* - It's size is 0 when a user has already cleared the queue
		*/
		if(bam.getQueueManager().getFullQueue(guildId) != null && bam.getQueueManager().getFullQueue(guildId).size() == 0) {
			senderChannel.sendMessage("The queue is already empty!").queue();
			return;
		}
		
		//Pause the currently playing track. I suppose when the user clears the queue, they also want to stop the currently playing song
		bam.setPauseState(guildId, true);
		
		//Clear the queue
		bam.getQueueManager().clearQueue(guildId);
		
		//Inform the user that the queue has been cleared
		senderChannel.sendMessage("The queue has been cleared!").queue();
	}
}
