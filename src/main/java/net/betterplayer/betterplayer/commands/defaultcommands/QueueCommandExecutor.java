package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

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

/**
 * This command will provide the user with the queue for the guild they sent it from.<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
@BotCommand(name = "queue", description = "Display the current queue", aliases = {"q"})
public class QueueCommandExecutor implements CommandExecutor {
	
	public QueueCommandExecutor(ConfigManifest botConfig) {}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		//Verify that the user is in the same voice channel as BetterPlayer
		if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
			return;
		}
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		long guildId = parameters.getGuildId();
		
		BetterAudioManager bam = betterPlayer.getBetterAudioManager();
		QueueManager qm = bam.getQueueManager();
		
		/*Fill out constant fields in the embed
		* - Color
		* - Guild name
		*/
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Color.GRAY)
				.setTitle("Queue for " + jda.getGuildById(guildId).getName());
		
		//First we get the currently playing item
		QueueItem currentlyPlaying = qm.getNowPlaying(guildId);
		if(currentlyPlaying == null || !bam.isPlaying(guildId)) {
			senderChannel.sendMessage("I'm currently not playing anything, so the queue is empty!").queue();
			return;
		}
		
		eb.appendDescription("__Now Playing:__\n");
		eb.appendDescription(currentlyPlaying.getTrackArtist() + " - " + currentlyPlaying.getTrackName() + "\n\n");
		
		//Next up we're going to get the rest of the queue
		List<QueueItem> queue = qm.getFullQueue(guildId);
		if(qm.getQueueSize(guildId) > 0) {
			eb.appendDescription("__Up Next:__\n");

			List<String> queueStrings = new LinkedList<>();
			
			for(int i = 0; i < queue.size(); i++) {
				QueueItem qi = queue.get(i);
				queueStrings.add("**" + (i + 1) + ".** " + qi.getTrackArtist() + " - " + qi.getTrackName() + "\n");
			}
			
			//If the user provided no page number, that means they just want the first page
			int page = 0;
			if(parameters.hasArgs()) {
				
				//Check if the provided page number is a positive integer
				if(parameters.getArgs()[0].matches("-?\\d+")) {
					
					BigInteger bigInt = new BigInteger(parameters.getArgs()[0]);
					if(bigInt.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0) {
						senderChannel.sendMessage("That number is too big! Nice try :)").queue();
						return;
					}
					
					if(Integer.valueOf(parameters.getArgs()[0]) <= 0) {
						senderChannel.sendMessage("Only numbers higher than 0 are allowed!").queue();
						return;
					}
					
					//Set the page number.
					//We have to subtract one, because users are 1-based, and the queue is 0 based
					page = Integer.valueOf(parameters.getArgs()[0]) -1;
				} else {
					senderChannel.sendMessage("You must provide a valid number!");
				}
			}
			
			//Set the max amount of items per page (constant) and calculate the amount of pages that are required
			int maxItemsPerPage = 10;
			int queuePages = (int) Math.floor(((double) queueStrings.size() / maxItemsPerPage));
			
			//If the page number provided is larger than the number of queue pages +1 (again, users are 1-based, not 0-based)
			//then inform the user that the number they provided is more than the amount of pages that exist
			if(page > queuePages) {
				senderChannel.sendMessage("The queue is only " + (queuePages+1) + " pages long!").queue();
				return;
			}
					
			//Iterate over the queueStrings list, from the first item on the page to the last item.
			int iCondition = (page * maxItemsPerPage + maxItemsPerPage);
			iCondition = (iCondition >= queueStrings.size()) ? queueStrings.size() : iCondition;
			for(int i = (page * maxItemsPerPage); i < iCondition; i++) {
				eb.appendDescription(queueStrings.get(i));
			}

			//Inform the user how many pages there are, but only if there are multiple
			if(queuePages > 0) {
				eb.addField("Page " + (page +1) + " / " + (queuePages +1), "", false);
			}
		}
		
		//Some neatness and a logo.
		eb.addField(queue.size() + " song(s) in the queue.", "", true);
		eb.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

		//Finally, send the embed!
		senderChannel.sendMessageEmbeds(eb.build()).queue();
	}
}
