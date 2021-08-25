package net.betterplayer.betterplayer.events.listeners;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinEventHandler extends ListenerAdapter {

	private BetterPlayer betterPlayer;
	
	public GuildJoinEventHandler(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		betterPlayer.getGuildConfig().getDefaultManifest(event.getGuild().getIdLong());
	}		
}
