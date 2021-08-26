package net.betterplayer.betterplayer.config;

import java.lang.reflect.Field;

import com.google.common.base.CaseFormat;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.Nullable;
import net.betterplayer.betterplayer.annotations.Required;

public class ConfigManifest {
	
	@Required
	private String botToken;
	private String useGoogleApiForSearch;
	@Required
	private String googleApiKey;
	private String ksoftApiToken;
	@Required
	private String dbHost;
	@Required
	private String dbDatabase;
	@Required
	private String dbUsername;
	@Required
	private String dbPassword;
	
	private ConfigManifest() {}
	
	public String getDbHost() {
		return this.dbHost;
	}
	
	public String getDbDatabase() {
		return this.dbDatabase;
	}
	
	public String getDbUsername() {
		return this.dbUsername;
	}
	
	public String getDbPassword() {
		return this.dbPassword;
	}
  	
	public String getBotToken() {
		return this.botToken;
	}
	
	@Nullable
	public Boolean isUseGoogleApiForSearch() {
		if(this.useGoogleApiForSearch == null) {
			return true;
		}
		
		Boolean boolValue = Boolean.valueOf(this.useGoogleApiForSearch.toLowerCase());
		return boolValue;
	}
	
	public String getGoogleApiKey() {
		return this.googleApiKey;
	}
	
	@Nullable
	public String getKsoftApiToken() {
		return this.ksoftApiToken;
	}
	
	/**
	 * Create a ConfigManifest from Environmental variables
	 * @return An instance of ConfigManifest
	 */
	public static ConfigManifest fromEnv() {
		// We do this reflectively so we don't have to continuesly updated this method
		ConfigManifest manifest = new ConfigManifest();
		
		Field[] fields = ConfigManifest.class.getDeclaredFields();
		for(Field f : fields) {
			String fieldNameCamelCase = f.getName();
			String snakeCaseName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldNameCamelCase);
			String envValue = System.getenv(snakeCaseName);

			try {
				f.set(manifest, envValue);
			} catch (Exception e) {}
		}
		
		return manifest;
	}
	
	public boolean verifyPrint() {
		boolean passed = true;
		
		for(Field f : ConfigManifest.class.getDeclaredFields()) {
			if(!f.isAnnotationPresent(Required.class)) continue;
			
			try {
				Object fV = f.get(this);
				if(fV == null || (fV.getClass().equals(String.class) && ((String) fV).isEmpty())) {
					BetterPlayer.logError(String.format("Configuration option '%s' is not allowed to be left empty. You can configure this field with the environmental variable '%s'", 
							f.getName(), 
							CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, f.getName())));
					passed = false;
				}
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		if(!passed) {
			BetterPlayer.logError("BetterPlayer is not configured properly. Exiting.");
			System.exit(1);
			return false;
		} else {
			BetterPlayer.logInfo("Configuration is valid.");
		}
		
		return true;
	}
}
