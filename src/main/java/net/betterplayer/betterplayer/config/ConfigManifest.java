package net.betterplayer.betterplayer.config;

import java.lang.reflect.Field;

import com.google.common.base.CaseFormat;

import net.betterplayer.betterplayer.annotations.Nullable;

public class ConfigManifest {
	private String botToken;
	private String useGoogleApiForSearch;
	private String googleApiKey;
	private String ksoftApiToken;
	private String dbHost;
	private String dbDatabase;
	private String dbUsername;
	private String dbPassword;
	
	private ConfigManifest() {}
	
	@Nullable
	public String getDbHost() {
		return this.dbHost;
	}
	
	@Nullable
	public String getDbDatabase() {
		return this.dbDatabase;
	}
	
	@Nullable
	public String getDbUsername() {
		return this.dbUsername;
	}
	
	@Nullable
	public String getDbPassword() {
		return this.dbPassword;
	}
  	
	@Nullable
	public String getBotToken() {
		return this.botToken;
	}
	
	@Nullable
	public Boolean isUseGoogleApiForSearch() {
		Boolean boolValue = Boolean.valueOf(this.useGoogleApiForSearch.toLowerCase());
		return boolValue;
	}
	
	@Nullable
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
}
