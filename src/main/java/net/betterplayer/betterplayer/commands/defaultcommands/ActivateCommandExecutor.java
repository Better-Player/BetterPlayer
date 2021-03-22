package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command gives the user a way to activate their Guild for premium BetterPlayer features<br>
 * Users need the 'MANAGER_SERVER' Permission to use this
 */
@BotCommand(name = "activate", description = "Activate BetterPlayer with your licence key.")
public class ActivateCommandExecutor implements CommandExecutor {

	public ActivateCommandExecutor(BotConfig botConfig) {}
	
	@Override
	public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
		JDA jda = betterPlayer.getJdaHandler().getJda();
		TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());
		Guild guild = jda.getGuildById(parameters.getGuildId());
		
		//Check if the sender has the required permission
		//if not inform them and return
		List<Permission> memberPermissions = new ArrayList<>(parameters.getSenderMember().getPermissions());
		if(!memberPermissions.contains(Permission.MANAGE_SERVER)) {
			senderChannel.sendMessage("You do not have permissions to modify this command. You must have the 'manage server' permission!").queue();
			return;
		}
		
		if(!betterPlayer.getAuthBinder().isAvailabe()) {
			senderChannel.sendMessage("Authentication is not available. Please use the official BetterPlayer bot to use premium/advanced features: https://betterplayer.net").queue();
			return;
		}
			
		String[] args = parameters.getArgs();

		if(args.length == 1) {
			Pattern p = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
			Matcher m = p.matcher(args[0]);
			if(m.matches()) {
				if(betterPlayer.getAuthBinder().isKeyRegistered(args[0])) {
					//TODO activate
				} else {
					senderChannel.sendMessage("The activation key you provided is invalid. Please double check your email").queue();
				}
			} else {
				senderChannel.sendMessage("The activation key you provided is not in the correct format. Please double check your email").queue();
				return;
			}
		} else {
			boolean isGuildActivated = betterPlayer.getAuthBinder().isActivated(parameters.getGuildId());
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle(guild.getName())
					.setColor(Color.GRAY)
					.addField("Activation Status", (isGuildActivated) ? "Activated" : "Not activated", false)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			if(!isGuildActivated) {
				eb.addField("Activate", "You can activate your guild with ``" + betterPlayer.getGuildConfig().getConfigValue(parameters.getGuildId(), "commandprefix") + "activate <licence key>``", false);
				eb.addField("More Information", "Check out https://betterplayer.net/premium/ for more information", false);
			}
			
			senderChannel.sendMessage(eb.build()).queue();
		}
	}
}
