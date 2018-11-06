package me.odium.simplehelptickets.manager;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.database.Database;
import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.objects.Pair;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.objects.TicketLocation;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Date;
import java.util.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import static java.sql.Types.TIMESTAMP;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 18/07/2017.
 */
public class TicketManager {

    public static Map<String, String> tableNames = new HashMap<>();

    static {
        tableNames.put("idea", "SHT_Ideas");
        tableNames.put("ticket", "SHT_Tickets");
    }
    
    private final Database database;
    private final Logger log;
    
    public TicketManager(SimpleHelpTickets plugin) {
        this(plugin.databaseService, plugin.getLogger());
    }
    
    public TicketManager(Database database, Logger log) {
        this.database = database;
        this.log = log;
    }
    
    public static Table getTableName(String identifier) {
        return Table.matchIdentifier(identifier);
    }

    public static Table getTargetItemName(String targetTable) {
        return Table.matchTableName(targetTable);
    }

    public static Table getTableFromCommandString(String commandString) {
        if (commandString.toLowerCase().contains("idea"))
            return Table.matchIdentifier("idea");
        else
            return Table.matchIdentifier("ticket");
    }
    
    public boolean createTables() {
        try {
            database.createTable();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void ShowAdminTickets(Player player) {
        int total = getTickets(Table.TICKET, null, Ticket.Status.OPEN).size();
        if (total > 0) {
            player.sendMessage(SimpleHelpTickets.instance.getMessage("AdminJoin").replace("&arg", total + ""));
        }
        int ideas = getTickets(Table.IDEA, null, Ticket.Status.OPEN).size();
        if (ideas > 0) {
            player.sendMessage(SimpleHelpTickets.instance.getMessage("AdminJoinIdeas").replace("&arg", ideas + ""));
        }
    }
    
    /**
     * @param table
     * @param status
     *
     * @return num rows deleted
     */
    public int deleteTickets(Table table, Ticket.Status status) {
        String sql = "DELETE FROM " + table.tableName + " WHERE status=?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, status.name());
            int integer = statement.executeUpdate();
            return integer;
        } catch (SQLException e) {
            log.warning("[DELETE TICKETS ERROR]:" + e.getMessage());
        }
        return 0;
    }

    public boolean saveTicket(Ticket ticket, Table table) {
        List<Ticket> t = new ArrayList<>();
        t.add(ticket);
        return saveTickets(t, table);
    }

    /**
     * Best to run this async - as if there is any delay or a lot of tickets to save then it could
     * lock up the main thread too long.
     *
     * @param tickets
     * @param table
     *
     * @return true if saved.
     */
    public boolean saveTickets(List<Ticket> tickets, Table table) {
        Connection con = database.getConnection();
        PreparedStatement updateSQL;
        PreparedStatement insertSQL;
        try {
            updateSQL = con.prepareStatement("UPDATE " + table.tableName + " SET description = ?,adminreply = ?,admin=?,userreply=?,status=?,owner=?  WHERE id = ?");
            String sql = "INSERT INTO " + table.tableName + "(description,date,uuid,owner,world,x,y,z,p,f,adminreply,userreply,status,admin,expiration) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            insertSQL = con.prepareStatement(sql);
            int done = 0;
        
            for (Ticket ticket : tickets) {
                if (ticket.getId() == null) {
                    insertSQL.setString(1, ticket.getDescription());
                    insertSQL.setDate(2, ticket.getCreatedDate());
                    insertSQL.setString(3, ticket.getOwner().toString());
                    insertSQL.setString(4, ticket.getOwnerName());
                    TicketLocation loc = ticket.getLocation();
                    if (loc != null) {
                        insertSQL.setString(5, loc.getWorld());
                        insertSQL.setDouble(6, loc.getX());
                        insertSQL.setDouble(7, loc.getY());
                        insertSQL.setDouble(8, loc.getZ());
                        insertSQL.setFloat(9, loc.getPitch());
                        insertSQL.setFloat(10, loc.getYaw());
                    } else {
                        insertSQL.setString(5, "NONE");
                        insertSQL.setDouble(6, 0D);
                        insertSQL.setDouble(7, 0D);
                        insertSQL.setDouble(8, 0D);
                    
                        insertSQL.setFloat(9, 0F);
                        insertSQL.setFloat(10, 0F);
                    }
                    insertSQL.setString(11, ticket.getAdminReply());
                    insertSQL.setString(12, ticket.getUserReply());
                    insertSQL.setString(13, ticket.getStatus().name());
                    insertSQL.setString(14, ticket.getAdmin());
                    Timestamp expirationDate = ticket.getExpirationDate();
                    if (expirationDate == null) {
                        insertSQL.setNull(15, TIMESTAMP);
                    } else {
                        insertSQL.setTimestamp(15, expirationDate);
                    }
                    int r = insertSQL.executeUpdate();
                    if (r == 1) {
                        done++;
                    }
                } else {
                    updateSQL.setString(1, ticket.getDescription());
                    updateSQL.setString(2, ticket.getAdminReply());
                    updateSQL.setString(3, ticket.getAdmin());
                    updateSQL.setString(4, ticket.getUserReply());
                    updateSQL.setString(5, ticket.getStatus().name());
                    updateSQL.setString(6, ticket.getOwnerName());
                    updateSQL.setInt(7, ticket.getId());
                    int r = updateSQL.executeUpdate();
                    if (r == 1) {
                        done++;
                    }
                }
            
            }
            if (tickets.size() != done) {
                return false;
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public int deleteTicketbyId(Table table, Integer id) {
        String sql = "DELETE FROM " + table.tableName + " WHERE id=?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            int integer = statement.executeUpdate();
            return integer;
        } catch (SQLException e) {
            log.warning("[DELETE TICKETS ERROR]:" + e.getMessage());
        }
        return 0;
    }
    
    /**
     * This will always return true but will log an error to the log if saving was unsuccessful.
     *
     * @param tickets
     * @param table
     *
     * @return
     */
    public boolean saveTicketsAsync(List<Ticket> tickets, Table table) {
        Bukkit.getScheduler().runTaskAsynchronously(SimpleHelpTickets.instance, () -> {
                    if (!saveTickets(tickets, table))
                        log.warning("[SHT] Error Saving Tickets");
                }
        );
        return true;
    }
    
    public Pair<Integer, Timestamp> getTicketCount(Player player, Table table, Ticket.Status status, Integer ticketId) {
        String where = "";
        int param = 1;
        int playerIndex = 0;
        int statusIndex = 0;
        int idIndex = 0;
        if (player != null) {
            where += "uuid like ?";
            playerIndex = param;
            param++;
            
        }
        if (status != null) {
            if (param > 1) where += " AND ";
            where += "status like ?";
            statusIndex = param;
            param++;
            
        }
        if (ticketId != null) {
            if (param > 1) where += " AND ";
            where += "id = ?";
            idIndex = param;
        }
        
        String sql = "SELECT COUNT(uuid) AS itemTotal, MAX(UNIX_TIMESTAMP(date)) AS newestItem " +
                "FROM " + table.tableName;
        if (param > 1) sql += " WHERE " + where;
        String uuidString = null;
        String statusString = null;
        if (player != null)
            uuidString = player.getUniqueId().toString();
        if (status != null)
            statusString = status.name();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            if (playerIndex > 0) statement.setString(playerIndex, uuidString);
            if (statusIndex > 0) statement.setString(statusIndex, statusString);
            if (idIndex > 0) statement.setInt(idIndex, ticketId);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return new Pair<>(rs.getInt(1), rs.getTimestamp(2));
        } catch (SQLException e) {
            log.warning(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void runOnJoin(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(SimpleHelpTickets.instance, () -> {
            if (player.hasPermission("sht.admin")) {
                boolean DisplayTicketAdmin = SimpleHelpTickets.instance.getConfig().getBoolean("OnJoin.DisplayTicketAdmin");
                if (DisplayTicketAdmin) {
                    ShowAdminTickets(player);
                }
                // IF PLAYER IS USER
            } else {
                boolean DisplayTicketUser = SimpleHelpTickets.instance.getConfig().getBoolean("OnJoin.DisplayTicketUser");
                
                if (DisplayTicketUser) {
                    showPlayerOpenTickets(player);
                }
            }
        });
        
    }
    
    private void showPlayerOpenTickets(Player player) {
        int ticket = getTickets(Table.TICKET, player, Ticket.Status.OPEN).size();
        if (ticket > 0) {
            player.sendMessage(SimpleHelpTickets.instance.getMessage("UserJoin").replace("&arg", ticket + ""));
        }
    }
    
    public List<Ticket> getTickets(Table table, Player player, Ticket.Status status) {
        String where;
        int param;
        if (player != null) {
            where = "uuid = ? AND status = ?";
            param = 2;
        } else {
            where = " status = ?";
            param = 1;
        }
        String sql = "SELECT * FROM " + table.tableName + " WHERE " + where;
        List<Ticket> tickets = new ArrayList<>();
        Connection con = database.getConnection();
        if (con != null) {
            try {
                PreparedStatement statement = con.prepareStatement(sql);
                if (param == 1) statement.setString(2, status.name());
                if (param > 1) {
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setString(2, status.name());
                }
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    tickets.add(getFromResultRow(result));
                }
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            log.warning("Unable to get Database Connection");
        }
        return tickets;
    }

    private Ticket getFromResultRow(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        UUID uuid = UUID.fromString(result.getString("uuid"));
        World world = null;
        try {
            world = Bukkit.getWorld(result.getString("world"));
        } catch (NullPointerException ignored) {
        }
        String server = result.getString("server");
        Location location;
        TicketLocation tL;
        if (world == null) {
            tL = new TicketLocation(result.getDouble("x"),
                    result.getDouble("y"),
                    result.getDouble("z"),
                    result.getString("world"),
                    result.getFloat("p"),
                    result.getFloat("f"),
                    server);
        } else {
            location = new Location(world, result.getDouble("x"), result.getDouble("y"), result.getDouble("z"), result.getFloat("p"), result.getFloat("f"));
            tL = new TicketLocation(location, server);
        }
        String details = result.getString("description");
        Date date = result.getDate("date");
        Ticket ticket = new Ticket(id, uuid, details, date, tL);
        String ownerName = result.getString("owner");
        ticket.setOwnerName(ownerName);
        ticket.setAdminReply(result.getString("adminreply"));
        ticket.setUserReply(result.getString("userreply"));
        Ticket.Status s;
        try {
            s = Ticket.Status.valueOf(result.getString("status"));
        } catch (IllegalArgumentException e) {
            s = Ticket.Status.OPEN;
        }
        ticket.setStatus(s);
        ticket.setExpirationDate(result.getTimestamp("expiration"));
        return ticket;
    }

    public List<Ticket> getTickets(Table targetTable, String where, int maxRecords) {
        Connection con = database.getConnection();
        List<Ticket> results = new ArrayList<>();
        try {
            PreparedStatement s = con.prepareStatement(GetItemSelectQuery(targetTable, where, maxRecords));
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                results.add(getFromResultRow(rs));
            }
            s.close();
            con.close();
        } catch (SQLException e) {
            log.warning(SimpleHelpTickets.instance.getMessage("Error").replace("&arg", e.getMessage()));
            e.printStackTrace();
            return results;
        }
        return results;
    }

    private String GetItemSelectQuery(Table table, String whereClause, int maxRecordsToReturn) {
        String innerQuery = "SELECT * FROM " + table.tableName;
        if (!whereClause.isEmpty())
            innerQuery += " WHERE " + whereClause;

        innerQuery += " ORDER BY id DESC LIMIT " + maxRecordsToReturn;

        return "SELECT * FROM (" + innerQuery + ") AS SelectQ ORDER BY id ASC";
    }

    public int expireItems(Table table) {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        Connection con;
        int expirations = 0;
        Date dateNEW;
        Date expirationNEW;
        try {
            con = database.getConnection();
            stmt = con.prepareStatement("SELECT * FROM " + table.tableName);
            rs = stmt.executeQuery();
            stmt2 = con.prepareStatement("DELETE FROM " + table.tableName + " WHERE id=?");
            while (rs.next()) {
                Ticket ticket = getFromResultRow(rs);
                Date exp = null;
                if (ticket.getExpirationDate() != null) {
                    exp = new Date(ticket.getExpirationDate().getTime());
                }
                Integer id = ticket.getId();
                // IF AN EXPIRATION HAS BEEN APPLIED
                if (exp != null) {
                    // CONVERT DATE-STRINGS FROM DB TO DATES
                    Date date = ticket.getCreatedDate();
                    Date expiration = new Date(ticket.getExpirationDate().getTime());
                    dateNEW = date;
                    expirationNEW = expiration;

                    // COMPARE STRINGS
                    int HasExpired = dateNEW.compareTo(expirationNEW);
                    if (HasExpired >= 0) {
                        expirations++;
                        stmt2.setInt(1, id);
                        stmt2.executeUpdate();
                    }
                }
            }
            // return expirations;
        } catch (Exception e) {
            log.info("[SimpleHelpTickets] " + "Error: " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (SQLException e) {
                System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
                e.printStackTrace();
            }
        }
        return expirations;
    }

    public List<Ticket> findTickets(Table table,
                                    String ticketOwner,
                                    String staffName,
                                    boolean mostRecentTimeDefined,
                                    boolean dateRangeDefined,
                                    SimpleDateFormat dateFormatter,
                                    String startDate,
                                    String endDate,
                                    Calendar cal,
                                    String searchPhrase,
                                    String sortDirection,
                                    Integer ticketsToShow,
                                    Integer ticketIDStart,
                                    Integer ticketIDEnd

    ) {
        List<Ticket> results = new ArrayList<>();
        long recentTimeStartMillisec = System.currentTimeMillis() - 86400 * 1000;
        String sqlStatement = "SELECT * FROM " + table.tableName +
                " WHERE id >= ? AND id <= ? AND owner LIKE ? AND admin LIKE ? AND " +
                " (description LIKE ? OR userreply LIKE ? OR adminreply LIKE ?) AND ";
        String recentTimeStartDate = Utilities.DateToString(recentTimeStartMillisec, dateFormatter);

        if (dateRangeDefined) {


            if ((startDate.contains("00:00:00") && endDate.contains("00:00:00"))) {
                // Start and end dates do not contain a time component
                // Add 24 hours to the end date

                java.util.Date parsedDate = Utilities.parseDate(endDate);

                cal.setTime(parsedDate);
                cal.add(Calendar.DATE, 1);

                endDate = Utilities.DateToString(cal, dateFormatter);
            }

            if (dateRangeDefined && mostRecentTimeDefined)
                sqlStatement += "(date BETWEEN '" + startDate + "' AND '" + endDate + "' AND " +
                        " date >= '" + recentTimeStartDate + "') ";
            else
                sqlStatement += "date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
        } else {
            sqlStatement += "date >= '" + recentTimeStartDate + "' ";
        }

        sqlStatement += "ORDER BY id " + sortDirection + " LIMIT " + ticketsToShow + ";";
        try {
            PreparedStatement statement = database.getConnection().prepareStatement(sqlStatement);
            statement.setString(1, Utilities.NumToString(ticketIDStart));
            statement.setString(2, Utilities.NumToString(ticketIDEnd));
            statement.setString(3, ticketOwner);
            statement.setString(4, staffName);
            statement.setString(5, searchPhrase);
            statement.setString(6, searchPhrase);
            statement.setString(7, searchPhrase);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                results.add(getFromResultRow(set));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;

    }
}
