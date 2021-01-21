package nl.thedutchmc.betterplayer.audio.speech;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DeepSpeechUtils {

    //Helper function to read a char from a RandomAccessFile in little endian
    public static char readLEChar(RandomAccessFile f) throws IOException {
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        return (char)((b2 << 8) | b1);
    }
    
    //Helper function to read an integer from a RandomAccessFile in little endian
    public static int readLEInt(RandomAccessFile f) throws IOException {
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        byte b3 = f.readByte();
        byte b4 = f.readByte();
        return (int)((b1 & 0xFF) | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24);
    }
}
