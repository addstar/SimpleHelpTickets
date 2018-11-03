package me.odium.simplehelptickets.manager;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.database.Database;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 18/07/2017.
 */
public class TicketManager {

    private final SimpleHelpTickets plugin;

    public TicketManager(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

    public void ShowAdminTickets(Player player) {
        int total = getTickets(Utilities.TICKET_TABLE_NAME, null, Ticket.Status.OPEN).size();
        if (total > 0) {
            player.sendMessage(plugin.getMessage("AdminJoin").replace("&arg", total + ""));
        }
        int ideas = getTickets(Utilities.IDEA_TABLE_NAME, null, Ticket.Status.OPEN).size();
        if (ideas > 0) {
            player.sendMessage(plugin.getMessage("AdminJoinIdeas").replace("&arg", ideas + ""));
        }
    }

    private List<Ticket> getTickets(String table, Player player, Ticket.Status status) {
        String sql;
        if (player != null) {
            sql = "SELECT * FROM " + table + " WHERE uuid='" + player.getUniqueId().toString() + "' and status = '" + status.name() + "'";
        } else {
            sql = "SELECT * FROM " + table + " WHERE status = '" + status.name() + "'";
        }
        List<Ticket> tickets = new ArrayList<>();
        Database db = plugin.databaseService;
        Connection con = db.getConnection();
        if (con != null) {
            try (Statement statement = con.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                while (result.next()) {
                    tickets.add(getFromResultRow(result));
                }
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            plugin.log.warning("Unable to get Database Connection");
        }
        return tickets;
    }

    public boolean saveTicket(Ticket ticket, String table) {
        List<Ticket> t = new ArrayList<>();
        t.add(ticket);
        return saveTickets(t, table);
    }

    public boolean saveTickets(List<Ticket> tickets, String table) {
        Connection con = plugin.databaseService.getConnection();
        PreparedStatement updateSQL;
        PreparedStatement insertSQL;
        try {
            updateSQL = con.prepareStatement("Update " + table + " SET(description = ?,adminreply = ?,admin=?,userreply=?,status=?,owner=?)  WHERE id = ?");
            insertSQL = con.prepareStatement("INSERT INTO " + table + "(description,date,uuid,owner,world,x,y,z,p,f,adminreply,userreply,status,admin,expiration) VALUES ('?','?','?','?','?','?','?','?','?','?','?','?','?','?','?')");
            for (Ticket ticket : tickets) {
                if (ticket.getId() == null) {
                    insertSQL.setString(1, ticket.getDetails());
                    insertSQL.setDate(2, new Date(ticket.getCreated().getTime()));
                    insertSQL.setString(3, ticket.getOwnerUUID().toString());
                    Location loc = ticket.getLocation();
                    if (loc != null) {
                        insertSQL.setString(4, loc.getWorld().getName());
                        insertSQL.setDouble(5, loc.getX());
                        insertSQL.setDouble(6, loc.getY());
                        insertSQL.setDouble(7, loc.getZ());
                        insertSQL.setFloat(8, loc.getPitch());
                        insertSQL.setFloat(9, loc.getYaw());
                    } else {
                        insertSQL.setString(4, null);
                        insertSQL.setDouble(5, 0);
                        insertSQL.setDouble(6, 0);
                        insertSQL.setDouble(7, 0);
                        insertSQL.setFloat(8, 0);
                        insertSQL.setFloat(9, 0);
                    }
                    insertSQL.setString(10, ticket.getAdminReply());
                    insertSQL.setString(11, ticket.getUserReply());
                    insertSQL.setString(12, ticket.getStatus().name());
                    insertSQL.setString(13, ticket.getAdminName());
                    insertSQL.setTimestamp(14, new Timestamp(ticket.getExpirationDate().getTime()));
                    int r = insertSQL.executeUpdate();
                    if (r == 1) {
                        tickets.remove(ticket);

                    }
                } else {
                    updateSQL.setString(1, ticket.getDetails());
                    updateSQL.setString(2, ticket.getAdminReply());
                    updateSQL.setString(3, ticket.getAdminName());
                    updateSQL.setString(4, ticket.getUserReply());
                    updateSQL.setString(5, ticket.getStatus().name());
                    updateSQL.setString(6, ticket.getOwnerName());
                    int r = updateSQL.executeUpdate();
                    if (r == 1) {
                        tickets.remove(ticket);
                    }
                }

            }
            if (tickets.size() != 0) {
                return false;
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Ticket getFromResultRow(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        UUID uuid = UUID.fromString(result.getString("uuid"));
        World world = Bukkit.getWorld(result.getString("world"));
        Location location = new Location(world, result.getDouble("x"), result.getDouble("y"), result.getDouble("z"), result.getFloat("p"), result.getFloat("f"));
        String details = result.getString("description");
        Date date = result.getDate("date");
        Ticket ticket = new Ticket(id, uuid, location, details, date, null);
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
        return ticket;
    }

    private void showPlayerOpenTickets(Player player) {
        int ticket = getTickets(Utilities.TICKET_TABLE_NAME, player, Ticket.Status.OPEN).size();
        if (ticket > 0) {
            player.sendMessage(plugin.getMessage("UserJoin").replace("&arg", ticket + ""));
        }
    }

    public void runOnJoin(Player player){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (player.hasPermission("sht.admin")) {
                boolean DisplayTicketAdmin = plugin.getConfig().getBoolean("OnJoin.DisplayTicketAdmin");
                if (DisplayTicketAdmin) {
                    ShowAdminTickets(player);
                }
                // IF PLAYER IS USER
            } else {
                boolean DisplayTicketUser = plugin.getConfig().getBoolean("OnJoin.DisplayTicketUser");

                if (DisplayTicketUser) {
                    showPlayerOpenTickets(player);
                }
            }
        });

    }

    public List<Ticket> getTickets(String targetTable, String where, int maxRecords) {
        Connection con = plugin.databaseService.getConnection();
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
            plugin.log.warning(plugin.getMessage("Error").replace("&arg", e.getMessage()));
            e.printStackTrace();
            return results;
        }
        return results;
    }

    private String GetItemSelectQuery(String targetTable, String whereClause, int maxRecordsToReturn) {
        String innerQuery = "SELECT * FROM " + targetTable;
        if (!whereClause.isEmpty())
            innerQuery += " WHERE " + whereClause;

        innerQuery += " ORDER BY id DESC LIMIT " + maxRecordsToReturn;

        return "SELECT * FROM (" + innerQuery + ") AS SelectQ ORDER BY id ASC";
    }

    public int expireItems(String targetTable) {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        Connection con;
        int expirations = 0;
        Date dateNEW;
        Date expirationNEW;
        try {
            con = plugin.databaseService.getConnection();
            stmt = con.prepareStatement("SELECT * FROM " + targetTable);
            rs = stmt.executeQuery();
            stmt2 = con.prepareStatement("DELETE FROM " + targetTable + " WHERE id=?");
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
                    Date date = new Date(ticket.getCreated().getTime());
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
            plugin.getLogger().info("[SimpleHelpTickets] " + "Error: " + e);
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
}
