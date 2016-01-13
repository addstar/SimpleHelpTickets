package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class purgetickets implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public purgetickets(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	Statement stmt = null;
	Connection con = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		// Use the command name to determine if we are working with a ticket or an idea
		String targetTable = Utilities.GetTargetTableName(label, Arrays.asList("purgeideas"));

		if (args.length == 0) {

			if (targetTable == Utilities.IDEA_TABLE_NAME)
				sender.sendMessage(plugin.GRAY + "[SimpleHelpTickets] " + ChatColor.GOLD + plugin.expireIdeas() +   ChatColor.WHITE + " Expired ideas purged");
			else
				sender.sendMessage(plugin.GRAY + "[SimpleHelpTickets] " + ChatColor.GOLD + plugin.expireTickets() + ChatColor.WHITE + " Expired tickets purged");

			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("-c")) {
            // PURGE CLOSED TICKETS
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();
				stmt.executeUpdate("DELETE FROM " + targetTable + " WHERE status='" + "CLOSED" + "'");

				String messageName;
				if (targetTable == Utilities.IDEA_TABLE_NAME)
					messageName = "AllClosedIdeasPurged";
				else
					messageName = "AllClosedTicketsPurged";
				sender.sendMessage(plugin.getMessage(messageName));

			} catch (Exception e) {
				sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
			} finally {
				try {
					if (rs != null) { rs.close(); rs = null; }
					if (stmt != null) { stmt.close(); stmt = null; }
				} catch (SQLException e) {
					System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
					e.printStackTrace();
				}
			}
			return true;

		} else if (args.length == 1 && args[0].equalsIgnoreCase("-a")) {
			// PURGE ALL TICKETS
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					stmt.executeUpdate("TRUNCATE " + targetTable);
				} else {
					stmt.executeUpdate("DELETE FROM " + targetTable);
				}

				String messageName;
				if (targetTable == Utilities.IDEA_TABLE_NAME)
					messageName = "AllIdeasPurged";
				else
					messageName = "AllTicketsPurged";
				sender.sendMessage(plugin.getMessage(messageName));

			} catch (Exception e) {
				plugin.log.info("[SimpleHelpTickets] " + "Error: " + e);
			} finally {
				try {
					if (rs != null) { rs.close(); rs = null; }
					if (stmt != null) { stmt.close(); stmt = null; }
				} catch (SQLException e) {
					System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}
}
