package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.rmi.CORBA.Util;

public class delticket implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public delticket(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	java.sql.Statement stmt = null;
	java.sql.Statement stmt2 = null;
	Connection con = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// Use the command name to determine if we are working with a ticket or an idea
		String targetTable = Utilities.GetTargetTableName(label);
		String itemName = Utilities.GetTargetItemName(targetTable);

		String messageName;
		String notExistMessageName;
		if (targetTable == Utilities.IDEA_TABLE_NAME) {
			messageName = "InvalidIdeaNumber";
			notExistMessageName = "IdeaNotExist";
		} else {
			messageName = "InvalidTicketNumber";
			notExistMessageName = "TicketNotExist";
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.WHITE + "/del" + itemName + " <#>");
			return true;
		} else if (args.length == 1) {

			for (char c : args[0].toCharArray()) {
				if (!Character.isDigit(c)) {
					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
					return true;
				}
			}

			itemName = Utilities.Capitalize(itemName);

			// CONSOLE COMMANDS
			if (player == null) {
				try {
					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						con = plugin.mysql.getConnection();
					} else {
						con = service.getConnection();
					}
					stmt = con.createStatement();
					// CHECK IF TICKET EXISTS
					rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM " + targetTable + " WHERE id='" + args[0] + "'");
					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						rs.next(); // sets pointer to first record in result set
					}
					if (rs.getInt("ticketTotal") == 0) {
						sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
						return true;
					}
					stmt.executeUpdate("DELETE FROM " + targetTable + " WHERE id='" + args[0] + "'");
					sender.sendMessage(plugin.GRAY + "[Tickets] " + plugin.WHITE + itemName + " " + ChatColor.GOLD + args[0] + ChatColor.WHITE + " Deleted");

					return true;

				} catch (Exception e) {
					if (e.toString().contains("ResultSet closed")) {
						sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
						return true;
					} else {
					}
					sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
					return true;
				} finally {
					try {
						if (rs != null) { rs.close(); rs = null; }
						if (stmt != null) { stmt.close(); stmt = null; }
						if (stmt2 != null) { stmt2.close(); stmt2 = null; }
					} catch (SQLException e) {
						System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
						e.printStackTrace();
					}
				}
				// PLAYER COMMANDS
			} else {
				try {
					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						con = plugin.mysql.getConnection();
					} else {
						con = service.getConnection();
					}
					stmt = con.createStatement();
					// CHECK IF TICKET EXISTS
					rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM " + targetTable + " WHERE id='" + args[0] + "'");
					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						rs.next(); // sets pointer to first record in result set
					}
					if (rs.getInt("ticketTotal") == 0) {
						sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
						return true;
					}
					rs.close();
					rs = stmt.executeQuery("SELECT * FROM " + targetTable + " WHERE id='" + args[0] + "'");
					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						rs.next(); // sets pointer to first record in result set
					}
					String playerUUID = player.getUniqueId().toString();
					if (!rs.getString("uuid").equals(playerUUID) && !player.hasPermission("sht.admin")) {
						sender.sendMessage(plugin.GRAY + "[SimpleHelpTickets] " + plugin.RED + itemName + " " + rs.getString("id") + " is not your ticket to delete.");
						return true;
					} else {
						stmt.executeUpdate("DELETE FROM " + targetTable + " WHERE id='" + args[0] + "'");
						sender.sendMessage(plugin.GRAY + "[Tickets] " + plugin.WHITE + itemName + " " + ChatColor.GOLD + args[0] + ChatColor.WHITE + " Deleted");
						return true;
					}
				} catch (Exception e) {
					sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
					return true;
				} finally {
					try {
						if (rs != null) { rs.close(); rs = null; }
						if (stmt != null) { stmt.close(); stmt = null; }
						if (stmt2 != null) { stmt2.close(); stmt2 = null; }
					} catch (SQLException e) {
						System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
}
