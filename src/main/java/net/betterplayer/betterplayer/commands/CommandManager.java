package net.betterplayer.betterplayer.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.commands.defaultcommands.*;
import net.betterplayer.betterplayer.config.BotConfig;

public class CommandManager {

	private BetterPlayer betterPlayer;
	private HashMap<String, CommandExecutor> executors = new HashMap<>();
	private List<CommandDetails> commandDetails = new ArrayList<>();
	private BotConfig config;
	
	/**
	 * Initialize CommandManager. This should be done only once.
	 * @param betterPlayer BetterPlayer instance
	 * @param config Config instance
	 */
	public CommandManager(BetterPlayer betterPlayer, BotConfig config) {
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
	public void register(String name, CommandExecutor executor, String description, String... aliases) {
		executors.put(name, executor);
		
		for(String alias : aliases) {
			executors.put(alias, executor);
		}
		
		commandDetails.add(new CommandDetails(name, aliases, description));
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
	 * Get a List of CommandDetails of all registered commands
	 * @return Returns a List of CommandDetails
	 */
	public List<CommandDetails> getCommandDetails() {
		return this.commandDetails;
	}
	
	/**
	 * This function will set up the default CommandExecutors shipped with BetterPlayer
	 */
	private void setupDefault() {
		register("join", new JoinCommandExecutor(), "Join a voice channel");
		register("help", new HelpCommandExecutor(), "Displays the help menu");
		register("leave", new LeaveCommandExecutor(), "Leave a voice channel");
		register("play", new PlayCommandExecutor((boolean) config.getConfigValue("useGoogleApi"), (String) config.getConfigValue("googleApikey")), "Play a YouTube video, playlist, or search for a video", "p");
		register("pause", new PauseCommandExecutor(), "Pause BetterPlayer");
		register("resume", new ResumeCommandExecutor(), "Resume BetterPlayer", "continue");
		register("queue", new QueueCommandExecutor(), "Display the current queue", "q");
		register("forceskip", new ForceSkipCommandExecutor(), "Force skip a track", "fs");
		register("nowplaying", new NowPlayingCommandExecutor(), "Display details about the track that is playing at the moment", "np");
		register("clearqueue", new ClearQueueCommandExecutor(), "Clear the queue", "clear", "c");
		register("remove", new RemoveCommandExecutor(), "Delete an item from the queue, by index shown by $queue", "delete", "rm", "del");
		register("shuffle", new ShuffleCommandExecutor(), "Shuffle the queue", "s");
		register("config", new ConfigCommandExecutor(), "Configure BetterPlayer", "option", "options");
		register("move", new MoveCommandExecutor(), "Move a track to a position in queue, or to first place", "mv");
		register("activate", new ActivateCommandExecutor(), "Activate BetterPlayer with your licence key.");
	}
}