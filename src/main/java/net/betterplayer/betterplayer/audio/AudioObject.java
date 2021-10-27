package net.betterplayer.betterplayer.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public record AudioObject(AudioTrack track, AudioPlayer player, String trackName, String artistName) {}
