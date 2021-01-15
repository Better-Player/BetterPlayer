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
	
	public CommandManager(BetterPlayer betterPlayer, Config config) {
		this.betterPlayer = betterPlayer;
		this.config = config;
		
		setupDefault();
	}
	
	public void register(String name, CommandExecutor executor, String... aliases) {
		executors.put(name, executor);
		
		for(String alias : aliases) {
			executors.put(alias, executor);
		}
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
		register("play", new PlayCommandExecutor((boolean) config.getConfigValue("useGoogleApi"), (String) config.getConfigValue("googleApikey")), "p");
		register("pause", new PauseCommandExecutor());
		register("resume", new ResumeCommandExecutor(), "continue");
		register("queue", new QueueCommandExecutor(), "q");
		register("forceskip", new ForceSkipCommandExecutor(), "fs");
		register("nowplaying", new NowPlayingCommandExecutor(), "np");
		register("clearqueue", new ClearQueueCommandExecutor(), "clear", "c");
		register("remove", new RemoveCommandExecutor(), "delete", "rm", "del");
	}
}