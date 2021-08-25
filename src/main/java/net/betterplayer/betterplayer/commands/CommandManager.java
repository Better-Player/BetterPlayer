package net.betterplayer.betterplayer.commands;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.google.common.reflect.ClassPath;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;

public class CommandManager {

	private BetterPlayer betterPlayer;
	private HashMap<String, CommandExecutor> executors = new HashMap<>();
	private List<CommandDetails> commandDetails = new ArrayList<>();
	private ConfigManifest config;
	
	/**
	 * Initialize CommandManager. This should be done only once.
	 * @param betterPlayer BetterPlayer instance
	 * @param config Config instance
	 */
	public CommandManager(BetterPlayer betterPlayer, ConfigManifest config) {
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
		BetterPlayer.logInfo("Loading commands...");
		try {
			registerAnnotatedCommands();
		} catch(IOException e) {
			BetterPlayer.logError("Unable to load BetterPlayer commands!");
			BetterPlayer.logInfo(Utils.getStackTrace(e));
		}
		
		BetterPlayer.logInfo(String.format("Successfully loaded %d commands.", this.commandDetails.size()));
	}
	
	private void registerAnnotatedCommands() throws IOException {
		//Get the path to the JAR we're running from.
		
		ClassPath.from(this.getClass().getClassLoader()).getTopLevelClasses("net.betterplayer.betterplayer.commands.defaultcommands").forEach(c -> {
			Class<?> clazz;
			try {
				clazz = Class.forName(c.getName());
			} catch (ClassNotFoundException e) {
				// Not possible
				e.printStackTrace();
				System.exit(1);
				return;
			}
			
			if(clazz.isAnnotationPresent(BotCommand.class)) {
				//Get the Class' constructor
				Constructor<?> constructor;
				try {
					constructor = clazz.getConstructor(ConfigManifest.class);
				} catch (NoSuchMethodException | SecurityException e) {
					BetterPlayer.logError(String.format("Class annotated with @BotCommand does not have Constructor taking BotConfig as only argument!", clazz.getName()));
					BetterPlayer.logDebug(Utils.getStackTrace(e));
					return;
				}
				
				//Create an instance of the Class
				Object executorObject;
				try {
					executorObject = constructor.newInstance(this.config);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					BetterPlayer.logError("Unable to create instance of class " + clazz.getName());
					BetterPlayer.logDebug(Utils.getStackTrace(e));
					return;
				}

				//Check if the class implements CommandExecutor
				List<Class<?>> annotations = Arrays.asList(clazz.getInterfaces());
				if(!annotations.contains(CommandExecutor.class)) {
					BetterPlayer.logError(String.format("Class '%s' annotated with @BotCommand does not implement CommandExecutor!", clazz.getName()));
					return;
				}
				
				//Cast the object to CommandExecutor
				CommandExecutor executorImpl = (CommandExecutor) executorObject;
				
				//Get the annotation details
				BotCommand botCommandAnnotation = clazz.getAnnotation(BotCommand.class);
				
				//Finally, register the class
				register(botCommandAnnotation.name(), executorImpl, botCommandAnnotation.description(), botCommandAnnotation.aliases());
				
				BetterPlayer.logDebug(String.format("Loaded BotCommand: '%s' (%s)", botCommandAnnotation.name(), clazz.getName()));
			}
			
		});
		
		/*
		File jarPath;
		try {
			jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			BetterPlayer.logError("Unable to determine the path to the JAR which is currently used.");
			BetterPlayer.logDebug(Utils.getStackTrace(e));

			System.exit(1);
			return;
		}
		
		JarFile jarFile = new JarFile(jarPath);
		
		//Get all files ending in class
		// - Replace .class with nothing
		// - Replace '/' with '.'
		//
		// So e.g net/betterplayer/betterplayer/commands/defaultcommands/PlayCommandExecutor.class
		// becomes: net.betterplayer.betterplayer.commands.defaultcommands.PlayCommandExecutor
		List<String> classNamesInJar = jarFile.stream()
				.map(ZipEntry::getName)
				.filter(name -> name.endsWith(".class"))
				.map(name -> name
						.replace(".class", "")
						.replace('/', '.'))
				.distinct()
				.collect(Collectors.toList());
		
		jarFile.close();
				
		for(String className : classNamesInJar) {
			
			//We only want to load classes in within our domain
			//Since we wont encounter any BotCommands outside of it
			if(!className.startsWith("net.betterplayer")) {
				continue;
			}
			
			//Check if the Class is annotated with the @BotCommand annotation
			Pair<Boolean, Class<?>> annotatedChecked;
			try {
				annotatedChecked = isAnnotatedWith(className, BotCommand.class);
			} catch(ClassNotFoundException e) {
				BetterPlayer.logError(String.format("Unable to determine if '%s' is annotated with the @BotCommand annotation", className));
				BetterPlayer.logDebug(Utils.getStackTrace(e));

				continue;
			}
			
			if(annotatedChecked.getFirst()) {
				//Class is annotated with the @BotCommand annotation
				

			}
		}*/
	}
	
	/**
	 * Check if a Class is annotated with an annotation
	 * @param className The name of the Class
	 * @param annotation The annotation Class
	 * @return Returns a Pair, where A is a Boolean indicating whether the Class is annotated with the provided annotation. B is a Class<?> object for the className
	 * @throws ClassNotFoundException Thrown when the provided className does not exist
	 */
	/*
	private Pair<Boolean, Class<?>>isAnnotatedWith(String className, Class<? extends Annotation> annotation) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(className, false, this.getClass().getClassLoader());
		return new Pair<Boolean, Class<?>>(clazz.isAnnotationPresent(annotation), clazz);
	}
	*/
}