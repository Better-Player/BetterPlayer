package nl.thedutchmc.betterplayer.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.commands.defaultcommands.*;
import nl.thedutchmc.betterplayer.config.Config;

public class CommandManager {

	private BetterPlayer betterPlayer;
	private HashMap<String, CommandExecutor> executors = new HashMap<>();
	private Config config;
	
	/**
	 * Initialize CommandManager. This should be done only once.
	 * @param betterPlayer BetterPlayer instance
	 * @param config Config instance
	 */
	public CommandManager(BetterPlayer betterPlayer, Config config) {
		this.betterPlayer = betterPlayer;
		this.config = config;
		
		//Setup the default commands
		setupDefault();
	}
	
	/**
	 * Register a command with the CommandManager
	 * @param name The primary name of the command
	 * @param executor The executor of the command
	 * @param aliases String... of aliases for the command
	 */
	public void register(String name, CommandExecutor executor, String... aliases) {
		executors.put(name, executor);
		
		for(String alias : aliases) {
			executors.put(alias, executor);
		}
	}
	
	/**
	 * Fire a command
	 * @param name The name of the command to fire
	 * @param parameters The CommandParameters to pass to the command
	 * @return
	 */
	public boolean fireCommand(String name, CommandParameters parameters) {
		//Get the executor for the name of the command provided
		CommandExecutor executor = executors.get(name);		
		
		//If the executor is null, we don't know a command by that name, so return false
		if(executor == null) {
			return false;
		}
		
		//Fire the command
		executor.fireCommand(betterPlayer, parameters);
		return true;
	}
	
	/**
	 * Get a List of all registered commands
	 * @return Returns a List of names of all registered commands
	 */
	public List<String> getAllCommands() {
		return new ArrayList<>(executors.keySet());
	}
	
	/**
	 * This function will set up the default CommandExecutors shipped with BetterPlayer
	 */
	private void setupDefault() {
		register("join", new JoinCommandExecutor());
		register("help", new HelpCommandExecutor());
		register("leave", new LeaveCommandExecutor());
		register("play", new PlayCommandExecutor((boolean) config.getConfigValue("useGoogleApi"), (String) config.getConfigValue("googleApikey")), "p");
		register("pause", new PauseCommandExecutor());
		register("resume", new ResumeCommandExecutor(), "continue");
		register("queue", new QueueCommandExecutor(), "q");
		register("forceskip", new ForceSkipCommandExecutor(), "fs");
		register("nowplaying", new NowPlayingCommandExecutor(), "np");
		register("clearqueue", new ClearQueueCommandExecutor(), "clear", "c");
		register("remove", new RemoveCommandExecutor(), "delete", "rm", "del");
		register("shuffle", new ShuffleCommandExecutor(), "s");
	}
}