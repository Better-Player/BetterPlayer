package net.betterplayer.betterplayer.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.utils.Utils;

public class BotConfig {

	private BetterPlayer betterPlayer;
	private File configDirectory;
	private HashMap<String, Object> configData = new HashMap<>();
	
	public BotConfig(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
		
		if(!BetterPlayer.IS_DOCKER) {
			//Get the path of the JAR, and thus the directory in which we should put the config file
			try {
				final File jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
				configDirectory = new File(jarPath.getParentFile().getPath());			
			} catch (URISyntaxException e) {
				
			}
		}
	}
		
	public void read() {
		BetterPlayer.logInfo("Reading configuration...");
		
		if(BetterPlayer.IS_DOCKER) {
			this.configData.put("botToken", 	System.getenv("BOT_TOKEN"));
			this.configData.put("useGoogleApi", Boolean.valueOf(System.getenv("USE_GOOGLE_API")));
			this.configData.put("googleApikey",	System.getenv("GOOGLE_API_KEY"));
			this.configData.put("dbHost", 		System.getenv("DB_HOST"));
			this.configData.put("dbName", 		System.getenv("DB_NAME"));
			this.configData.put("dbUsername", 	System.getenv("DB_USERNAME"));
			this.configData.put("dbPassword", 	System.getenv("DB_PASSWORD"));
		} else {
			Yaml yaml = new Yaml();
			File configFile = new File(configDirectory, "config.yml");
			
			//Check if the config file does not exist
			//If it doesnt, save it.
			if(!configFile.exists()) {
				betterPlayer.saveResource("config.yml", configDirectory.getAbsolutePath());
			}
			
			//Open a FileInputStream for the file. If a FNFE is thrown, inform the syadmin and exit.
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(configFile);
			} catch (FileNotFoundException e) {
				BetterPlayer.logError("Unable to read config file: FileNotFoundException. Run with --debug for more info");
				BetterPlayer.logDebug(Utils.getStackTrace(e));
				
				System.exit(1);
			}
			
			//Load the config into a HashMap
			configData = yaml.load(fis);
		}
		
		BetterPlayer.logInfo("Configuration loaded.");
	}
	
	/**
	 * Get a configuration option
	 * @param name The name of the option to get
	 * @return Returns the Object associated with the provided name. Null if no value is accotiated with the provided name
	 */
	public Object getConfigValue(String name) {
		return this.configData.get(name);
	}
}
