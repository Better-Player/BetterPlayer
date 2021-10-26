package net.betterplayer.betterplayer.events.listeners;

import java.util.Arrays;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.guild.GuildConfigManifest;
import net.betterplayer.betterplayer.events.EventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceivedEventHandler extends ListenerAdapter {

	private final EventManager eventManager;
	private final BetterPlayer betterPlayer;
	
	public MessageReceivedEventHandler(EventManager eventManager, BetterPlayer betterPlayer) {
		this.eventManager = eventManager;
		this.betterPlayer = betterPlayer;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {		
				
		//Check if the author is a bot, if so return
		if(event.getAuthor().isBot()) return;
		
		//Get the message that was sent
		final String contentDisplayMessage = event.getMessage().getContentDisplay();
		
		GuildConfigManifest manifest = this.betterPlayer.getGuildConfig().getManifest(event.getGuild().getIdLong());
		if(manifest == null) {
			manifest = GuildConfigManifest.defaultManifest();
			this.betterPlayer.getGuildConfig().setManifest(event.getGuild().getIdLong(), manifest);
		}
		
		//If the message does not start with the command prefix, return
		if(manifest.getCommandPrefix() == null) {
			BetterPlayer.logDebug(String.format("GuildCommandPrefix is null. Assuming that guild has no entry in database, adding it now. For guild: %d", event.getGuild().getIdLong()));
			manifest.setCommandPrefix("$");
			this.betterPlayer.getGuildConfig().setManifest(event.getGuild().getIdLong(), manifest);
		}
		
		if(!contentDisplayMessage.startsWith(manifest.getCommandPrefix())) return;
				
		//Remove the commandPrefix, and split on spaces 
		String commandWithArgs = contentDisplayMessage.replace(manifest.getCommandPrefix(), "");
		String[] parts = commandWithArgs.split(" ");

		//If the user provided any input (because they could just type the prefix), then we want to process
		if(parts.length >= 1) {			
			//The command is the first element
			String command = parts[0].toLowerCase();
			
			//The arguments are everything after the first element
			String[] args = Arrays.copyOfRange(parts, 1, parts.length);
						
			//Construct a CommandParameters object
			CommandParameters parameters = new CommandParameters(
					event.getAuthor().getIdLong(),
					event.getChannel().getIdLong(), 
					event.getGuild().getIdLong(),
					event.getMember());
			
			//Set the args in the parameters object
			parameters.setArgs(args);
			
			//Fire the command
			boolean resolved = eventManager.getCommandManager().fireCommand(command, parameters);
			
			//If the command did not complete, inform the user
			if(!resolved) {
				TextChannel senderChannel = event.getTextChannel();
				
				//Construct an embed informing the user the command did not succeed
				EmbedBuilder builder = new EmbedBuilder()
						.setTitle("Unknown command")
						.setDescription("Use ''" + manifest.getCommandPrefix() + "help'' for a list of supported commands");
				
				//Send the embed
				senderChannel.sendMessageEmbeds(builder.build()).queue();
			}
		}
	}
}
