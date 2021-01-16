package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.audio.AudioObject;
import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.audio.queue.QueueItem;
import nl.thedutchmc.betterplayer.audio.queue.QueueManager;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

/**
 * This command will provide the user with the queue for the guild they sent it from.<br>
 * This command requires the user to be connected to the same voice channel as BetterPlayer
 */
public class QueueCommandExecutor implements CommandExecutor {
	
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
		
		//Get the current queue index and the queue itself
		int queueIndex = qm.getQueueIndex(guildId);
		List<QueueItem> queue = qm.getFullQueue(guildId);
		
		//If the queue index is less than 0, then the queue is empty
		if(queueIndex < 0) {
			senderChannel.sendMessage("The queue is empty.").queue();
			return;
		}
		
		//Get the track which is currently playing
		AudioObject currentlyPlaying = bam.getCurrentlyPlaying(guildId);
		
		//Add the name and artist of the track which is currently playing to the embed
		eb.appendDescription("__Now Playing:__\n");
		eb.appendDescription(currentlyPlaying.getArtist() + " - " + currentlyPlaying.getName() + "\n\n");
				
		List<String> queueStrings = new LinkedList<>();
		
		//If the queue size is 1, that means there is no up next category.
		if(queue.size() != 1) {
			eb.appendDescription("__Up Next:__\n");
			
			//Iterate over the rest of the queue, and add them to the queueStrings list
			//Format: {position in queue}. {Artist Name} - {Track name}
			for(int i = (queueIndex +1); i < queue.size(); i++) {
				QueueItem qi = queue.get(i);
				queueStrings.add("**" + (i - queueIndex) + ".** " + qi.getTrackArtist() + " - " + qi.getTrackName() + "\n");
			}
		}
		
		/*
		 * We can only send 2048 characters in the description, so we can't send everything in one go,
		 * so we have to split it over pages.
		 */
		
		
		//If the user provided no page number, that means they just want the first page
		int page = 0;
		if(parameters.hasArgs()) {
			
			//Check if the provided page number is a positive integer
			if(parameters.getArgs()[0].matches("-?\\d+") && Integer.valueOf(parameters.getArgs()[0]) > 0) {
				
				//Set the page number.
				//We have to subtract one, because users are 1-based, and the queue is 0 based
				page = Integer.valueOf(parameters.getArgs()[0]) -1;
			} else {
				senderChannel.sendMessage("You must provide a valid number!");
			}
		}
		
		//Set the max amount of items per page (constant) and calculate the amount of pages that are required
		int maxItemsPerPage = 10;
		int queuePages = (int) Math.floor((((double) queueStrings.size() / maxItemsPerPage)));
		
		//If the page number provided is larger than the number of queue pages +1 (again, users are 1-based, not 0-based)
		//then inform the user that the number they provided is more than the amount of pages that exist
		if(page > queuePages) {
			senderChannel.sendMessage("The queue is only " + (queuePages+1) + " pages long!").queue();
			return;
		}
		
		//Iterate over the queueStrings list, from the first item on the page to the last item.
		for(int i = (page * maxItemsPerPage); i < (page * maxItemsPerPage + maxItemsPerPage); i++) {
			eb.appendDescription(queueStrings.get(i));
		}

		//Inform the user how many pages there are, but only if there are multiple
		if(queuePages > 0) {
			eb.addField("Page " + (page +1) + " / " + (queuePages +1), "", false);
		}
		
		//Some neatness and a logo.
		eb.addField((queue.size() - queueIndex) + " song(s) in the queue.", "", true);
		eb.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

		//Finally, send the embed!
		senderChannel.sendMessage(eb.build()).queue();
	}
}
