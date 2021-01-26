package net.betterplayer.betterplayer.audio.speech;

import java.io.File;
import java.net.URISyntaxException;

import ai.picovoice.picovoice.Picovoice;
import ai.picovoice.picovoice.PicovoiceException;
import ai.picovoice.picovoice.PicovoiceInferenceCallback;
import ai.picovoice.picovoice.PicovoiceWakeWordCallback;
import net.betterplayer.betterplayer.BetterPlayer;

public class SpeechDetection {

	private String keywordPath, contextPath;
	private Picovoice handle;
	
	public SpeechDetection(BetterPlayer betterPlayer) {		
		//Get the path of the JAR, and thus the directory in which we should put the speech files
		try {
			final File jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			final File saveDir = new File(jarPath.getParentFile().getPath());
			
			File keywordFile = new File(saveDir, "keyword.ppn");
			File contextFile = new File(saveDir, "context.rhn");
			
			if(!keywordFile.exists()) {
				betterPlayer.saveResource("keyword.ppn", saveDir.getAbsolutePath());
			}
			
			if(!contextFile.exists()) {
				betterPlayer.saveResource("context.rhn", saveDir.getAbsolutePath());
			}
			
			keywordPath = keywordFile.getAbsolutePath();
			contextPath = contextFile.getAbsolutePath();
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		setupPico();
	}
	
	private void setupPico() {
		PicovoiceWakeWordCallback wakeWordCallback = () -> {
			System.out.println("Wakeword!");
		};
		
		PicovoiceInferenceCallback inferenceCallback = inference -> {
			System.out.println(inference.getIntent());
		};
		
		try {
			handle = new Picovoice.Builder()
					.setKeywordPath(keywordPath)
					.setContextPath(contextPath)
					.setWakeWordCallback(wakeWordCallback)
					.setInferenceCallback(inferenceCallback)
					.build();
		} catch(PicovoiceException e) {
			e.printStackTrace();
		}
	}
	
	public void detect(short[] audioFrame) {
		try {
			this.handle.process(audioFrame);
		} catch (PicovoiceException e) {
			e.printStackTrace();
		}
	}
}
