package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class checkticket implements CommandExecutor {

    private final SimpleHelpTickets plugin;

	public checkticket(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

    private ResultSet rs = null;
    private java.sql.Statement stmt = null;

    static boolean checkInvalidNumber(CommandSender sender, String[] args, String targetTable, SimpleHelpTickets plugin) {
        String messageName;
        for (char c : args[0].toCharArray()) {
            if (!Character.isDigit(c)) {
                if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
                    messageName = "InvalidIdeaNumber";
                else
                    messageName = "InvalidTicketNumber";

                sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
                return true;
            }
        }
        return false;
    }

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
			// Show syntax: /checkticket or /checkidea
			sender.sendMessage(ChatColor.WHITE + "/check" + itemName + " <#>");
			return true;
		} else {

            if (checkInvalidNumber(sender, args, targetTable, plugin)) return true;

			int ticketNumber = Integer.parseInt(args[0]);
			try {
				Connection con = plugin.databaseService.getConnection();
                ;
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
					if (plugin.getConfig().getBoolean("MultiWorld",false)) {
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
					if (plugin.getConfig().getBoolean("MultiWorld",false)) {
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
					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
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
                closeticket.closeResources(rs, stmt);
            }
			return true;
		}
	}
}
