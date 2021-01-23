package nl.thedutchmc.betterplayer.config.guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.config.BotConfig;
import nl.thedutchmc.betterplayer.config.guild.database.SqlManager;

public class GuildConfigManager {
	
	//TODO JSON config
	
	private SqlManager sqlManager;
	private HashMap<Long, HashMap<String, Object>> guildConfigs = new HashMap<>();
	
	private final boolean useDatabase;
	private final String dbHost, dbName, dbUsername, dbPassword, configPath;
	
	public GuildConfigManager(BotConfig botConfig) {
		
		useDatabase = (boolean) botConfig.getConfigValue("useDatabase");
		
		if(useDatabase) {
			dbHost = (String) botConfig.getConfigValue("dbHost");
			dbName = (String) botConfig.getConfigValue("dbName");
			dbUsername = (String) botConfig.getConfigValue("dbUsername");
			dbPassword = (String) botConfig.getConfigValue("dbPassword");
			
			configPath = "";
			
			sqlManager = new SqlManager(dbHost, dbName, dbUsername, dbPassword);
			
			checkDb();
			guildConfigs = readGuildConfigsTable();
		} else {
			dbHost = dbName = dbUsername = dbPassword = "";
			
			configPath = (String) botConfig.getConfigValue("guildConfigPath");
		}
		
	}
	
	/**
	 * Check if all required tables in the database are present (if it's initialized or not)
	 * If not, create them
	 */
	private void checkDb() {
		String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS";
		 
		List<String> requiredTableNames = new ArrayList<>(Arrays.asList(
			"guildConfigs"
		));
		
		try {
			PreparedStatement pr = sqlManager.createPreparedStatement(sql);
			ResultSet rs = sqlManager.executeFetchQuery(pr);
			
			List<String> allTableNames = new ArrayList<>();
			while(rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				allTableNames.add(tableName);
			}
			
			//There are some tables, but not all. The sysadmin must wipe the DB to proceed
			if(!allTableNames.isEmpty() & !allTableNames.containsAll(requiredTableNames)) {
				BetterPlayer.logError("Your database is not complete. Please drop it and recreate it!");
				return;
			}
			
			//The DB is empty, meaning it's unitinialized, so do that
			if(allTableNames.isEmpty()) {
				initDb();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			//TODO error handling
		}	
	}
	
	/**
	 * Initialize the database with the default required tables for BetterPlayer
	 * @param existingTables W
	 */
	private void initDb() {
		/*
		 * Table: guildConfigs
		 * - guildId 					Big Integer, primary key
		 * - commandPrefix				Varchar, length 1
		 * - useDeepSpeech				boolean
		 */
		String sql = "CREATE TABLE `" + this.dbName + "`.`guildConfigs` ( `guildId` BIGINT NOT NULL , `commandPrefix` VARCHAR(1) NOT NULL , `useDeepSpeech` BOOLEAN NOT NULL , PRIMARY KEY (`guildId`)) ENGINE = InnoDB;";
		try {
			PreparedStatement pr = sqlManager.createPreparedStatement(sql);
			sqlManager.executePutQuery(pr);
		} catch(SQLException e) {
			e.printStackTrace();
			//TODO error handling
		}
	}
	
	/**
	 * Read the guildConfigs table into a HashMap.<br>
	 * Key of the outer hashmap is the field guildId in the table<br>
	 * Value is a HashMap of all other fields in the table for that guildId<br>
	 */
	private HashMap<Long, HashMap<String, Object>> readGuildConfigsTable() {
		HashMap<Long, HashMap<String, Object>> result = new HashMap<>();
		
		String sql = "SELECT * FROM guildConfigs";
		try {
			PreparedStatement pr = sqlManager.createPreparedStatement(sql);
			ResultSet rs = sqlManager.executeFetchQuery(pr);
			ResultSetMetaData md = rs.getMetaData();
			int columnCount = md.getColumnCount();
			
			while(rs.next()) {
				HashMap<String, Object> guildConfig = new HashMap<>();
				long guildId = rs.getLong("guildId");
				
				//SQL Stuff is not 0-based, its 1-based,
				//we start at 2 because we don't want to read the guildId again
				for(int i = 2; i <= columnCount; i++) {
					guildConfig.put(md.getColumnName(i), rs.getObject(i));
				}
				
				result.put(guildId, guildConfig);
			}
		} catch(SQLException e) {
			e.printStackTrace();
			//TODO error handling
		}
		
		return result;
	}
	
	/**
	 * Get a config value for a guild
	 * @param guildId The ID of the guild
	 * @param key The key of the value to fetch
	 * @return Returns the found value, null if the config didn't exist or if the key did not have a value
	 */
	public Object getConfigValue(long guildId, String key) {
		//Check if a config exists for the guild
		if(!guildConfigs.containsKey(guildId))
			return null;
		
		//Check if the config for the guild contains the option requested
		if(!guildConfigs.get(guildId).containsKey(key))
			return null;
		
		//Return the requested option
		return guildConfigs.get(guildId).get(key);
	}
	
	/**
	 * Set a config value for a guild
	 * @param guildId 
	 * @param key
	 * @param value
	 */
	public void setConfigValue(long guildId, String key, Object value) {
		HashMap<String, Object> config;
		boolean exists;
		
		//Check if the guildConfigs map contains the config for this guild, if so get it<br>
		//if not create a new hashmap and use that, and add an initial entry for the guild
		if(guildConfigs.containsKey(guildId)) {
			config = guildConfigs.get(guildId);
			exists = true;
		} else {
			config = new HashMap<>();
			exists = false;
		}
		
		//Insert the key-value pair into the config, and put the config into the guildConfigs map
		config.put(key, value);
		guildConfigs.put(guildId, config);
		
		//If no config existed yet, that means the DB doesn't have an entry yet for this guild
		//Create one
		//Only do so if we use a database
		if(!exists && useDatabase) {
			initializeDbForGuild(guildId);
			//TODO create initial JSON config
		}
		
		//Update the value in the database, if it is enabled
		if(useDatabase) updateDbForGuild(guildId, key, value);
		//TODO write to json config
	}
	
	/**
	 * Create a default value row for a guild
	 * @param guildId The ID of the guild to initialize
	 */
	public void initializeDbForGuild(long guildId) {
		String sql = "INSERT INTO `guildConfigs` (`guildId`, `commandPrefix`, `useDeepSpeech`) VALUES ('?', '$', '0')";
		try {
			PreparedStatement pr = sqlManager.createPreparedStatement(sql);
			pr.setLong(1, guildId);
			
			sqlManager.executePutQuery(pr);
		} catch(SQLException e) {
			e.printStackTrace();
			//TODO error handling
		}
	}
	
	/**
	 * Update a field in the database
	 * @param guildId The ID of the guild to update
	 * @param key The field name to update
	 * @param value The value to set
	 */
	private void updateDbForGuild(long guildId, String key, Object value) {
		String sql = "UPDATE `guildConfigs` SET `?` = '?' WHERE `guildId` = ?";
		try {
			PreparedStatement pr = sqlManager.createPreparedStatement(sql);
			pr.setString(1, key);
			pr.setObject(2, value);
			pr.setLong(3, guildId);
			
			sqlManager.executePutQuery(pr);
		} catch(SQLException e) {
			e.printStackTrace();
			//TODO error handling
		}
	}
}