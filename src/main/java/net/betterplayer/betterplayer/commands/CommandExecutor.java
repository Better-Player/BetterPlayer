package net.betterplayer.betterplayer.commands;

import net.betterplayer.betterplayer.BetterPlayer;

public interface CommandExecutor {
	void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters);
}
