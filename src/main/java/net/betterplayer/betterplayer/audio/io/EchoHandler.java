package net.betterplayer.betterplayer.audio.io;
/*
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.deepspeech.libdeepspeech.DeepSpeechStreamingState;

import net.betterplayer.betterplayer.BetterPlayer;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.betterplayer.betterplayer.audio.speech.DeepSpeechRecognition;
import net.betterplayer.betterplayer.audio.utils.AudioUtils;
/*

public class EchoHandler implements AudioSendHandler, AudioReceiveHandler {

	private DeepSpeechRecognition deepSpeechRecognition;
	private boolean isStreaming = false;
	private boolean awaitingEndOfTalking = false;
	private DeepSpeechStreamingState deepSpeechStreamingState;
	private int cyclesNoTalking = 11;
	
	//private SpeechDetection speechDetection;
	//private long guildId;
	
	public EchoHandler(BetterPlayer betterPlayer, long guildId) {
		//speechDetection = new SpeechDetection(betterPlayer);
		//this.guildId = guildId;
		
		deepSpeechRecognition = new DeepSpeechRecognition();
	}
	
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
	
    @Override
    public boolean canReceiveCombined() {
    	return queue.size() < 10;
    }
    
    //private List<Byte> prevBuf = new LinkedList<>();
    
    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
    	
    	//Check if a user is talking
    	if(combinedAudio.getUsers().isEmpty()) {
    		
    		//If we're not waiting for the user to stop talking we can stop straight away
    		if(!awaitingEndOfTalking) {
    			return;
    		}
    		
    		cyclesNoTalking++;
    	} else {
    		//A user is now talking. Reset the cyclesNoTalking and we're now awaiting the end of the talking
    		cyclesNoTalking = 0;
    		awaitingEndOfTalking = true;
    	}
    	
    	//If we were not streaming already, open a stream and set isStreaming true
    	if(!isStreaming) {
    		deepSpeechStreamingState = deepSpeechRecognition.openStream();
    		isStreaming = true;
    	}
    	
    	//If no user has talked for 1.0 seconds (50 cycles of 20 ms)
    	//Then we assume the command is finished.
    	if(cyclesNoTalking >= 50) {
    		isStreaming = false;
    		awaitingEndOfTalking = false;
    		System.out.println("Result: " + deepSpeechRecognition.finishStream(deepSpeechStreamingState));
    		return;
    	}
    	        
    	//Get the raw audio from Discord
    	byte[] inputAudio = combinedAudio.getAudioData(1.0f);
    	
    	//Convert the audio to a short[]
    	short[] audioAsShort = AudioUtils.byteArrayToShortArray(inputAudio);
    	
    	//Downsample from 48Khz to 16Khz
    	short[] audioDownsampled = AudioUtils.downsample(audioAsShort, 48000, 16000);
    	
    	//Turn the audio back into a byte[]
    	byte[] audioDownsampledAsBytes = AudioUtils.shortArrayToByteArray(audioDownsampled);
    	
    	//Convert the audio from stereo to mono
    	AudioFormat af = new AudioFormat(48000, 16, 2, true, true);
    	AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioDownsampledAsBytes), af, audioDownsampledAsBytes.length);
    	
    	//Extract the data as byte[] from the AudioInputStream
    	byte[] audioTransformedBytes = new byte[(int) ais.getFrameLength()];
		try {
			audioTransformedBytes = ais.readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Convert the audio back to short[]
    	short[] audioTransformedShorts = AudioUtils.byteArrayToShortArray(audioTransformedBytes);
    	
    	//Feed the audio to DeepSpeech
    	deepSpeechRecognition.feedAudioContent(deepSpeechStreamingState, audioTransformedShorts);
    	
    	/**
    	 * We know the audio format we receive from JDA:
    	 * - Sample rate: 48Khz
    	 * - Sample size in bits: 16
    	 * - Channels: 2 (Stereo)
    	 * - Signed: True
    	 * - Endianness: Big
    	 */
    	/*
    	AudioFormat inputFormat = new AudioFormat(48000, 16, 2, true, true);
    	AudioInputStream inputStream = new AudioInputStream(new ByteArrayInputStream(inputAudio), inputFormat, inputAudio.length);
    	*/
    	/*
    	 * The desired audio format for Picovoice is:
    	 * - Sample rate: 16Khz
    	 * - Sample size in bits: 16
    	 * - Channels: 1 (Mono)
    	 * - Signed: True
    	 * - Endianness: Big
    	 */
    	/*
    	AudioFormat targetFormat = new AudioFormat(16000f, 16, 1, true, true);
    	AudioInputStream formattedStream = AudioSystem.getAudioInputStream(targetFormat, inputStream);
    	
    	System.out.println(formattedStream.getFrameLength());
    	System.out.println(AudioSystem.isConversionSupported(targetFormat, inputFormat));
    	*/
    	/*
    	 * Next up we have to turn the AudioInputStream into a short[] of size 512.
    	 */
    	/*
    	byte[] formattedBytes = null;
    	try {
    		formattedBytes = new byte[formattedStream.available()];
    		formattedStream.read(formattedBytes);
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	if(formattedBytes == null) {
    		return;
    	}
    	    	
    	if(prevBuf.size() < 1024) {
    		prevBuf.addAll(Arrays.asList(ArrayUtils.toObject(formattedBytes)));
    	}
        	
    	byte[] newBuf = new byte[1024];
    	if(prevBuf.size() >= 1024) {
        	newBuf = ArrayUtils.toPrimitive(prevBuf.stream().limit(1024).collect(Collectors.toList()).toArray(new Byte[1024]));
    	} else {
    		return;
    	}
    	*/
    	
    	//short[] dataShort = new short[newBuf.length /2];
    	//ByteBuffer.wrap(newBuf).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(dataShort);
    	
    	//speechDetection.detect(dataShort);
    	//queue.add(formattedBytes);
    //}
    /*
	@Override
	public boolean canProvide() {
		return !queue.isEmpty();
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		byte[] data = queue.poll();
		return data == null ? null : ByteBuffer.wrap(data);
	}
	
	/*
	 *     	byte[] dataStereo = combinedAudio.getAudioData(1.0f);
    	byte[] dataMono = new byte[dataStereo.length/2];
    	
    	System.out.println(dataStereo.length);
    	
    	for(int i = 0; i < dataStereo.length/4; ++i) {
            int HI = 0;
            int LO = 1;
            int left = (dataStereo[i * 4 + HI] << 8) | (dataStereo[i * 4 + LO] & 0xff);
            int right = (dataStereo[i * 4 + 2 + HI] << 8) | (dataStereo[i * 4 + 2 + LO] & 0xff);
            int avg = (left + right) / 2;
            dataMono[i * 2 + HI] = (byte)((avg >> 8) & 0xff);
            dataMono[i * 2 + LO] = (byte)(avg & 0xff);
    	}
              
        int[] phase1 = new int[dataMono.length / 2];
        for(int i = 0; i < dataMono.length/2; i++) {
            phase1[i] = ((int)(dataMono[i*2] << 8)) | dataMono[i*2+1];
        }
        
        char[] phase2 = new char[phase1.length /3];
        for(int i = 0; i < phase1.length / 3; i++) {
            phase2[i] = (char) ((phase1[i*3] + phase1[i*3+1] + phase1[i*3+2]) / 3);
        }
        
        byte[] result = new byte[dataMono.length /3];
        for(int i = 0; i < phase2.length; i+=2) {
            result[i] = (byte)(phase2[i] >> 8);
            result[i +1] = (byte) phase2[i];
        }
    	
    	if(prevBuf.size() < 1024) {
    		prevBuf.addAll(Arrays.asList(ArrayUtils.toObject(result)));
    	}
        	
    	byte[] newBuf = new byte[1024];
    	if(prevBuf.size() >= 1024) {
        	//newBuf = ArrayUtils.toPrimitive(prevBuf.stream().limit(1024).collect(Collectors.toList()).toArray(new Byte[512]));
        	newBuf = ArrayUtils.toPrimitive((Byte[]) prevBuf.stream().limit(1024).toArray());
    	} else {
    		return;
    	}
    	
    	short[] dataShort = new short[newBuf.length /2];
    	ByteBuffer.wrap(newBuf).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(dataShort);
	 * 
	 */
//}
