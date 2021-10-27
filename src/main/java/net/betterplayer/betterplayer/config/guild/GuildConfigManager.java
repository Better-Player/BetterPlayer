package net.betterplayer.betterplayer.config.guild;

import dev.array21.jdbd.DatabaseDriver;
import dev.array21.jdbd.datatypes.PreparedStatement;
import dev.array21.jdbd.datatypes.SqlRow;
import dev.array21.jdbd.exceptions.SqlException;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.Nullable;

import java.util.HashMap;

public class GuildConfigManager {

	private final DatabaseDriver driver;

	public GuildConfigManager(DatabaseDriver driver) {
		this.driver = driver;
	}
	
	public DatabaseDriver getDatabaseDriver() {
		return this.driver;
	}
	
	/**
	 * Read the guildConfigs table into a HashMap.<br>
	 * Key of the outer hashmap is the field guildId in the table<br>
	 * Value is a HashMap of all other fields in the table for that guildId<br>
	 */
	private HashMap<Long, GuildConfigManifest> getGuildConfigs() {
		HashMap<Long, GuildConfigManifest> configs = new HashMap<>();
		SqlRow[] rs;
		try {
			rs = this.driver.query(new PreparedStatement("SELECT guildid,commandprefix FROM guildConfigs"));
		} catch(SqlException e) {
			e.printStackTrace();
			return null;
		}
		
		for(SqlRow row : rs) {
			long guildId = row.getLong("guildid");
			GuildConfigManifest manifest = GuildConfigManifest.fromRow(row);
			
			configs.put(guildId, manifest);
		}
		
		return configs;
	}
	
	/**
	 * Get a config value for a guild
	 * @param guildId The ID of the guild
	 * @return Returns the Manifest for the provided guild, or null if it does not exist
	 */
	@Nullable
	public GuildConfigManifest getManifest(long guildId) {
		HashMap<Long, GuildConfigManifest> configs = this.getGuildConfigs();
		return configs.get(guildId);
	}
	
	/**
	 * Set a config value for a guild
	 * @param guildId 
	 * @param manifest
	 */
	public void setManifest(long guildId, GuildConfigManifest manifest) {
		HashMap<Long, GuildConfigManifest> configs = this.getGuildConfigs();
		if(configs.containsKey(guildId)) {
			PreparedStatement pr = new PreparedStatement("UPDATE guildConfigs SET commandprefix = '?' WHERE guildid = '?'");
			pr.bind(0, manifest.getCommandPrefix());
			pr.bind(1, guildId);
			
			try {
				this.driver.execute(pr);
			} catch(SqlException e) {
				e.printStackTrace();
			}
		} else {
			PreparedStatement pr = new PreparedStatement("INSERT INTO guildConfigs (guildid, commandprefix) VALUES ('?', '?')");
			pr.bind(0, guildId);
			pr.bind(1, manifest.getCommandPrefix());
			
			try {
				this.driver.execute(pr);
			} catch(SqlException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create a default manifest for a Guild and insert it into the database
	 * @param guildId The ID of the guild to initialize
	 */
	public GuildConfigManifest getDefaultManifest(long guildId) {
		GuildConfigManifest manifest = GuildConfigManifest.defaultManifest();
		
		PreparedStatement pr = new PreparedStatement("INSERT INTO guildConfigs (guildid, commandprefix) VALUES ('?', '?')");
		pr.bind(0, guildId);
		pr.bind(1, manifest.getCommandPrefix());
		
		try {
			this.driver.execute(pr);
		} catch(SqlException e) {
			e.printStackTrace();
		}
		
		return manifest;
	}
	
	/**
	 * Remove all values associated with this guild from the database
	 * @param guildId The ID of the guild to remove
	 */
	public void removeGuild(long guildId) {
		PreparedStatement pr = new PreparedStatement("DELETE FROM guildConfigs WHERE guildid = '?'");
		pr.bind(0, guildId);
		
		try {
			this.driver.execute(pr);
		} catch(SqlException e) {
			e.printStackTrace();
		}
	}
}