package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.Optional;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.AudioObject;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command provides the user with a way to see what track is currently playing
 */
@BotCommand(name = "nowplaying", description = "Display details about the track that is playing at the moment", aliases = {"np"})
public class NowPlayingCommandExecutor implements CommandExecutor {

	public NowPlayingCommandExecutor(ConfigManifest botConfig) {}
	
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
		Optional<AudioObject> oCurrentlyPlaying = betterPlayer.getBetterAudioManager().getCurrentlyPlaying(guildId);
		
		if(oCurrentlyPlaying.isEmpty()) {
			senderChannel.sendMessage("I'm currently not playing anything!").queue();
			return;
		}
		AudioObject currentlyPlaying = oCurrentlyPlaying.get();
		
		if(currentlyPlaying.track() == null) {
			senderChannel.sendMessage("I'm currently not playing anything!").queue();
			return;
		}
		
		long trackDuration = currentlyPlaying.track().getDuration();
		long trackPosition = currentlyPlaying.track().getPosition();
		
		//Calculate the percentage of completion. Rather than going 0-10, we go 0-20
		//This is because 10 hyphens isn't all that much
		long percentageTrackComplete = -1;
		if(trackPosition != 0) {
			percentageTrackComplete = (long) 20.0D * trackPosition / trackDuration;
		}
		
		//Construct the visual indicator of how far into the track we are
		StringBuilder trackProgress = new StringBuilder();
		for(int i = 0; i < 20; i++) {
			
			//If i is equal to the percentageComplete, do a dot rather than a hyphen
			if(i == percentageTrackComplete) {
				trackProgress.append("\u25CF"); //Black dot
			} else {
				trackProgress.append("-");
			}
		}
		
		String progressTimeStamp = Utils.milisToTimeStamp(trackPosition) + "/" + Utils.milisToTimeStamp(trackDuration);
		
		Optional<QueueItem> oCurrentItem = betterPlayer.getBetterAudioManager().getQueueManager().getNowPlaying(guildId);
		if(oCurrentItem.isEmpty()) {
			senderChannel.sendMessage("I'm currently not playing anything!").queue();
			return;
		}
		QueueItem currentItem = oCurrentItem.get();

		//Construct the embed
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle(currentItem.artistName() + " - " + currentItem.trackName())
				.setColor(Color.GRAY)
				.addField("Progress", trackProgress.toString(), true)
				.appendDescription(progressTimeStamp)
				.setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
				
		//Send the embed
		senderChannel.sendMessageEmbeds(eb.build()).queue();
	}
}
