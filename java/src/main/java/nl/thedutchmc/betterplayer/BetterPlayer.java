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
		
		/*DeepSpeechNativeInterface dsni = new DeepSpeechNativeInterface();
		dsni.loadNative();
		
		String audio = "/mnt/a/LDC93S1.wav";
		
		try {
			RandomAccessFile raf = new RandomAccessFile(audio, "r");
			
			raf.seek(40);
			int bufferSize = DeepSpeechUtils.readLEInt(raf);
			
			raf.seek(44);
			byte[] bytes = new byte[bufferSize];
			raf.readFully(bytes);
			
			dsni.callNativeMethod(bytes);
			
		} catch(Exception e) {
			e.printStackTrace();
		}*/
		
		//Start up BetterPlayer		
		BetterPlayer betterPlayer = new BetterPlayer();
		betterPlayer.init();
		betterPlayer.setupShutdown();
		
		isReady = true;
	}
	
	private void init() {
		INSTANCE = this;

		//Read the config
		config = new Config(this);
		config.read();
		
		//Create all objects required for operation
		jdaHandler = new JdaHandler(this);
		betterAudioManager = new BetterAudioManager(jdaHandler);
		commandManager = new CommandManager(this, config);
		eventManager = new EventManager((String) config.getConfigValue("commandPrefix"), commandManager);

		//Initialize JDA and connect to Discord
		jdaHandler.initJda((String) config.getConfigValue("botToken"));
				
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	private void setupShutdown() {
		
		//Before our application is terminated by the JVM we want to try to properly stop all we need to
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				
				//Disconnect from all connected voice channels
				betterAudioManager.getConnectedVoiceChannels().forEach(vc -> {
					betterAudioManager.leaveAudioChannel(vc);
				});
				
				//Shutdown the JDA. This is guaranteed to throw errors, but we do not care for them.
				try {
					jdaHandler.shutdownJda();
				} catch(Exception e) {}
			}
		}, "shutdown-thread"));
	}
	
	/**
	 * Get the DEBUG flag. True if debug is enabled, false if it is not
	 * @return Returns the debug status
	 */
	public static boolean isDebug() {
		return DEBUG;
	}
	
	/**
	 * Get the BetterAudioManager
	 * @return Returns the BetterAudioManager
	 */
	public BetterAudioManager getBetterAudioManager() {
		return this.betterAudioManager;
	}
	
	/**
	 * Get the JdaHandler
	 * @return Returns the JdaHandler
	 */
	public JdaHandler getJdaHandler() {
		return this.jdaHandler;
	}
	
	/**
	 * Get the EventManager
	 * @return Returns the EventManager
	 */
	public EventManager getEventManager() {
		return this.eventManager;
	}
	
	/**
	 * Get the CommandManager
	 * @return Returns the CommandManager
	 */
	public CommandManager getCommandManager() {
		return this.commandManager;
	}
	
	/**
	 * Get a resource from within the jar. If the resource is in the root of the jar, do not provide a path to it, just the name.<br>
	 * <br>
	 * Throws a FileNotFoundException if the targeted resource was not found.
	 * @param name The name of the resource
	 * @param targetPath The path where the resource should be saved
	 */
	public void saveResource(String name, String targetPath) {
		InputStream in = null;
		
		try {
			in = this.getClass().getResourceAsStream("/" + name);
			
			if(name == null) {
				throw new FileNotFoundException("Cannot find file " + name + " in jar!");
			}
			
			if(in == null) {
				throw new FileNotFoundException("Cannot find file " + name + " in jar!");
			}
			
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
	
	/**
	 * Log an Object with log level INFO
	 * @param log The object to log
	 */
	public static void logInfo(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][INFO] " + log);
	}
	
	/**
	 * Log an Object with log level ERROR
	 * @param log The object to log
	 */
	public static void logError(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.err.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][ERROR] " + log);
	}
	
	/**
	 * Log an Object with log level DEBUG<br>
	 * <br>
	 * Will only log if DEBUG is true.
	 * @param log The object to log
	 */
	public static void logDebug(Object log) {
		if(!DEBUG) return;
		
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][DEBUG] " + log);
	}
	
	/**
	 * Get the BetterPlayer instance
	 * @return Returns the BetterPlayer instance. Null if isReady is false.
	 */
	public static BetterPlayer getBetterPlayer() {
		if(isReady) {
			return INSTANCE;
		} else {
			return null;
		}
	}
}
