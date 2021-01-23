package nl.thedutchmc.betterplayer.config.guild.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nl.thedutchmc.betterplayer.BetterPlayer;
import nl.thedutchmc.betterplayer.Utils;

public class SqlManager {

	private Connection connection;
	
	public SqlManager(String dbHost, String dbName, String dbUsername, String dbPassword) {
		BetterPlayer.logInfo("Initializing database connection...");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch(ClassNotFoundException e) {
			BetterPlayer.logError("Unable to initialize the database connection! MySQL driver not found");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			return;
		}
		
		BetterPlayer.logInfo("Connecting to database...");
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/" + dbName + "?user=" + dbUsername + "&password=" + dbPassword);
		} catch(SQLException e) {
			BetterPlayer.logError("Unable to establish connection to database!");
			BetterPlayer.logDebug(Utils.getStackTrace(e));
			return;
		}
	}
	
	public ResultSet executeFetchQuery(PreparedStatement preparedStatement) throws SQLException {
		return preparedStatement.executeQuery();
	}
	
	public int executePutQuery(PreparedStatement preparedStatement) throws SQLException {
		return preparedStatement.executeUpdate();
	}
	
	public PreparedStatement createPreparedStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}
}
