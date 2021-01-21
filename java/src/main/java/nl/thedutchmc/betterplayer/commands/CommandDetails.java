package nl.thedutchmc.betterplayer.commands;

public class CommandDetails {

	private String name, description;
	private String[] aliases;
	
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
