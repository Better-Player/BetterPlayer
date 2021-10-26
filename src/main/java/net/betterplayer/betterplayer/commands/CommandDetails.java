package net.betterplayer.betterplayer.commands;

public class CommandDetails {

	private final String name, description;
	private final String[] aliases;
	
	public CommandDetails(String name, String[] aliases, String description) {
		this.name = name;
		this.aliases = aliases;
		this.description = description;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public String getDescription() {
		return this.description;
	}
}
