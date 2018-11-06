package me.odium.simplehelptickets.database;

import me.odium.simplehelptickets.SimpleHelpTickets;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public abstract class Database {

	Connection connection;
    protected static int version = 1;
	public int lastUpdate;
    protected Logger log;

	Database(SimpleHelpTickets plugin) {

        this(plugin.getLogger());
    }

    Database(Logger log) {
        this.log = log;
    }

	public abstract Connection getConnection();
    
    public abstract void createTable() throws SQLException;

    public abstract boolean clearTable(Table tableName);

	public abstract void executeStatement(String query) throws SQLException;

	public abstract void open() throws SQLException, ClassNotFoundException;

	public abstract void close();
    
    protected void checkVersion() {
        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM version");
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
        }
    }

    public void update(int current) {
        if (current < version) {
            try {
                int updated = 0;
                switch (current) {
                    case 0:
                        for (Table table : Table.values()) {
                            String sql = "ALTER TABLE " + table.tableName + " ADD server varchar(30) DEFAULT null  NULL; ";
                            this.getConnection().createStatement().executeUpdate(sql);
                        }
                        String sql = "CREATE TABLE version (version int DEFAULT 0)";
                        this.getConnection().createStatement().executeUpdate(sql);
                        sql = "INSERT INTO VERSION (version) VALUES (1)";
                        this.getConnection().createStatement().executeUpdate(sql);
                        updated = 1;
                    case 1:
                    default:
                        break;
                }
                log.info("[SHT] Database Updated from " + current + " to Version " + updated);
            } catch (SQLException e) {

            }
        } else if (current > version) {
            log.warning("[SHT] Database is possibly from a newer version ...please check plugin");
        }
    }
}