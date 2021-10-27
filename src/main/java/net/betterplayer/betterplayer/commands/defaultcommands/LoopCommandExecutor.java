package net.betterplayer.betterplayer.commands.defaultcommands;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.annotations.BotCommand;
import net.betterplayer.betterplayer.audio.BetterAudioManager;
import net.betterplayer.betterplayer.audio.queue.QueueManager;
import net.betterplayer.betterplayer.commands.CommandExecutor;
import net.betterplayer.betterplayer.commands.CommandParameters;
import net.betterplayer.betterplayer.config.ConfigManifest;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

@BotCommand(name = "loop", description = "Loop the current queue", aliases = {"l"})
public class LoopCommandExecutor implements CommandExecutor {

    public LoopCommandExecutor(ConfigManifest config) {}

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

        if(qm.isLoopMode(guildId)) {
            qm.setLoopMode(guildId, false);
            senderChannel.sendMessage("Loop mode has been disabled!").queue();
        } else {
            qm.setLoopMode(guildId, true);
            senderChannel.sendMessage("Loop mode has been enabled!").queue();
        }
    }
}
