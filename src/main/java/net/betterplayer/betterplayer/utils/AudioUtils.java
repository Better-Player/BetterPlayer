package net.betterplayer.betterplayer.utils;

public class AudioUtils {

    
    public static short[] byteArrayToShortArray(byte[] input) {
    	int size = input.length;
    	short[] shortArray = new short[size];

    	for (int index = 0; index < size; index++) {
    	    shortArray[index] = (short) input[index];
    	}
    	    
    	return shortArray;
    }
    
    public static byte[] shortArrayToByteArray(short[] input) {
    	byte[] resultBytes = new byte[input.length *2];
    	for(int i = 0; i < input.length /2; i+=2) {
    		resultBytes[i] = (byte) (input[i] & 0xFF);
    		resultBytes[i+1] = (byte) ((input[i] >> 8) & 0xFF);
    	}
    	
    	return resultBytes;
    }
}
