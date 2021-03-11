package net.betterplayer.betterplayer.audio.io;

import java.nio.ByteBuffer;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.utils.AudioUtils;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

public class AudioReceiver implements AudioReceiveHandler {

	private final ByteBuffer buffer = ByteBuffer.allocate(Integer.MAX_VALUE /4);
	private final BetterPlayer betterPlayer;
	
	private int cyclesNoTalking = 0;
	private boolean awaitingEndOfTalking = false;
	
	public AudioReceiver(BetterPlayer betterPlayer) {
		this.betterPlayer = betterPlayer;
	}
	
	@Override
	public boolean canReceiveCombined() {
		return true;
	}
	
	int bufTotal = 0;
	
	@Override
	public void handleCombinedAudio(CombinedAudio combinedAudio) {
		
		if(combinedAudio.getUsers().isEmpty()) {
			if(!awaitingEndOfTalking) return;
			
			cyclesNoTalking++;
		} else {
			cyclesNoTalking = 0;
			awaitingEndOfTalking = true;
		}
		
		if(cyclesNoTalking >= 50) {
			awaitingEndOfTalking = false;
		}
		
		byte[] audio = combinedAudio.getAudioData(1.0d);
		bufTotal += audio.length;
		buffer.put(audio);
		
		
		if(!awaitingEndOfTalking) {
			System.out.println("End of talking!");
			System.out.println("buffer size:" + buffer.remaining());
			byte[] buff = buffer.array();
			buffer.clear();
			System.out.println("buff size: " + buff.length);
			System.out.println("bufTotal: " + bufTotal);
			short[] audioAsShort = AudioUtils.byteArrayToShortArray(buff);
			
			System.out.println("Shorts in the buffer: " + audioAsShort.length);
			short[] audioDownsampled = betterPlayer.getLibBetterPlayerBinder().transformDiscordAudioToSpec(audioAsShort);
			System.out.println("Audio size: " + audioDownsampled.length);
		}		
	}
}
