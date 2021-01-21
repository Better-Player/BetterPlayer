package nl.thedutchmc.betterplayer.audio.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;

public class AudioUtils {

    public static short[] downsample(short[] inSamples, int srIn, int srOut) {
		short[] temp = new short[inSamples.length];
		int inSampleIndex = -1;
		int outSampleIndex = 0;
		int k = srOut;
		boolean done = false;
		while (!done) {
			int sum = 0;
			for (int i = 0; i < srIn; i++) {
				if (k == srOut) {
					inSampleIndex++;
					if (inSampleIndex >= inSamples.length) {
						done = true;
						break;
					}
					k = 0;
				}
				sum += inSamples[inSampleIndex];
				k++;
			}
			temp[outSampleIndex++] = (short) (sum / srIn);
		}
		return Arrays.copyOf(temp, outSampleIndex);
	}
    
    public static short[] byteArrayToShortArray(byte[] input) {
    	short[] dataShort = new short[input.length /2];
    	ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(dataShort);
    	
    	return dataShort;
    }
    
    public static byte[] shortArrayToByteArray(short[] input) {
    	byte[] resultBytes = new byte[input.length *2];
    	for(int i = 0; i < input.length /2; i+=2) {
    		resultBytes[i] = (byte) (input[i] & 0xFF);
    		resultBytes[i+1] = (byte) ((input[i] >> 8) & 0xFF);
    	}
    	
    	return resultBytes;
    }
    
    public static class Stereo2Mono {
    	private int mode;
    	
    	public Stereo2Mono() {
    		this.mode = AudioChannels.STEREO.getValue();
    	}
    	
    	public Stereo2Mono(AudioChannels mode) {
    		this.mode = mode.getValue();
    	}
    	
    	public AudioInputStream apply(AudioInputStream ais) {
    		return new MonoAudioInputStream(ais, mode);
    	}
    }
}
