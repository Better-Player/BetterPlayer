package net.betterplayer.betterplayer.commands;

import net.dv8tion.jda.api.entities.Member;

public class CommandParameters {

	private Member senderMember;
	private long senderId, channelId, guildId;
	private String[] args;
	
	public CommandParameters(long senderId, long channelId, long guildId, Member senderMember) {
		this.senderId = senderId;
		this.channelId = channelId;
		this.guildId = guildId;
		this.senderMember = senderMember;
	}
	
	/**
	 * The Member who sent the command
	 * @return Returns the member who sent the command
	 */
	public Member getSenderMember() {
		return this.senderMember;
	}
	
	/**
	 * The long ID of the user who sent the command
	 * @return ID of the user who sent the command
	 */
	public long getSenderId() {
		return this.senderId;
	}
	
	/**
	 * The long ID of the channel in which the command was sent
	 * @return Returns the ID of the channel in which the command was sent
	 */
	public long getChannelId() {
		return this.channelId;
	}
	
	/**
	 * The long ID of the guild in which the command was sent
	 * @return Returns the ID of the guild in which the command was sent
	 */
	public long getGuildId() {
		return this.guildId;
	}
	
	/**
	 * Set arguments for the command fired
	 * @param args The arguments for the command
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	/**
	 * Get the arguments for the provided command<br>
	 * <br>
	 * You should run {@link #hasArgs()} first, because there might not be any arguments!
	 * @return Returns the arguments for the command
	 */
	public String[] getArgs() {
		return this.args;
	}
	
	/**
	 * Returns if the command has any arguments
	 * @return Returns true if the command has arguments, false if it does not
	 */
	public boolean hasArgs() {
		return args != null && args.length != 0;
	}
}
