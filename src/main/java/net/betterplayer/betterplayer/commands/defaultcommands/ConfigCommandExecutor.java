package net.betterplayer.betterplayer.commands.defaultcommands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.config.guild.GuildConfigManifest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This command provides a way for server administrators to change config options for BetterPlayer on Discord<br>
 * This command requires the sender to have the 'manage server' permission
 */
@BotCommand(name = "config", description = "Configure BetterPlayer", aliases = {"option", "options"})
public class ConfigCommandExecutor implements CommandExecutor {

	public ConfigCommandExecutor(ConfigManifest botConfig) {}
	
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
		
		String[] args = parameters.getArgs();
		
		//$config get <option>
		//$config set <option> <value>
		if(args.length == 2 && args[0].equalsIgnoreCase("get")){
			//Get operation
			String option = args[1].toLowerCase();
			
			GuildConfigManifest manifest = betterPlayer.getGuildConfig().getManifest(guild.getIdLong());
			if(manifest == null) {
				manifest = betterPlayer.getGuildConfig().getDefaultManifest(guild.getIdLong());
			}
			
			String value = null;
			for(Field f : manifest.getClass().getDeclaredFields()) {
				if(f.getName().toLowerCase().equals(option)) {
					f.setAccessible(true);

					try {
						value = f.get(manifest).toString();
					} catch(Exception e) {
						e.printStackTrace();
						senderChannel.sendMessage("Something went wrong, please try again later!").queue();
						return;
					}
				}
			}
			
			//Check if the value is null, meaning the config option does not exist
			if(value == null) {
				senderChannel.sendMessage("Unknown config option!").queue();
				return;
			}
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Configuration for " + guild.getName())
					.setColor(BetterPlayer.GRAY)
					.addField(option, value, false)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

			senderChannel.sendMessageEmbeds(eb.build()).queue();
		} else if(args.length == 3 && args[0].equalsIgnoreCase("set")) {
			//Set operation
			String option = args[1].toLowerCase();
			String value = args[2].toLowerCase();
			
			GuildConfigManifest manifest = betterPlayer.getGuildConfig().getManifest(guild.getIdLong());
			if(manifest == null) {
				manifest = betterPlayer.getGuildConfig().getDefaultManifest(guild.getIdLong());
			}
			
			String originalValue = null;
			for(Field f : manifest.getClass().getDeclaredFields()) {
				if(f.getName().toLowerCase().equals(option)) {
					f.setAccessible(true);

					try {
						originalValue = f.get(manifest).toString();
					} catch(Exception e) {
						e.printStackTrace();
						senderChannel.sendMessage("Something went wrong, please try again later!").queue();
						return;
					}
				}
			}
			
			//Check if the original value is null, meaning the config option does not exist
			if(originalValue == null) {
				senderChannel.sendMessage("Unknown config option!").queue();
				return;
			}
			
			for(Field f : manifest.getClass().getDeclaredFields()) {
				if(f.getName().toLowerCase().equals(option)) {
					f.setAccessible(true);
					
					try {
						f.set(manifest, value);
					} catch(Exception e) {
						e.printStackTrace();
						senderChannel.sendMessage("Something went wrong, please try again later!").queue();
						return;
					}
				}
			}
			
			betterPlayer.getGuildConfig().setManifest(guild.getIdLong(), manifest);
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Configuration for " + guild.getName())
					.setColor(BetterPlayer.GRAY)
					.addField(option, "From " + originalValue.toString() + " to " + value, false)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessageEmbeds(eb.build()).queue();
		} else {
			//Sender idd not provide the correct number of arguments
			GuildConfigManifest manifest = betterPlayer.getGuildConfig().getManifest(guild.getIdLong());
			senderChannel.sendMessage("Invalid number of arguments provided. See ''" + manifest.getCommandPrefix() + "help'' for more info!").queue();
		}
	}
}