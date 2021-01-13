package nl.thedutchmc.betterplayer.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.Utils;

public class Config {

	private BetterPlayer betterPlayer;
	private File configDirectory;
	private HashMap<String, Object> configData = null;
	
	public Config(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
		
		try {
			final File jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			configDirectory = new File(jarPath.getParentFile().getPath());			
		} catch (URISyntaxException e) {
			
		}
	}
		
	public void read() {
	
		Yaml yaml = new Yaml();
		
		File configFile = new File(configDirectory, "config.yml");
		
		if(!configFile.exists()) {
			betterPlayer.saveResource("config.yml", configDirectory.getAbsolutePath());
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			BetterPlayer.logError("Unable to read config file: FileNotFoundException. Run with --debug for more info");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			
			System.exit(1);
		}
		
		configData = yaml.load(fis);
	}
	
	public Object getConfigValue(String name) {
		return this.configData.get(name);
	}
}
