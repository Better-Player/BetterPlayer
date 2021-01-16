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

/**
 * This command provides the user with a way to see what track is currently playing
 */
public class NowPlayingCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		long guildId = parameters.getGuildId();
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		//If BetterPlayer is not playing anything, inform the user and return
		if(!betterPlayer.getBetterAudioManager().isPlaying(guildId)) {
			senderChannel.sendMessage("I'm currently not playing anything!").queue();
			return;
		}
		
		//Get the track which is currently playing, its total length, and how far we're in
		AudioObject currentlyPlaying = betterPlayer.getBetterAudioManager().getCurrentlyPlaying(guildId);
		long trackDuration = currentlyPlaying.getAudioTrack().getDuration();
		long trackPosition = currentlyPlaying.getAudioTrack().getPosition();
		
		//Calculate the percentage of completion. Rather than going 0-10, we go 0-20
		//This is because 10 hyphens isn't all that much
		long percentageTrackComplete = -1;
		if(trackPosition != 0) {
			percentageTrackComplete = (long) 20.0D * trackPosition / trackDuration;
		}
		
		//Construct the visual indicator of how far into the track we are
		String trackProgress = "";
		for(int i = 0; i < 20; i++) {
			
			//If i is equal to the percentageComplete, do a dot rather than a hyphen
			if(i == percentageTrackComplete) {
				trackProgress += "●";
			} else {
				trackProgress += "-";
			}
		}
		
		//Calculate the total duration of the track in a format of mm:ss
		double minDuration = Math.round(TimeUnit.MILLISECONDS.toMinutes(trackDuration) * 100.0) / 100.0;
		double secDuration = Math.round(TimeUnit.MILLISECONDS.toSeconds(trackDuration) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(trackDuration)) * 100.0) / 100.0;
		String duration = String.format("%02d:%02d", (long) minDuration, (long) secDuration);
		
		//Calculate how far into the track we are in a format of mm:ss
		double minPosition = Math.round(TimeUnit.MILLISECONDS.toMinutes(trackPosition) * 100.0) / 100.0;
		double secPosition = Math.round(TimeUnit.MILLISECONDS.toSeconds(trackPosition) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(trackPosition)) * 100.0) / 100.0;
		String position = String.format("%02d:%02d", (long) minPosition, (long) secPosition);
		
		//Combine the two above calculated values
		String progressAsTime = position + "/" + duration;
		
		//Construct the embed
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle(currentlyPlaying.getArtist() + " - " + currentlyPlaying.getName())
				.setColor(Color.GRAY)
				.addField("Progress", trackProgress, true)
				.appendDescription(progressAsTime)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
				
		//Send the embed
		senderChannel.sendMessage(eb.build()).queue();
	}
}
