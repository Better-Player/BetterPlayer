package net.betterplayer.betterplayer.config.guild;

import dev.array21.jdbd.datatypes.SqlRow;

public class GuildConfigManifest {	
	
	private String commandPrefix;
	
	private GuildConfigManifest(String commandPrefix) {
		this.commandPrefix = commandPrefix;
	}
	
	public void setCommandPrefix(String commandPrefix) {
		this.commandPrefix = commandPrefix;
	}
	
	public String getCommandPrefix() {
		return this.commandPrefix;
	}
	
	public static GuildConfigManifest fromRow(SqlRow row) {
		String commandPrefix = row.getString("commandprefix");
		return new GuildConfigManifest(commandPrefix);
	}
	
	public static GuildConfigManifest defaultManifest() {
		return new GuildConfigManifest("$");
	}
}
