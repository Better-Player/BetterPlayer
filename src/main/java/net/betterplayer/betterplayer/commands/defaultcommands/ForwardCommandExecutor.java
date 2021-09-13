package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.BetterAudioManager.SkipAction;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

@BotCommand(name = "forward", description = "Forward N whole seconds in the current track", aliases = {"f"})
public class ForwardCommandExecutor implements CommandExecutor {

	public ForwardCommandExecutor(ConfigManifest botConfig) {}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		long guildId = parameters.getGuildId();
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		if(!parameters.hasArgs()) {
			senderChannel.sendMessage("You need to provide the amount of whole seconds to skip!").queue();
			return;
		}
		
		if(!Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel)) {
			return;
		}	
		
		int seconds = Integer.parseInt(parameters.getArgs()[0]);
		SkipAction sa = betterPlayer.getBetterAudioManager().skipSeconds(guildId, seconds);
		
		if(sa == null) {
			senderChannel.sendMessage("BetterPlayer is currently not playing anything!").queue();
			return;
		}
		
		EmbedBuilder eb;
		switch(sa) {
		case OK:
			eb = new EmbedBuilder()
				.setTitle(String.format("Forwarded %d seconds ahead!", seconds));
			break;
		case NEXT_TRACK:
			eb = new EmbedBuilder()
				.setTitle("Forwarded to the next track!")
				.setDescription(String.format("After forwarding %d seconds ahead, the end of the track has been reached", seconds));
			break;
		case QUEUE_END:
			eb = new EmbedBuilder()
				.setTitle("The queue has ended!")
				.setDescription(String.format("After forwarding %d seconds ahead, the end of track had been reached, as well as the end of the queue", seconds));
			break;
		default:
			return;
		}
		
		eb.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
		eb.setColor(Color.GRAY);

		senderChannel.sendMessageEmbeds(eb.build()).queue();
	}
}
