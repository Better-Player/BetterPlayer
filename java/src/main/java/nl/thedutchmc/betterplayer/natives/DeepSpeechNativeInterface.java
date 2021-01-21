package nl.thedutchmc.betterplayer.natives;

public class DeepSpeechNativeInterface {

	public void loadNative() {
		/*String resourceName = "libbetterplayer.so";
        URL resourceUrl = DeepSpeechNativeInterface.class.getResource("/jni/x86_64/" + resourceName);

        File tmpDir = null;
        try {
			tmpDir = Files.createTempDirectory("libbetterplayer").toFile();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        //tmpDir.deleteOnExit();
        File resourceFile = new File(tmpDir, resourceName);
        //resourceFile.deleteOnExit();
        
        try (InputStream resourceIn = resourceUrl.openStream()) {
        	
        	Files.copy(resourceIn, resourceFile.toPath());
        	
        } catch(IOException e) {
        	e.printStackTrace();
        }
        
		System.load(resourceFile.getAbsolutePath());
		
		System.out.println(resourceFile.getAbsolutePath());*/
		System.load("/tmp/libbetterplayer.so");
	}
	
	public native void callNativeMethod(byte[] inputAudio);
}