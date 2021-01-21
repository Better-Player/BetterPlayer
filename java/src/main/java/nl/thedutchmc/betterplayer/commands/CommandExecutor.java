package nl.thedutchmc.betterplayer.commands;

import nl.thedutchmc.betterplayer.BetterPlayer;

public interface CommandExecutor {
	
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters);
}
