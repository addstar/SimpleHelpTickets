package me.odium.simplehelptickets.database;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.utilities.Utilities;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection extends Database {
	private final Statement stmt = null;

    SimpleHelpTickets plugin;

    public DBConnection(SimpleHelpTickets plugin) {
        super(plugin);
	}

	/**
	 * We set the plugin that is to be used for these connections.
	 * 
	 * @param plugin the Plugin
	 */
	public void setPlugin(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

    public void open() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		// con = DriverManager.getConnection("jdbc:sqlite:Tickets.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "Tickets.db");

	}

	public Connection getConnection() {
        return connection;
    }

    public void close() {
		try {
            connection.close();
		} catch (Exception ignore) {
		}
	}
	
	public void createTable() throws SQLException {
		Statement stmt;
            stmt = connection.createStatement();

			String columnList = " (id INTEGER PRIMARY KEY, description text, date datetime, "
					+ "uuid varchar(36), owner varchar(20), world varchar(30), x double(30,20), y double(30,20), z double(30,20), "
					+ "p double(30,20), f double(30,20), adminreply text, userreply text, status varchar(16), "
					+ "admin varchar(30), expiration timestamp, server varchar(30))";
			
			String queryTickets =
					"CREATE TABLE IF NOT EXISTS " + TicketManager.getTableName("ticket") + " " + columnList;
			stmt.executeUpdate(queryTickets);
			
			String queryIdeas =
					"CREATE TABLE IF NOT EXISTS " + TicketManager.getTableName("idea") + " " + columnList;
			stmt.executeUpdate(queryIdeas);

			stmt.close();
	}

	@Override
	public boolean clearTable(Table table) {
		try {
			this.getConnection().createStatement().executeQuery("DELETE * FROM" + table.tableName);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setStatement() throws Exception {
        if (connection == null) {
            open();
        }
        Statement stmt = connection.createStatement();
		int timeout = 30;
		stmt.setQueryTimeout(timeout); // set timeout to 30 sec.
	}

	public Statement getStatement() {
		return stmt;
	}

    public void executeStatement(String instruction) throws SQLException {
		stmt.executeUpdate(instruction);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone is not allowed.");
	}
}