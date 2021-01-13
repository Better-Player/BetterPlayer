package nl.thedutchmc.betterplayer.events.listeners;

import java.util.Arrays;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.thedutchmc.betterplayer.commands.CommandParameters;
import nl.thedutchmc.betterplayer.events.EventManager;

public class MessageReceivedEventHandler extends ListenerAdapter {

	private EventManager eventManager;
	
	public MessageReceivedEventHandler(EventManager eventManager) {
		this.eventManager = eventManager;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {		
				
		if(event.getAuthor().isBot()) return;
		
		final String contentDisplayMessage = event.getMessage().getContentDisplay();
		if(!contentDisplayMessage.startsWith(eventManager.getCommandPrefix())) return;
				
		String commandWithArgs = contentDisplayMessage.replace(eventManager.getCommandPrefix(), "");
		String[] parts = commandWithArgs.split(" ");

		if(parts.length >= 1) {			
			String command = parts[0];
			String[] args = Arrays.copyOfRange(parts, 1, parts.length);
						
			CommandParameters parameters = new CommandParameters(
					event.getAuthor().getIdLong(),
					event.getChannel().getIdLong(), 
					event.getGuild().getIdLong());
			
			parameters.setArgs(args);
			
			boolean resolved = eventManager.getCommandManager().fireCommand(command, parameters);
			
			if(!resolved) {
				TextChannel senderChannel = event.getTextChannel();
				
				EmbedBuilder builder = new EmbedBuilder()
						.setTitle("Unknown command")
						.setDescription("Use ``" + eventManager.getCommandPrefix() + "help`` for a list of supported commands");
				
				senderChannel.sendMessage(builder.build()).queue();
			}
		}
	}
}
