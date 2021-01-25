package nl.thedutchmc.betterplayer.events.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.thedutchmc.betterplayer.BetterPlayer;

public class GuildJoinEventHandler extends ListenerAdapter {

	private BetterPlayer betterPlayer;
	
	public GuildJoinEventHandler(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		betterPlayer.getGuildConfig().initializeDbForGuild(event.getGuild().getIdLong());
	}		
}
