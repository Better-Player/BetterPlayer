package net.betterplayer.betterplayer.config.guild.database;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.betterplayer.betterplayer.BetterPlayer;
import net.betterplayer.betterplayer.utils.Utils;

public class SqlManager {

	private Connection connection;
	
	private String dbHost, dbName, dbUsername, dbPassword;
	
	public SqlManager(String dbHost, String dbName, String dbUsername, String dbPassword) {
		this.dbHost = dbHost;
		this.dbName = dbName;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
		
		BetterPlayer.logInfo("Initializing database connection...");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch(ClassNotFoundException e) {
			BetterPlayer.logError("Unable to initialize the database connection! MySQL driver not found");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			System.exit(1);
		}
		
		BetterPlayer.logInfo("Connecting to database...");
		this.connection = connect();
		
		if(this.connection == null) {
			BetterPlayer.logError("Unable to establish connection with database!");
			System.exit(1);
		}
	}
	
	/**
	 * Connect to the database
	 * @return Returns the Connection, null if an error occured
	 */
	private Connection connect() {
		try {
			String passwordEncoded = URLEncoder.encode(dbPassword, StandardCharsets.UTF_8);
			return DriverManager.getConnection("jdbc:mysql://" + dbHost + "/" + dbName + "?user=" + dbUsername + "&password=" + passwordEncoded);
		} catch(SQLException e) {
			BetterPlayer.logError("Unable to establish connection to database!");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			return null;
		}
	}
	
	/**
	 * Execute a fetch query to the database
	 * @param preparedStatement PreparedStatement to execute
	 * @return Returns a ResultSet with the results from the database
	 * @throws SQLException
	 */
	public ResultSet executeFetchQuery(PreparedStatement preparedStatement) throws SQLException {
		if(connection.isClosed()) {
			BetterPlayer.logInfo("Connection to database lost. Reconnecting!");
			connection = connect();
		}
		
		return preparedStatement.executeQuery();
	}
	
	/**
	 * Execute a put statement (Insert/Update etc)
	 * @param preparedStatement PreparedStatement to execute
	 * @return Returns the status code returned by the database.
	 * @throws SQLException
	 */
	public int executePutQuery(PreparedStatement preparedStatement) throws SQLException {
		if(connection.isClosed()) {
			BetterPlayer.logInfo("Connection to database lost. Reconnecting!");
			connection = connect();
		}
		return preparedStatement.executeUpdate();
	}
	
	/**
	 * Create a PreparedStatement
	 * @param sql SQL statement to use for the PreparedStatement
	 * @return Returns the created PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement createPreparedStatement(String sql) throws SQLException {
		if(connection.isClosed()) {
			BetterPlayer.logInfo("Connection to database lost. Reconnecting!");
			connection = connect();
		}
		return connection.prepareStatement(sql);
	}
}
