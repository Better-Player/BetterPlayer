package net.betterplayer.betterplayer.events;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.commands.CommandManager;
import net.betterplayer.betterplayer.events.listeners.GuildJoinEventHandler;
import net.betterplayer.betterplayer.events.listeners.GuildLeaveEventHandler;
import net.betterplayer.betterplayer.events.listeners.MessageReceivedEventHandler;
import net.dv8tion.jda.api.JDA;

public class EventManager {
	
	private final CommandManager commandManager;
	private final BetterPlayer betterPlayer;
	
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
