package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class checkticket implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public checkticket(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	java.sql.Statement stmt = null;
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

		if (args.length != 1) {
			sender.sendMessage(ChatColor.WHITE + "/check" + itemName + " <#>");
			return true;
		} else {

			for (char c : args[0].toCharArray()) {
				if (!Character.isDigit(c)) {
					if (targetTable == Utilities.IDEA_TABLE_NAME)
						messageName = "InvalidIdeaNumber";
					else
						messageName = "InvalidTicketNumber";

					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
					return true;
				}
			}

			int ticketNumber = Integer.parseInt(args[0]);
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();

				rs = stmt.executeQuery("SELECT * FROM " + targetTable + " WHERE id='" + ticketNumber + "'");
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					rs.next(); // sets pointer to first record in result set
				}

				if (player == null || player.hasPermission("sht.admin") || rs.getString("uuid").equals(player.getUniqueId().toString())) {
					String world = null;
					String strloc = null;
					String date;
					String expiration;

					String id = rs.getString("id");
					String owner = rs.getString("owner");

					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						date = new SimpleDateFormat("dd/MMM/yy HH:mm").format(rs.getTimestamp("date"));
					} else {
						date = rs.getString("date");
					}

					String admin = rs.getString("admin");
					String adminreply = rs.getString("adminreply");
					String userreply = rs.getString("userreply");
					if (plugin.getConfig().getBoolean("MultiWorld") == true) {
						world = rs.getString("world");
						strloc = "(" + rs.getInt("x") + ", " + rs.getInt("y") + ", " + rs.getInt("z") + ")";
					}
					String description = rs.getString("description");
					String status = rs.getString("status");

					// Capitalize itemName
					itemName = Utilities.Capitalize(itemName);

					sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + ChatColor.BOLD + itemName + " " + id + ChatColor.RESET + ChatColor.GOLD + " ]");
					sender.sendMessage(ChatColor.BLUE + " Owner: " + ChatColor.WHITE + owner);
					sender.sendMessage(ChatColor.BLUE + " Date: " + ChatColor.WHITE + date);
					if (plugin.getConfig().getBoolean("MultiWorld") == true) {
						sender.sendMessage(ChatColor.BLUE + " Location: " + ChatColor.WHITE + world + " " + ChatColor.GRAY + strloc);
					}
					if (status.contains("OPEN")) {
						sender.sendMessage(ChatColor.BLUE + " Status: " + ChatColor.GREEN + status);
					} else {
						sender.sendMessage(ChatColor.BLUE + " Status: " + ChatColor.RED + status);
					}
					sender.sendMessage(ChatColor.BLUE + " Assigned: " + ChatColor.WHITE + admin);
					sender.sendMessage(ChatColor.BLUE + " " + itemName + ": " + ChatColor.GOLD + description);
					if (adminreply.equalsIgnoreCase("NONE")) {
						sender.sendMessage(ChatColor.BLUE + " Staff Reply: " + ChatColor.WHITE + "(none)");
					} else {
						sender.sendMessage(ChatColor.BLUE + " Staff Reply: " + ChatColor.YELLOW + adminreply);
					}
					if (userreply.equalsIgnoreCase("NONE")) {
						sender.sendMessage(ChatColor.BLUE + " User Reply: " + ChatColor.WHITE + "(none)");
					} else {
						sender.sendMessage(ChatColor.BLUE + " User Reply: " + ChatColor.YELLOW + userreply);
					}
					// IF AN EXPIRATION HAS BEEN APPLIED
					if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
						if (rs.getTimestamp("expiration") != null) {
							expiration = new SimpleDateFormat("dd/MMM/yy HH:mm").format(rs.getTimestamp("expiration"));
							sender.sendMessage(ChatColor.BLUE + " Expiration: " + ChatColor.WHITE + expiration);
						}
					} else {
						if (rs.getTimestamp("expiration") != null) {
							expiration = rs.getString("expiration");
							sender.sendMessage(ChatColor.BLUE + " Expiration: " + ChatColor.WHITE + expiration);
						}
						// COMPARE STRINGS
						// int HasExpired = date.compareTo(expiration);
						// if (HasExpired >= 0) {
						// // plugin.log.info("ticket HAS expired!");
						// } else {
						// // plugin.log.info("ticket HAS NOT expired!");
						// }
					}
				}
			} catch (SQLException e) {
				if (e.toString().contains("empty result set.")) {
					if (targetTable == Utilities.IDEA_TABLE_NAME)
						messageName = "IdeaNotExist";
					else
						messageName = "TicketNotExist";
					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
					return true;
				} else {
					sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
					return true;
				}
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
	}
}
