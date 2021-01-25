package nl.thedutchmc.betterplayer.events;

import net.dv8tion.jda.api.JDA;
import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.CommandManager;
import nl.thedutchmc.betterplayer.events.listeners.GuildJoinEventHandler;
import nl.thedutchmc.betterplayer.events.listeners.GuildLeaveEventHandler;
import nl.thedutchmc.betterplayer.events.listeners.MessageReceivedEventHandler;

public class EventManager {
	
	private CommandManager commandManager;
	private BetterPlayer betterPlayer;
	
	public EventManager(CommandManager commandManager, BetterPlayer betterPlayer) {
		this.commandManager = commandManager;
		this.betterPlayer = betterPlayer;
	}
	
	/**
	 * Register default event listeners provided by BetterPlayer
	 * @param jda
	 */
	public void registerDefaultEvents(JDA jda) {
		jda.addEventListener(new MessageReceivedEventHandler(this, betterPlayer));
		jda.addEventListener(new GuildJoinEventHandler(betterPlayer));
		jda.addEventListener(new GuildLeaveEventHandler(betterPlayer));
	}
	
	/**
	 * Get the CommandManager
	 * @return Returns the CommandManager
	 */
	public CommandManager getCommandManager() {
		return this.commandManager;
	}
}
