package net.betterplayer.betterplayer.commands.defaultcommands;

import java.util.Optional;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * This command will allow the user to force-skip the currently playing item<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "forceskip", description = "Force skip a track", aliases = {"fs"})
public class ForceSkipCommandExecutor implements CommandExecutor {

	public ForceSkipCommandExecutor(ConfigManifest botConfig) {}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		User sender = jda.getUserById(parameters.getSenderId());
		long guildId = parameters.getGuildId();
		
		BetterAudioManager betterAudioManager = betterPlayer.getBetterAudioManager();
		QueueManager qm = betterAudioManager.getQueueManager();
		
		//Get the track which is currently playing, we want to tell the user what they skipped
		Optional<QueueItem> oCurrentlyPlaying = qm.getNowPlaying(guildId);
				
		//Get the new current queue item
		Optional<QueueItem> oqi = qm.pollQueue(guildId);
		
		//If the new queue item (qi) is null, that means there is no next track.
		//If that is the case, we don't play. Otherwhise we will play the next track
		if(oqi.isPresent()) {
			QueueItem qi = oqi.get();

			betterAudioManager.loadTrack(qi.trackIdentifier(), guildId);
			qm.setNowPlaying(guildId, qi);
		} else {
			// We know the player exists, this has been verified it above.
			betterAudioManager.getAudioPlayer(guildId).get().destroy();
			betterAudioManager.setPlaying(parameters.getGuildId(), false);
		}
		
		if(oCurrentlyPlaying.isEmpty()) {
			senderChannel.sendMessage("Nothing is currently playing!").queue();
			return;
		}

		QueueItem currentlyPlaying = oCurrentlyPlaying.get();

		//Inform the user what they skipped
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor("Force skipped " + currentlyPlaying.trackName(), "https://google.com", sender.getEffectiveAvatarUrl())
				.setColor(BetterPlayer.GRAY)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		//Send the embed
		senderChannel.sendMessageEmbeds(eb.build()).queue();
	}
}
