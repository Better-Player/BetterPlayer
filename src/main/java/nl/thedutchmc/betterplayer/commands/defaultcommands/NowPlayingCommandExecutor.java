package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.AudioObject;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class NowPlayingCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		long guildId = parameters.getGuildId();
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		if(!betterPlayer.getBetterAudioManager().isPlaying(guildId)) {
			senderChannel.sendMessage("I'm currently not playing anything!").queue();
			return;
		}
		
		AudioObject currentlyPlaying = betterPlayer.getBetterAudioManager().getCurrentlyPlaying(guildId);
		long trackDuration = currentlyPlaying.getAudioTrack().getDuration();
		long trackPosition = currentlyPlaying.getAudioTrack().getPosition();
		
		long percentageIncrementsOfTen = -1;
		if(trackPosition != 0) {
			percentageIncrementsOfTen = (long) 20.0D * trackPosition / trackDuration;
		}
		
		String trackProgress = "";
		for(int i = 0; i < 20; i++) {
			if(i == percentageIncrementsOfTen) {
				trackProgress += "●";
			} else {
				trackProgress += "-";
			}
		}
		
		double minDuration = Math.round(TimeUnit.MILLISECONDS.toMinutes(trackDuration) * 100.0) / 100.0;
		double secDuration = Math.round(TimeUnit.MILLISECONDS.toSeconds(trackDuration) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(trackDuration)) * 100.0) / 100.0;
		String duration = String.format("%02d:%02d", (long) minDuration, (long) secDuration);
		
		double minPosition = Math.round(TimeUnit.MILLISECONDS.toMinutes(trackPosition) * 100.0) / 100.0;
		double secPosition = Math.round(TimeUnit.MILLISECONDS.toSeconds(trackPosition) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(trackPosition)) * 100.0) / 100.0;;
		String position = String.format("%02d:%02d", (long) minPosition, (long) secPosition);
		
		String progressAsTime = position + "/" + duration;
		
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle(currentlyPlaying.getArtist() + " - " + currentlyPlaying.getName())
				.setColor(Color.GRAY)
				.addField("Progress", trackProgress, true)
				.appendDescription(progressAsTime)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
				
		senderChannel.sendMessage(eb.build()).queue();
	}
}
