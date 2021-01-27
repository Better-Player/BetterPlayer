package net.betterplayer.betterplayer.audio.speech;
/*
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.deepspeech.libdeepspeech.DeepSpeechStreamingState;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.Utils;

public class DeepSpeechRecognition {
    private LibDeepSpeech deepSpeechModel = null;

    private String TF_MODEL, SCORER_MODEL;
    private final int BEAM_WIDTH = 50;
	
    public DeepSpeechRecognition() {
    	File jarDir = null;
		
    	//Get the jar path
    	try {
			final File jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			jarDir = new File(jarPath.getParentFile().getPath());			
		} catch (URISyntaxException e) {
			BetterPlayer.logError("Unable to determine JAR path! Enable debug mode for more details!");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			System.exit(1);
		}
		
    	//TensorFlow model and Scorer model
		File tfModelFile = new File(jarDir, "deepspeech-0.9.3-models.pbmm");
		File scorerModelFile = new File(jarDir, "deepspeech-0.9.3-models.scorer");
		
		//Check if the TensorFlow model exists
		if(!tfModelFile.exists()) {
			BetterPlayer.logError("TensorFlow model not found. Check installation instructions!");
			System.exit(1);
		}
		
		//Check if the Scorer model exists
		if(!scorerModelFile.exists()) {
			BetterPlayer.logError("Scorer model not found. Check installation instructions!");
			System.exit(1);
		}
		
		//Get the full paths to both models
		TF_MODEL = tfModelFile.getAbsolutePath();
		SCORER_MODEL = scorerModelFile.getAbsolutePath();
    }
	
    public void createModel(String tfModel, String scorerModel) {
        if(this.deepSpeechModel == null) {
            this.deepSpeechModel = new LibDeepSpeech(tfModel);
            this.deepSpeechModel.enableExternalScorer(scorerModel);
            this.deepSpeechModel.setBeamWidth(BEAM_WIDTH);
        }
    }
    
    public DeepSpeechStreamingState openStream() {
    	createModel(this.TF_MODEL, this.SCORER_MODEL);
    	return deepSpeechModel.createStream();
    }
    
    public String finishStream(DeepSpeechStreamingState ctx) {
    	return deepSpeechModel.finishStream(ctx);
    }
    
    public void feedAudioContent(DeepSpeechStreamingState ctx, short[] input) {
    	deepSpeechModel.feedAudioContent(ctx, input, input.length);
    }
    
    //The format we want is a 16Khz 16 bit mono .WAV file!
    public void doInfer(String audioFile) {
        long inferenceExecTime = 0L;

        //Create a new model to use during the inference
    	this.createModel(this.TF_MODEL, this.SCORER_MODEL);
    	
    	System.out.println("Extracting audio features...");
    	
    	try {
            RandomAccessFile wave = new RandomAccessFile(audioFile, "r");

            //Assert that the audio format is PCM
            wave.seek(20);
            char audioFormat = DeepSpeechUtils.readLEChar(wave);
            assert (audioFormat == 1); // 1 is PCM

            //Assert that the amount of channels is 1, meaning mono audio
            wave.seek(22);
            char numChannels = DeepSpeechUtils.readLEChar(wave);
            assert (numChannels == 1); // MONO

            //Assert that the sample rate is the sample rate expected by the model
            //This can vary per model!
            wave.seek(24);
            int sampleRate = DeepSpeechUtils.readLEInt(wave);
            assert (sampleRate == deepSpeechModel.sampleRate()); // desired sample rate

            //Assert that the bits per sample is 16
            wave.seek(34);
            char bitsPerSample = DeepSpeechUtils.readLEChar(wave);
            assert (bitsPerSample == 16); // 16 bits per sample
            
            //Assert that the buffer size is more than 0
            wave.seek(40);
            int bufferSize = DeepSpeechUtils.readLEInt(wave);
            assert (bufferSize > 0);

            //Read the actual contents of the audio
            wave.seek(44);
            byte[] bytes = new byte[bufferSize];
            wave.readFully(bytes);

            //Turn the byte[] into a short[] and set the correct byte order
            short[] shorts = new short[bytes.length/2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            
            System.out.println("Running inference...");

            //current time. Used later on to calculate the time the inference took
            long inferenceStartTime = System.currentTimeMillis();

            //This is where we actually do the inference
            String decoded = this.deepSpeechModel.stt(shorts, shorts.length);

            //Calculate how long it took to run the inference
            inferenceExecTime = System.currentTimeMillis() - inferenceStartTime;

            System.out.println("Inference result: " + decoded);

        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    	
    	System.out.println("Finished! Took " + inferenceExecTime);
    }
	
}
*/