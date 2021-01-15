package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.AudioObject;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class ForceSkipExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {

		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		User sender = jda.getUserById(parameters.getSenderId());
		
		long guildId = parameters.getGuildId();
		
		BetterAudioManager betterAudioManager = betterPlayer.getBetterAudioManager();
		AudioObject currentlyPlaying = betterAudioManager.getCurrentlyPlaying(guildId);
		
		QueueManager qm = betterAudioManager.getQueueManager();
		qm.incrementQueueIndex(guildId);
		QueueItem qi = qm.getCurrentQueueItem(guildId);
		
		if(qi != null) {
			betterAudioManager.loadTrack(qi.getIdentifier(), guildId);
		}
		
		EmbedBuilder eb = new EmbedBuilder()
				.setAuthor("Force skipped " + currentlyPlaying.getName(), "https://google.com", sender.getEffectiveAvatarUrl())
				.setColor(Color.GRAY)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		
		senderChannel.sendMessage(eb.build()).queue();
	}
}
