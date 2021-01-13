package nl.thedutchmc.betterplayer.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.defaultcommands.HelpCommandExecutor;
import nl.thedutchmc.betterplayer.commands.defaultcommands.JoinCommandExecutor;
import nl.thedutchmc.betterplayer.commands.defaultcommands.LeaveCommandExecutor;

public class CommandManager {

	private BetterPlayer betterPlayer;
	private HashMap<String, CommandExecutor> executors = new HashMap<>();
	
	public CommandManager(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
		setupDefault();
	}
	
	public void register(String name, CommandExecutor executor) {
		executors.put(name, executor);
	}
	
	public boolean fireCommand(String name, CommandParameters parameters) {
		CommandExecutor executor = executors.get(name);
		if(executor == null) {
			return false;
		}
		
		executor.fireCommand(betterPlayer, parameters);
		return true;
	}
	
	public List<String> getAllCommands() {
		return new ArrayList<>(executors.keySet());
	}
	
	private void setupDefault() {
		register("join", new JoinCommandExecutor());
		register("help", new HelpCommandExecutor());
		register("leave", new LeaveCommandExecutor());
	}
}
