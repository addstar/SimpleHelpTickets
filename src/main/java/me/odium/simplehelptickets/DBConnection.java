package me.odium.simplehelptickets;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	private static DBConnection instance = new DBConnection();
	public Connection con = null;
	public int Timeout = 30;
	public Statement stmt = null;

	private SimpleHelpTickets plugin;

	private DBConnection() {
	}

	public synchronized static DBConnection getInstance() {
		return instance;
	}

	/**
	 * We set the plugin that is to be used for these connections.
	 * 
	 * @param plugin the Plugin
	 */
	public void setPlugin(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	public void setConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		// con = DriverManager.getConnection("jdbc:sqlite:Tickets.db");
		con = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "Tickets.db");

	}

	public Connection getConnection() {
		return con;
	}

	public void closeConnection() {
		try {
			con.close();
		} catch (Exception ignore) {
		}
	}

	public void createTable() {
		Statement stmt;
		try {
			stmt = con.createStatement();

			String columnList = " (id INTEGER PRIMARY KEY, description text, date datetime, "
					+ "uuid varchar(36), owner varchar(20), world varchar(30), x double(30,20), y double(30,20), z double(30,20), "
					+ "p double(30,20), f double(30,20), adminreply text, userreply text, status varchar(16), "
					+ "admin varchar(30), expiration timestamp)";

			String queryTickets = "CREATE TABLE IF NOT EXISTS " + Utilities.TICKET_TABLE_NAME + " " + columnList;
			stmt.executeUpdate(queryTickets);

			String queryIdeas = "CREATE TABLE IF NOT EXISTS " + Utilities.IDEA_TABLE_NAME + " " + columnList;
			stmt.executeUpdate(queryIdeas);

			stmt.close();
		} catch (Exception e) {
			plugin.log.info("[Tickets] SQLite createTable Error: " + e);
		}
	}

	public void setStatement() throws Exception {
		if (con == null) {
			setConnection();
		}
		Statement stmt = con.createStatement();
		stmt.setQueryTimeout(Timeout); // set timeout to 30 sec.
	}

	public Statement getStatement() {
		return stmt;
	}

	public void executeStmt(String instruction) throws SQLException {
		stmt.executeUpdate(instruction);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone is not allowed.");
	}
}