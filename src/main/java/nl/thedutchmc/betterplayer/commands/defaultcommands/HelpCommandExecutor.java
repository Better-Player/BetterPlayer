package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

/**
 * This command will provide the user with a help page
 */
public class HelpCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		//Set all constant fields of the embed
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("BetterPlayer Help Menu")
				.setColor(Color.BLUE);

		//Iterate over all commands, and add it
		for(String command : betterPlayer.getCommandManager().getAllCommands()) {
			embedBuilder.appendDescription("- " + betterPlayer.getEventManager().getCommandPrefix() + command + "\n");
		}

		//Send the embed
		senderChannel.sendMessage(embedBuilder.build()).queue();
	}
}