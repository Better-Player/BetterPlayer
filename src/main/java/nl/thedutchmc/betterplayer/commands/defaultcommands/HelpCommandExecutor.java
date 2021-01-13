package nl.thedutchmc.betterplayer.commands.defaultcommands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.CommandExecutor;
import nl.thedutchmc.betterplayer.commands.CommandParameters;

public class HelpCommandExecutor implements CommandExecutor {

	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("BetterPlayer Help Menu")
				.setColor(Color.BLUE);

		for(String command : betterPlayer.getCommandManager().getAllCommands()) {
			embedBuilder.appendDescription("- " + betterPlayer.getEventManager().getCommandPrefix() + command + "\n");
		}

		senderChannel.sendMessage(embedBuilder.build()).queue();
	}

}
