package me.odium.simplehelptickets.database;

import me.odium.simplehelptickets.SimpleHelpTickets;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public abstract class Database {

	Connection connection;
	protected SimpleHelpTickets plugin;
    protected static int version = 1;
	public int lastUpdate;
    private Logger log;

	Database(SimpleHelpTickets plugin) {
        
        this(plugin, plugin.getLogger());
    }
    
    Database(SimpleHelpTickets plugin, Logger log) {
        this.plugin = plugin;
        this.log = log;
    }

	public abstract Connection getConnection();

	public abstract void createTable();

	public abstract boolean clearTable(String tableName);

	public abstract void executeStatement(String query) throws SQLException;

	public abstract void open() throws SQLException, ClassNotFoundException;

	public abstract void close();
    
    protected void checkVersion() {
        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM VERSION");
            rs.next();
            int dbVersion = rs.getInt(1);
            if (dbVersion == version) {
                log.info("MYSQL database is up to date");
            } else {
                update(dbVersion);
            }
        } catch (SQLException e) {
            //assume no version hence current is 0
            update(0);
            e.printStackTrace();
            
        }
    }
    
    public abstract void update(int current);
	
}