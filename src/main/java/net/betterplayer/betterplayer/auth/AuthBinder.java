package net.betterplayer.betterplayer.auth;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.function.Consumer;

import net.betterplayer.auth.ThrowingFunction;
import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.config.guild.database.SqlManager;
import net.betterplayer.betterplayer.utils.ReflectionUtils;

public class AuthBinder {

	private final SqlManager sqlManager;
	private final String dbName;
	
	private Object betterPlayerAuthInstance;
	private Object authenticatorInstance;
	
	private Method isAuthenticatedMethod;

	/**
	 * After creating an instance of AuthBinder you should:<br>
	 * 1. {@link AuthBinder#isAvailabe()}<br>
	 * 2. {@link AuthBinder#setup()}
	 */
	public AuthBinder(SqlManager sqlManager, String dbName) {
		this.sqlManager = sqlManager;
		this.dbName = dbName;
	}
	
	/**
	 * Check if authentication is available
	 * @return Returns true if authentication is available
	 */
	public boolean isAvailabe() {
		//Check if the class is present. If it is that means that authentication is available
		return ReflectionUtils.getClass("net.betterplayer.auth.BetterPlayerAuth") != null;
	}
	
	/**
	 * Setup AuthBinder-er. You should do this as few times as possible
	 */
	public void setup() {
		//Get the BetterPlayerAuth class and constructor
		Class<?> betterPlayerAuthClazz = ReflectionUtils.getClass("net.betterplayer.auth.BetterPlayerAuth");
		Constructor<?> betterPlayerAuthConstructor = ReflectionUtils.getConstructor(betterPlayerAuthClazz, Consumer.class, Consumer.class, Consumer.class, ThrowingFunction.class, ThrowingFunction.class, ThrowingFunction.class);
		
		Consumer<Object> logInfoFunction = BetterPlayer::logInfo;
		Consumer<Object> logErrorFunction = BetterPlayer::logError;
		Consumer<Object> logDebugFunction = BetterPlayer::logDebug;
		
		ThrowingFunction<PreparedStatement, ResultSet> executeFetchQueryFunction = sqlManager::executeFetchQuery;
		ThrowingFunction<PreparedStatement, Integer> executePutQueryFunction = sqlManager::executePutQuery;
		ThrowingFunction<String, PreparedStatement> createPreparedStatementFunction = sqlManager::createPreparedStatement;
		
		//Create an instance of BetterPlayerAuth
		this.betterPlayerAuthInstance = ReflectionUtils.createInstance(betterPlayerAuthConstructor, logInfoFunction, logErrorFunction, logDebugFunction, executeFetchQueryFunction, executePutQueryFunction, createPreparedStatementFunction);
		
		//Get the Authenticator class and it's constructor
		Class<?> authClazz = ReflectionUtils.getClass("net.betterplayer.auth.Authenticator");
		Constructor<?> authConstructor = ReflectionUtils.getConstructor(authClazz, this.betterPlayerAuthInstance.getClass(), String.class);	
		
		//Create an instance of Authenticator
		this.authenticatorInstance = ReflectionUtils.createInstance(authConstructor, this.betterPlayerAuthInstance, this.dbName);
		
		//Get the method Authenticator#isAuthenticated(UUID, long)
		this.isAuthenticatedMethod = ReflectionUtils.getMethod(authClazz, "isAuthenticated", UUID.class, long.class);	
	}
	
	/**
	 * Check if a guild is authenticated ('licenced')
	 * @param licenceKey The licence key of the Guild
	 * @param guildId The ID of the Guild
	 * @return True if authenticated
	 */
	public boolean isAuthenticated(String licenceKey, long guildId) {
		//Invoke the method to Authenticator#isAuthenticated(UUID, long)
		Boolean isAuthenticated = (Boolean) ReflectionUtils.invokeMethod(this.authenticatorInstance, this.isAuthenticatedMethod, UUID.fromString(licenceKey), guildId);
		
		//If isAuthenticated returned null, that means an exception occured, returning false as a safeguard
		if(isAuthenticated == null) {
			return false;
		}
		
		return isAuthenticated;
	}
}
