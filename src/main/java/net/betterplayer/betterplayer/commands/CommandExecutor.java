package net.betterplayer.betterplayer.commands;

import net.betterplayer.betterplayer.BetterPlayer;

public interface CommandExecutor {
	
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters);
}
