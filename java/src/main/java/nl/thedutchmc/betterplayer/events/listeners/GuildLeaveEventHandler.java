package nl.thedutchmc.betterplayer.events.listeners;

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.thedutchmc.betterplayer.BetterPlayer;

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