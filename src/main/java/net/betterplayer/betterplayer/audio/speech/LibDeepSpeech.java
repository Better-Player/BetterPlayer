package net.betterplayer.betterplayer.audio.speech;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import org.deepspeech.libdeepspeech.DeepSpeechModel;

public class LibDeepSpeech extends DeepSpeechModel {
	
	public LibDeepSpeech(String modelPath) {		
		super(true);
		loadNativeLibs();
		
		super.loadModel(modelPath);
	}
	
	public void loadNativeLibs() {
        String jniName = "libdeepspeech-jni.so";
        String libName = "libdeepspeech.so";
        
        System.out.println("Setting up DeepSpeech...");
        
        URL jniUrl = DeepSpeechModel.class.getResource("/jni/x86_64/" + jniName);
        URL libUrl = DeepSpeechModel.class.getResource("/jni/x86_64/" + libName);
        File tmpDir = null;
		try {
			tmpDir = Files.createTempDirectory("libdeepspeech").toFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        tmpDir.deleteOnExit();
    	
        File jniTmpFile = new File(tmpDir, jniName);
        jniTmpFile.deleteOnExit();
        File libTmpFile = new File(tmpDir, libName);
        libTmpFile.deleteOnExit();
        
        try (
        		InputStream jniIn = jniUrl.openStream();
        		InputStream libIn = libUrl.openStream();
        ) {
            Files.copy(jniIn, jniTmpFile.toPath());
            Files.copy(libIn, libTmpFile.toPath());
        } catch (IOException e) {
			e.printStackTrace();
		}
                
        System.load(jniTmpFile.getAbsolutePath());
        System.load(libTmpFile.getAbsolutePath());
	}
}
