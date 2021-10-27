package net.betterplayer.betterplayer.commands.defaultcommands;

import dev.array21.jdbd.exceptions.SqlException;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.config.guild.GuildConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

@BotCommand(name = "save", description = "Save the current Queue for later use")
public class SaveCommandExecutor implements CommandExecutor {

    public SaveCommandExecutor(ConfigManifest manifest) {}

    @Override
    public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
        //Verify that the user is in the same voice channel as BetterPlayer
        if(!new VoiceChannelVerify().verify(betterPlayer, parameters, false)) {
            return;
        }

        long guildId = parameters.getGuildId();
        JDA jda = betterPlayer.getJdaHandler().getJda();
        TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

        BetterAudioManager bam = betterPlayer.getBetterAudioManager();
        QueueManager qm = bam.getQueueManager();

        long queueId;
        try {
            // Get is save as the check above verifies a Queue exists for the current Guild
            queueId = qm.saveQueue(guildId).get();
        } catch(SqlException e) {
            BetterPlayer.logError(String.format("Failed to save Queue: %s", e.getMessage()));
            BetterPlayer.logDebug(Utils.getStackTrace(e));
            senderChannel.sendMessage("Something went wrong. Please try again later.").queue();

            return;
        }

        GuildConfigManifest ggm = betterPlayer.getGuildConfig().getManifest(guildId);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Your Queue has been saved!")
                .setColor(BetterPlayer.GRAY)
                .setDescription(String.format("To load this queue, you can use `%sload %d`", ggm.getCommandPrefix(), queueId))
                .setFooter("Brought to you by BetterPlayer. Powered by YouTube", "https://archive.org/download/mx-player-icon/mx-player-icon.png");

        senderChannel.sendMessageEmbeds(eb.build()).queue();
    }
}
