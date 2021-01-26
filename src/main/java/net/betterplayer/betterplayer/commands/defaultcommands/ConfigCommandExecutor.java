package net.betterplayer.betterplayer.commands.defaultcommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.guild.GuildConfigManager.ConfigValueType;

/**
 * This command provides a way for server administrators to change config options for BetterPlayer on Discord<br>
 * This command requires the sender to have the 'manage server' permission
 */
public class ConfigCommandExecutor implements CommandExecutor {

	//These config options are booleans
	final List<String> optionsOfTypeBool = new ArrayList<>(Arrays.asList(
			"usedeepspeech"
	));
	
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
			Object value = betterPlayer.getGuildConfig().getConfigValue(parameters.getGuildId(), option);

			//Check if the value is null, meaning the config option does not exist
			if(value == null) {
				senderChannel.sendMessage("Unknown config option!").queue();
				return;
			}
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Configuration for " + guild.getName())
					.setColor(Color.GRAY)
					.addField(option, value.toString(), false)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

			senderChannel.sendMessage(eb.build()).queue();
		} else if(args.length == 3 && args[0].equalsIgnoreCase("set")) {
			//Set operation
			String option = args[1].toLowerCase();
			String value = args[2].toLowerCase();
			
			//Get the original config value
			Object originalValue = betterPlayer.getGuildConfig().getConfigValue(parameters.getGuildId(), option);
			
			//Check if the original value is null, meaning the config option does not exist
			if(originalValue == null) {
				senderChannel.sendMessage("Unknown config option!").queue();
				return;
			}
			
			//Determine the type of the value
			ConfigValueType cvt;
			if(optionsOfTypeBool.contains(option)) {
				cvt = ConfigValueType.BOOLEAN;
				
				//Check if the value is 'true' or 'false', the only two acceptable answers for a Boolean
				if(!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
					senderChannel.sendMessage("Value must be either ``true`` or ``false``!").queue();
					return;
				}
			} else {
				cvt = ConfigValueType.STRING;
			}
			
			betterPlayer.getGuildConfig().setConfigValue(parameters.getGuildId(), option, value, cvt);
			
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Configuration for " + guild.getName())
					.setColor(Color.GRAY)
					.addField(option, "From " + originalValue.toString() + " to " + value, false)
					.setFooter("Brought to you by BetterPlayer", "https://archive.org/download/mx-player-icon/mx-player-icon.png");
			
			senderChannel.sendMessage(eb.build()).queue();
		} else {
			//Sender idd not provide the correct number of arguments
			senderChannel.sendMessage("Invalid number of arguments provided. See ``" + betterPlayer.getGuildConfig().getConfigValue(parameters.getGuildId(), "commandprefix") + "help`` for more info!").queue();
		}
	}
}