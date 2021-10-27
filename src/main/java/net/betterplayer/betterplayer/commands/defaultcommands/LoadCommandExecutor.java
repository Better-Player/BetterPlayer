package net.betterplayer.betterplayer.commands.defaultcommands;

import dev.array21.jdbd.exceptions.SqlException;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueItem;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.betterplayer.betterplayer.config.guild.GuildConfigManifest;
import net.betterplayer.betterplayer.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.LinkedList;
import java.util.Queue;

@BotCommand(name = "load", description = "Load a saved playlist")
public class LoadCommandExecutor implements CommandExecutor {

    public LoadCommandExecutor(ConfigManifest manifest) {}

    @Override
    public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
        //Verify that the user is in the same voice channel as BetterPlayer
        if(!new VoiceChannelVerify().verify(betterPlayer, parameters, true)) {
            return;
        }

        long guildId = parameters.getGuildId();
        JDA jda = betterPlayer.getJdaHandler().getJda();
        TextChannel senderChannel = jda.getTextChannelById(parameters.getChannelId());

        GuildConfigManifest ggm = betterPlayer.getGuildConfig().getManifest(guildId);

        if(!parameters.hasArgs()) {
            senderChannel.sendMessage(String.format("This command requires an argument. See %shelp for more information.", ggm.getCommandPrefix())).queue();
            return;
        }

        if(!Utils.verifyPositiveInteger(parameters.getArgs()[0], senderChannel)) {
            return;
        }

        int queueId = Integer.valueOf(parameters.getArgs()[0]);

        BetterAudioManager bam = betterPlayer.getBetterAudioManager();
        QueueManager qm = bam.getQueueManager();

        boolean queueIdExists;
        try {
            queueIdExists = qm.loadQueue(guildId, queueId);
        } catch(SqlException e) {
            BetterPlayer.logError("Failed to load Queue: " + e.getMessage());
            senderChannel.sendMessage("Something went wrong. Please try again later.").queue();
            return;
        }

        if(!queueIdExists) {
            senderChannel.sendMessage("That Queue ID does not exist. To get the correct Queue ID, scroll back to when you saved the Queue you want to load.").queue();
            return;
        }

        // Get is safe, as the loadQueue method creates a queue
        int queueLength = qm.getQueueSize(guildId).get();

        if(qm.getNowPlaying(guildId).isPresent()) {
            queueLength++;
            QueueItem nowPlaying = qm.getNowPlaying(guildId).get();

            bam.loadTrack(nowPlaying.trackIdentifier(), guildId);
        }

        if(queueLength == 1) {
            senderChannel.sendMessage("1 Track has been loaded!").queue();
        } else {
            senderChannel.sendMessage(String.format("%d Tracks have been loaded!", queueLength)).queue();
        }
    }
}
