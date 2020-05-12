package com.programmerdan.minecraft.wordbank.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.programmerdan.minecraft.wordbank.WordBank;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Wrapper for Connection Pool, and holder for static strings.
 * 
 * @author ProgrammerDan
 */
public class WordBankData {
	public static final String init =
			"CREATE TABLE IF NOT EXISTS wordbank_utilization (" +
			"  id BIGINT NOT NULL AUTO_INCREMENT," +
			"  event TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
			"  wbkey VARCHAR(32) NOT NULL," +
			"  uuid VARCHAR(36) NOT NULL," +
			"  target VARCHAR(50) NOT NULL," +
			"  wbname VARCHAR(150) NOT NULL," +
			"  CONSTRAINT pk_wordbank PRIMARY KEY (id)," +
			"  INDEX pk_whatwhowhat USING BTREE (wbkey, uuid, target)" +
			");";
	
	public static final String insert =
			"INSERT INTO wordbank_utilization (wbkey, uuid, target, wbname) VALUES (?, ?, ?, ?);";
	
	public static final String keys = 
			"SELECT wbkey, wbname, count(*) AS cnt FROM wordbank_utilization GROUP BY wbkey LIMIT ? OFFSET ?;";
	
	public static final String key =
			"SELECT uuid, count(*) AS cnt, count(DISTINCT target) AS targets " +
			"  FROM wordbank_utilization WHERE wbkey = ? GROUP BY uuid LIMIT ? OFFSET ?;";
	
	public static final String getvalue =
			"SELECT wbkey, wbname AS val FROM wordbank_utilization WHERE wbkey = ? LIMIT 1;";
	
	private HikariDataSource datasource;
	
	private WordBank plugin;
	/**
	 * Sets up a new WordBank using the standing configuration; attempts to create the
	 * database if it doesn't already exist.
	 */
	public WordBankData() {
		this(null);
	}
	
	public WordBankData(WordBank plugin) {
		this.plugin = plugin;
		if (plugin().config().hasDB()) {
			this.datasource = new HikariDataSource(plugin().config().database());
			
			try {
				Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(WordBankData.init);
				statement.execute();
				statement.close();
				connection.close();
			} catch (SQLException se) {
				plugin().logger().log(Level.SEVERE, "Unable to initialize Database", se);
			}
		} else {
			this.datasource = null;
		}
	}
	
	protected WordBank plugin() {
		return this.plugin == null ? WordBank.instance() : this.plugin; 
	}
	
	public Connection getConnection() throws SQLException {
		available();
		return this.datasource.getConnection();
	}
	
	public void close() throws SQLException {
		available();
		this.datasource.close();
	}
	
	/**
	 * Quick test; either ends or throws an exception if data source isn't configured.
	 * @throws SQLException
	 */
	public void available() throws SQLException {
		if (this.datasource == null) {
			throw new SQLException("No Datasource Available");
		}
	}
}
