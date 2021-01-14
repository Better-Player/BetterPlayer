package nl.thedutchmc.betterplayer.commands;

public class CommandParameters {

	private long senderId, channelId, guildId;
	private String[] args;
	
	public CommandParameters(long senderId, long channelId, long guildId) {
		this.senderId = senderId;
		this.channelId = channelId;
		this.guildId = guildId;
	}
	
	public long getSenderId() {
		return this.senderId;
	}
	
	public long getChannelId() {
		return this.channelId;
	}
	
	public long getGuildId() {
		return this.guildId;
	}
	
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public String[] getArgs() {
		return this.args;
	}
	
	public boolean hasArgs() {
		return args != null && args.length != 0;
	}
}
