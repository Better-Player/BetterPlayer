package nl.thedutchmc.betterplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import nl.thedutchmc.betterplayer.audio.BetterAudioManager;
import nl.thedutchmc.betterplayer.commands.CommandManager;
import nl.thedutchmc.betterplayer.config.Config;
import nl.thedutchmc.betterplayer.events.EventManager;

public class BetterPlayer {

	private BetterAudioManager betterAudioManager;
	private JdaHandler jdaHandler;
	private EventManager eventManager;
	private CommandManager commandManager;
	private Config config;
	
	private static boolean DEBUG = false;
	private static boolean isReady = false;
	private static BetterPlayer INSTANCE;
	
	public static void main(String[] args) {		
		List<String> argsList = Arrays.asList(args);
		
		if(argsList.contains("--debug")) DEBUG = true;
		
		
		BetterPlayer betterPlayer = new BetterPlayer();
		betterPlayer.init();
		betterPlayer.setupShutdown();
		
		isReady = true;
	}
	
	private void init() {
		INSTANCE = this;

		config = new Config(this);
		config.read();
		
		jdaHandler = new JdaHandler(this);	
		betterAudioManager = new BetterAudioManager(jdaHandler);
		
		commandManager = new CommandManager(this);
		
		eventManager = new EventManager((String) config.getConfigValue("commandPrefix"), commandManager);

		jdaHandler.initJda((String) config.getConfigValue("botToken"));
	}
	
	private void setupShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				betterAudioManager.getConnectedVoiceChannels().forEach(vc -> {
					betterAudioManager.leaveAudioChannel(vc);
				});
				
				try {
					jdaHandler.shutdownJda();
				} catch(Exception e) {}
			}
		}, "shutdown-thread"));
	}
	
	public BetterAudioManager getBetterAudioManager() {
		return this.betterAudioManager;
	}
	
	public JdaHandler getJdaHandler() {
		return this.jdaHandler;
	}
	
	public EventManager getEventManager() {
		return this.eventManager;
	}
	
	public CommandManager getCommandManager() {
		return this.commandManager;
	}
	
	public void saveResource(String name, String targetPath) {
		InputStream in = null;
		
		try {
			//ClassLoader cl = this.getClass().getClassLoader();
			in = this.getClass().getResourceAsStream("/" + name);
			
			if(name == null) {
				throw new FileNotFoundException("Cannot find file " + name + " in jar!");
			}
			
			if(in == null) {
				throw new FileNotFoundException("Cannot find file " + name + " in jar!");
			}
			
			logDebug(in);

			Path exportPath = Paths.get(targetPath + File.separator + name);
			Files.copy(in, exportPath);
		} catch (FileNotFoundException e) {
			logError("A FileNotFoundException was thrown whilst trying to save " + name + ". Use --debug for more details.");
			logDebug(Utils.getStackTrace(e));
		} catch (IOException e) {
			logError("An IOException was thrown whilst trying to save " + name + ". Use --debug for more details.");
			logDebug(Utils.getStackTrace(e));
		}
	}
	
	public static void logInfo(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][INFO] " + log);
	}
	
	public static void logError(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.err.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][ERROR] " + log);
	}
	
	public static void logDebug(Object log) {
		if(!DEBUG) return;
		
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][DEBUG] " + log);
	}
	
	public static BetterPlayer getBetterPlayer() {
		if(isReady) {
			return INSTANCE;
		} else {
			return null;
		}
	}
}
