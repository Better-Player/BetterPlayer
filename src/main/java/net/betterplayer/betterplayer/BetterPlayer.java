package net.betterplayer.betterplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.commands.CommandManager;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.config.guild.GuildConfigManager;
import net.betterplayer.betterplayer.events.EventManager;
import net.betterplayer.betterplayer.utils.Utils;

public class BetterPlayer {
	public static volatile boolean DEBUG = false;
	public static volatile boolean IS_READY = false;
	public static volatile boolean IS_DOCKER = false;
	
	private static BetterPlayer INSTANCE;
	private static final Logger LOGGER = LogManager.getLogger(BetterPlayer.class);
	
	private BetterAudioManager betterAudioManager;
	private JdaHandler jdaHandler;
	private EventManager eventManager;
	private CommandManager commandManager;
	private ConfigManifest config;
	private GuildConfigManager guildConfig;
	public static void main(String[] args) {		
		List<String> argsList = Arrays.asList(args);
		
		//Check if we're running in Docker
		if(System.getenv("IS_DOCKER") != null && System.getenv("IS_DOCKER").equalsIgnoreCase("true")) {
			IS_DOCKER = true;
			
			//Check the DEBUG env variable
			if(System.getenv("DEBUG") != null && System.getenv("DEBUG").equalsIgnoreCase("true")) {
				DEBUG = true;
			}
		}
		
		if(argsList.contains("--debug")) DEBUG = true;
		
		if(DEBUG) {
			LOGGER.setLevel(Level.DEBUG);
		}
		
		//Start up BetterPlayer		
		BetterPlayer betterPlayer = new BetterPlayer();
		betterPlayer.init();
		betterPlayer.setupShutdown();
				
		IS_READY = true;
	}
	
	private void init() {
		INSTANCE = this;

		this.config = ConfigManifest.fromEnv();
		
		//Fetch the Guild configs
		guildConfig = new GuildConfigManager(config);

		//Create all objects required for operation
		jdaHandler = new JdaHandler(this);
		betterAudioManager = new BetterAudioManager(this);
		commandManager = new CommandManager(this, config);
		eventManager = new EventManager(commandManager, this);
		
		//libBetterPlayerBinder.transformDiscordAudioToSpec(new short[] {0, 1, 0, 1});
		
		//Initialize JDA and connect to Discord
		jdaHandler.initJda(this.config.getBotToken());
				
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
					betterAudioManager.leaveAudioChannel(vc, false);
				});
				
				//Shutdown the JDA. This is guaranteed to throw errors, but we do not care for them.
				try {
					jdaHandler.shutdownJda();
				} catch(Exception e) {}
			}
		}, "shutdown-thread"));
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
	 * Get the GuildConfigManager
	 * @return Returns the GuildConfigManager
	 */
	public GuildConfigManager getGuildConfig() {
		return this.guildConfig;
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
		LOGGER.info(log);
	}
	
	/**
	 * Log an Object with log level ERROR
	 * @param log The object to log
	 */
	public static void logError(Object log) {
		LOGGER.error(log);
	}
	
	/**
	 * Log an Object with log level DEBUG<br>
	 * <br>
	 * Will only log if DEBUG is true.
	 * @param log The object to log
	 */
	public static void logDebug(Object log) {
		if(!DEBUG) return;
		
		LOGGER.debug(log);
	}
	
	/**
	 * Get the BetterPlayer instance
	 * @return Returns the BetterPlayer instance. Null if isReady is false.
	 */
	public static BetterPlayer getBetterPlayer() {
		if(IS_READY) {
			return INSTANCE;
		} else {
			return null;
		}
	}
}
