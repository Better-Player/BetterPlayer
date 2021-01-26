package net.betterplayer.betterplayer.events.listeners;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildLeaveEventHandler extends ListenerAdapter {

	private BetterPlayer betterPlayer;
	
	public GuildLeaveEventHandler(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		betterPlayer.getGuildConfig().removeGuildFromDb(event.getGuild().getIdLong());
	}
}