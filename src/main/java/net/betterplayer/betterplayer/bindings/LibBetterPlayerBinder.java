package net.betterplayer.betterplayer.bindings;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import net.betterplayer.betterplayer.utils.Utils;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.utils.ReflectionUtils;

public class LibBetterPlayerBinder {

	private Object libBetterPlayerInstance;
	private Object audioInstance;
	
	private Method transformDiscordAudioToSpecMethod;
	private Method loadNativeLibrariesMethod;
	
	private boolean forceUnavailable = false;
	
	public boolean isAvailable() {
		if(forceUnavailable) {
			return false;
		}
		
		return ReflectionUtils.getClass("net.betterplayer.libbetterplayer.LibBetterPlayer") != null;
	}
	
	/**
	 * Transform the supplied audio to the output specification.<br>
	 * Will throw a {@link RuntimeException} if an issue occurs.<br>
	 * <br>
	 * <strong> This is a native method!</strong>
	 * <br>
	 *  <table>
	 * 		<tr>
	 * 			<th> </th>
	 * 			<th> Sample rate </th>
	 * 			<th> Channels </th>
	 * 			<th> Bit depth </th>
	 * 			<th> Encoding </td>
	 * 		</tr>
	 * 		<tr>
	 * 			<td> <strong>Input</strong> </td>
	 * 			<td> 48Khz </td>
	 * 			<td> 2 - Stereo </td>
	 * 			<td> 16 bits </td>
	 * 			<td> Signed PCM </td>
	 * 		</tr>
	 * 		<tr>
	 * 			<td><strong>Output</strong></td>
	 * 			<td> 16Khz </td>
	 * 			<td> 1 - Mono </td>
	 * 			<td> 16 bits </td>
	 * 			<td> Signed PCM </td>
	 * 		</tr>
	 * 	</table>
	 * @param input The input audio, must match the described specification
	 * @return Returns the transformed audio, will match the output specification
	 */
	public short[] transformDiscordAudioToSpec(short[] input) {
		Object o =  ReflectionUtils.invokeMethod(this.audioInstance, this.transformDiscordAudioToSpecMethod, input);
		
		if(o == null) {
			BetterPlayer.logError("Result from transformDiscordAudioToSpec (Native) is null. This shouldn't happen!");
			return null;
		}
		
		return (short[]) o;
		
		//If audioAtSpec is null, that means an error occured, probably at the native level
		/*if(audioAtSpec == null) {
			throw new RuntimeException("AudioAtSpec is null. It should not be");
		}
		
		return null;
		//return audioAtSpec;*/
	}
	
	/**
	 * Setup LibBetterPlayerBinder-er. You should do this as few times as possible
	 */
	public void setup() {
		//Get the LibBetterPlayer class and its constructor
		Class<?> libBetterPlayerClazz = ReflectionUtils.getClass("net.betterplayer.libbetterplayer.LibBetterPlayer");
		Constructor<?> libBetterPlayerConstructor = ReflectionUtils.getConstructor(libBetterPlayerClazz, Consumer.class, Consumer.class, Consumer.class);
			
		Consumer<Object> logInfoFunction = BetterPlayer::logInfo;
		Consumer<Object> logErrorFunction = BetterPlayer::logError;
		Consumer<Object> logDebugFunction = BetterPlayer::logDebug;
		
		//Create an instance of the LibBetterPlayer class
		this.libBetterPlayerInstance = ReflectionUtils.createInstance(libBetterPlayerConstructor, logInfoFunction, logErrorFunction, logDebugFunction);
		
		//Get the Methods we want from the LibBetterPlayer class
		this.loadNativeLibrariesMethod = ReflectionUtils.getMethod(libBetterPlayerClazz, "loadNativeLibraries");
		
		//Load native libraries
		loadNativeLibraries();

		if(this.forceUnavailable) {
			return;
		}
		
		//Get the Audio class nat its constructor
		Class<?> audioClazz = ReflectionUtils.getClass("net.betterplayer.libbetterplayer.Audio");
		Constructor<?> audioConstructor = ReflectionUtils.getConstructor(audioClazz, this.libBetterPlayerInstance.getClass());
		
		//Create an instance of the Audio class
		this.audioInstance = ReflectionUtils.createInstance(audioConstructor, this.libBetterPlayerInstance);
		
		//Get the Methods we want from the Audio class
		this.transformDiscordAudioToSpecMethod = ReflectionUtils.getMethod(audioClazz, "transformDiscordAudioToSpec", short[].class);
		

	}
	
	/**
	 * Load the native Rust Libraries
	 */
	private void loadNativeLibraries() {
		try {
			ReflectionUtils.invokeMethodWithExceptions(this.libBetterPlayerInstance, this.loadNativeLibrariesMethod);
		} catch(Exception e) {
			BetterPlayer.logError("Exception when trying to load native libraries. Run with debug mode for more details.");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
		
			forceUnavailable = true;
			return;
		}
	}
}
