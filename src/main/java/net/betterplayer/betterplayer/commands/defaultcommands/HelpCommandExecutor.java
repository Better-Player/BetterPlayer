package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.betterplayer.betterplayer.commands.CommandDetails;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.BotConfig;
import net.betterplayer.betterplayer.utils.Utils;

/**
 * This command will provide the user with a help page
 */
@BotCommand(name = "help", description = "Displays the help menu.")
public class HelpCommandExecutor implements CommandExecutor {

	public HelpCommandExecutor(BotConfig botConfig) {}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		//Set all constant fields of the embed
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("BetterPlayer Help Menu")
				.setColor(Color.GRAY);
				
		//If the user provided no page number, that means they just want the first page
		int pageIndex = 0;
		if(parameters.hasArgs()) {
			
			if(!Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel)) {
				return;
			}
			
			pageIndex = Integer.valueOf(parameters.getArgs()[0]) -1;
		}
		
		List<CommandDetails> cmdDetails = betterPlayer.getCommandManager().getCommandDetails();
		
		//Calculate the amount of pages needed
		int maxItemsPerPage = 8;
		int pageCount = (int) Math.floor(((double) cmdDetails.size() / maxItemsPerPage));
		
		//Check if a user has provided a pageIndex thats higher than the amount of pages there are
		if(pageIndex > pageCount) {
			senderChannel.sendMessage("The help menu only has " + (pageCount+1) + " pages!").queue();
			return;
		}
		
		//Loop over items for the page requested
		int iCondition = pageIndex * maxItemsPerPage + maxItemsPerPage;
		iCondition = (iCondition >= cmdDetails.size()) ? cmdDetails.size() : iCondition;
		for(int i = (pageIndex * maxItemsPerPage); i < iCondition; i++) {
			CommandDetails details = cmdDetails.get(i);
			
			//Nicely format the aliases into a single String and add the command prefix to it
			String aliases = "";
			for(int j = 0; j < details.getAliases().length; j++) {
				aliases += "$" + details.getAliases()[j] + "";
				
				if(j != details.getAliases().length -1) {
					aliases += ", ";
				}
			}
			
			//The description can't fit on a single line, so we have to put in a new line every charsPerLine characters
			//We can only do this after a word though.
		    int charsPerLine = 30;
		    char[] commandDescriptionChars = details.getDescription().toCharArray();
		    char[] commandDescriptionCharsWrapped = new char[commandDescriptionChars.length + (int) (commandDescriptionChars.length / charsPerLine)];
		    int indexInWrapped = 0;
		    boolean spliceOnNextPossible = false;
		    for(int j = 0; j < commandDescriptionChars.length; j++) {
		    	commandDescriptionCharsWrapped[indexInWrapped] = commandDescriptionChars[j];

		    	if(commandDescriptionChars[j] == ' ') {
		    		spliceOnNextPossible = true;
		    	}
		    	
		    	if(j % charsPerLine == 0 && spliceOnNextPossible) {
		    		indexInWrapped++;
		    		commandDescriptionCharsWrapped[indexInWrapped] = '\n';
	    		}
		    	
		    	indexInWrapped++;
		    }

		    //Add the command name field, with value = the description
			embedBuilder.addField(WordUtils.capitalize(details.getName()), new String(commandDescriptionCharsWrapped), true);
			
			//If there are aliases, add it
			if(aliases != "") {
				embedBuilder.addField("Aliases", aliases, true);
			} else {
				embedBuilder.addBlankField(true);
			}
			
			//Blank field for alignment
			embedBuilder.addBlankField(true);
		}
		
		//If there's more than one page, tell the user
		if(pageCount > 0) {
			embedBuilder.addField("Page " + (pageIndex +1) + " / " + (pageCount + 1), "", false);
		}

		//Send the embed
		senderChannel.sendMessageEmbeds(embedBuilder.build()).queue();
	}
}