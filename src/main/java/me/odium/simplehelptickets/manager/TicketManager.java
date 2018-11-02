package me.odium.simplehelptickets.manager;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 18/07/2017.
 */
public class TicketManager {

    private final SimpleHelpTickets plugin;
    private Statement stmt;
    private Connection con;



    public TicketManager(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }
public void ShowAdminTickets(Player player){
    try {
        con = plugin.service.getConnection();
        stmt = con.createStatement();

        int ticketTotal = CountOpenItems(stmt, Utilities.TICKET_TABLE_NAME);
        if (ticketTotal > 0) {
            player.sendMessage(plugin.getMessage("AdminJoin").replace("&arg", ticketTotal+""));
        }

        int ideaTotal = CountOpenItems(stmt, Utilities.IDEA_TABLE_NAME);
        if (ideaTotal > 0) {
            player.sendMessage(plugin.getMessage("AdminJoinIdeas").replace("&arg", ideaTotal+""));
        }

    } catch(Exception e) {
        plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
    }
}


    private void ShowPlayerOpenTickets(Player player) {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            try {
                con = plugin.service.getConnection();
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );
                rs.next(); //sets pointer to first record in result set

                int ticketTotal = rs.getInt("ticketTotal");
                if (ticketTotal > 0) {
                    rs.close();
                    rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );
                    int openNumber = 0;
                    openNumber = findOpenNumber(player, rs, openNumber);
                    if (openNumber > 0) {
                        player.sendMessage(plugin.getMessage("UserJoin").replace("&arg", openNumber+""));
                    }
                }
                rs.close();
                stmt.close();
            } catch(Exception e) {
                plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
            }
        } else {
            try {
                con = plugin.service.getConnection();
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );

                int ticketTotal = rs.getInt("ticketTotal");
                if (ticketTotal == 0) {
                    // DO NOTHING
                    rs.close();
                    stmt.close();
                } else if(ticketTotal > 0) {
                    rs.close();
                    rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );
                    int openNumber = 0;

                    openNumber = findOpenNumber(player, rs, openNumber);
                    if (openNumber > 0) {
                        player.sendMessage(plugin.getMessage("UserJoin").replace("&arg", openNumber + ""));
                    }
                    rs.close();
                    stmt.close();
                }
            } catch (Exception e) {
                plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
            }
        }
    }

    private int findOpenNumber(Player player, ResultSet rs, int openNumber) throws SQLException {
        while (rs.next()) {
            String adminreply = rs.getString("adminreply");
            String status = rs.getString("status");
            if (status.equalsIgnoreCase("OPEN")) {
                openNumber++;
            }
            if (!adminreply.equalsIgnoreCase("NONE") && status.equalsIgnoreCase("OPEN")) {
                player.sendMessage(plugin.getMessage("UserJoin-TicketReplied"));
            }
        }
        return openNumber;
    }

    private int CountOpenItems(java.sql.Statement stmt, String targetTable) throws SQLException {

        ResultSet rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM " + targetTable + " WHERE status='"+"OPEN"+"'");

        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            rs.next(); //sets pointer to first record in result set
        }

        return rs.getInt("ticketTotal");

    }

    public void runOnJoin(Player player){
        if (player.hasPermission("sht.admin")) {
            boolean DisplayTicketAdmin = plugin.getConfig().getBoolean("OnJoin.DisplayTicketAdmin");
            if (DisplayTicketAdmin) {
                ShowAdminTickets(player);
            }
            // IF PLAYER IS USER
        } else {
            boolean DisplayTicketUser = plugin.getConfig().getBoolean("OnJoin.DisplayTicketUser");

            if (DisplayTicketUser) {
                ShowPlayerOpenTickets(player);
            }
        }
    }

}
