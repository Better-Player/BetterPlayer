package nl.thedutchmc.betterplayer.events;

import net.dv8tion.jda.api.JDA;
import nl.thedutchmc.betterplayer.commands.CommandManager;
import nl.thedutchmc.betterplayer.events.listeners.MessageReceivedEventHandler;

public class EventManager {
	
	private String commandPrefix;
	private CommandManager commandManager;
	
	public EventManager(String commandPrefix, CommandManager commandManager) {
		this.commandPrefix = commandPrefix;
		this.commandManager = commandManager;
	}
	
	/**
	 * Register default event listeners provided by BetterPlayer
	 * @param jda
	 */
	public void registerDefaultEvents(JDA jda) {
		jda.addEventListener(new MessageReceivedEventHandler(this));
	}
	
	/**
	 * Get the command prefix
	 * @return Returns the command prefix
	 */
	public String getCommandPrefix() {
		return this.commandPrefix;
	}
	
	/**
	 * Get the CommandManager
	 * @return Returns the CommandManager
	 */
	public CommandManager getCommandManager() {
		return this.commandManager;
	}
}
