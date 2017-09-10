package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class closeticket implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public closeticket(SimpleHelpTickets plugin) {
		this.plugin = plugin;
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

		if (args.length == 0) {
			sender.sendMessage(ChatColor.WHITE + "/close" + itemName + " <#>");
			sender.sendMessage(ChatColor.GRAY + "(reopen with /close" + itemName + " -r <#>)");
			return true;
		} else if (args.length == 1) {
			// CLOSING TICKET
			// CHECK TICKETNUMBER IS A DIGIT
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

			int id = Integer.parseInt(args[0]);

			boolean success = CloseItem(plugin, sender, targetTable, id);
			if(success)plugin.reminder.addResponse(sender);
			return success;

		} else if (args.length == 2 && args[0].equalsIgnoreCase("-r")) {

			// REOPENING A TICKET
			// CHECK TICKETNUMBER IS A DIGIT
			for (char c : args[1].toCharArray()) {
				if (!Character.isDigit(c)) {
					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
						messageName = "InvalidIdeaNumber";
					else
						messageName = "InvalidTicketNumber";

					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[1]));
					return true;
				}
			}

			int id = Integer.parseInt(args[1]);

			DBConnection service = DBConnection.getInstance();
			ResultSet rs = null;
			java.sql.Statement stmt = null;
			Connection con;

			// OPEN CONNECTION
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();
				// GET TICKET FROM DB
				rs = stmt.executeQuery("SELECT * FROM " + targetTable + " WHERE id='" + id + "'");
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					rs.next(); // sets pointer to first record in result set
				}
				// CHECK TICKET IS NOT ALREADY OPEN, IF SO END HERE.
				if (rs.getString("status").equalsIgnoreCase("OPEN")) {
					sender.sendMessage(plugin.getMessage("TicketNotClosed").replace("&arg", args[1]));
					return true;
				}
				// CHECK THE OWNER OF THE TICKET, AND GET CUSTOM OUTPUT FROM
				// CONFIG
				Player target = Bukkit.getPlayer(rs.getString("uuid"));
				// IF PLAYER IS CONSOLE OR ADMIN
				if (player == null || player.hasPermission("sht.admin")) {
					// SET THE ADMIN VARIABLE TO RELECT CONSOLE/ADMIN
					String admin = "ADMIN";
					if (player == null) {
						admin = "CONSOLE";
					} else if (rs.getString("uuid").contains(player.getUniqueId().toString()) || player.hasPermission("sht.admin")) {
						admin = player.getName();
					}
					// UPDATE THE TICKET
					stmt.executeUpdate("UPDATE " + targetTable + " SET status='" + "OPEN" + "', admin='" + admin + "', expiration=NULL WHERE id='" + id + "'");

					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
						messageName = "IdeaReopened";
					else
						messageName = "TicketReopened";
					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id).replace("&admin", admin));

					// IF TICKETOWNER IS ONLINE, TELL THEM OF CHANGES TO THEIR
					// TICKET
					if (target != null && target != player) {

						if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
							messageName = "IdeaReopenedOWNER";
						else
							messageName = "TicketReopenedOWNER";
						target.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id).replace("&admin", admin));
					}

					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
						messageName = "IdeaReopenedADMIN";
					else
						messageName = "TicketReopenedADMIN";

					// INFORM ADMINS OF CHANGES TO TICKET
					Collection<? extends Player> players = Bukkit.getOnlinePlayers();
					for (Player op : players) {
						if (op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose") && op != sender) {
							op.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id).replace("&admin", admin));
						}
					}
					return true;
				} else {
					sender.sendMessage(plugin.getMessage("NoPermission"));
					return true;
				}
			} catch (Exception e) {
				if (e.toString().contains("ResultSet closed")) {
					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
						messageName = "IdeaNotExist";
					else
						messageName = "TicketNotExist";

					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[1]));
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
		}
		return false;
	}

	public static boolean CloseItem(SimpleHelpTickets plugin, CommandSender sender, String targetTable, int id) {

		DBConnection service = DBConnection.getInstance();
		ResultSet rs = null;
		java.sql.Statement stmt = null;
		Connection con;

		String idText = String.valueOf(id);

		String messageName;
		String mailmessageName = null;
		String notExistMessageName;
		if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME)) {
			notExistMessageName = "IdeaNotExist";
		} else {
			notExistMessageName = "TicketNotExist";
		}

		// OPEN CONNECTION
		try {
			Player player = null;
			if (sender instanceof Player) {
				player = (Player) sender;
			}

			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				con = plugin.mysql.getConnection();
			} else {
				con = service.getConnection();
			}
			stmt = con.createStatement();

			// CHECK IF ITEM EXISTS
			rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM " + targetTable + " WHERE id='" + id + "'");
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				rs.next(); // sets pointer to first record in result set
			}
			if (rs.getInt("ticketTotal") == 0) {
				sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", idText));
				return true;
			}

			// GET ITEM FROM DB
			rs = stmt.executeQuery("SELECT * FROM " + targetTable + " WHERE id='" + id + "'");
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				rs.next(); // sets pointer to first record in result set
			}
			// CHECK ITEM STATUS IS NOT ALREADY CLOSED, IF SO END HERE.
			if (rs.getString("status").equalsIgnoreCase("CLOSED")) {

				if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
					messageName = "IdeaAlreadyClosed";
				else
					messageName = "TicketAlreadyClosed";

				sender.sendMessage(plugin.getMessage(messageName).replace("&arg", idText));
				return true;
			}
			// CHECK THE OWNER OF THE ITEM, AND GET CUSTOM OUTPUT FROM CONFIG
			// Player target = Bukkit.getPlayer(rs.getString("uuid"));
			String uuid = rs.getString("uuid");
			String owner = rs.getString("owner");

			// IF PLAYER IS CONSOLE, TICKET/ITEM OWNER, OR ADMIN
			if (player == null || uuid.equals(player.getUniqueId().toString()) || player.hasPermission("sht.admin")) {
				// SET THE ADMIN VARIABLE TO REFLECT CONSOLE/ADMIN
				String admin = "ADMIN";

				if (player == null) {
					admin = "CONSOLE";
				} else if (uuid.equals(player.getUniqueId().toString()) || player.hasPermission("sht.admin")) {
					admin = player.getName();
				}

				// GET EXPIRATION DATE
				String date = rs.getString("date");
				String expiration = plugin.getExpiration(date);

				// UPDATE THE ITEM
				stmt.executeUpdate("UPDATE " + targetTable + " SET status='" + "CLOSED" + "', admin='" + admin + "', expiration='" + expiration + "' WHERE id='" + id + "'");

				if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME)) {
					messageName = "IdeaClosed";
					mailmessageName = "IdeaClosedMail";
				}
				else{
					messageName = "TicketClosed";
					mailmessageName = "TicketClosedMail";
				}
				sender.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id));
				plugin.sendMailOnClose(sender,owner,plugin.getMessage(mailmessageName).replace("&arg", "" + id));
				if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME)) {
					messageName = "IdeaClosedADMIN";
					mailmessageName = "IdeaClosedMail";
				}
				else {
					messageName = "TicketClosedADMIN";
					mailmessageName = "TicketClosedMail";
				}
				String msg = plugin.getMessage(messageName).replace("&arg", "" + idText).replace("&admin", admin);
				plugin.sendMailOnClose(sender,owner,plugin.getMessage(mailmessageName).replace("&arg", "" + id));

				plugin.notifyAdmins(msg, null);

				// IF ITEM OWNER IS USER WHO CLOSED THE ITEM, LET THEM
				// KNOW OF THE CHANGE TO THEIR ITEM
				if (!uuid.equals(player.getUniqueId().toString())) {
					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
						messageName = "IdeaClosedOWNER";
					else
						messageName = "TicketClosedOWNER";

					msg = plugin.getMessage(messageName).replace("&arg", "" + idText).replace("&admin", admin);
					plugin.notifyUser(msg, owner);
				}
				return true;
			} else {
				if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
					messageName = "NotYourIdeaToClose";
				else
					messageName = "NotYourTicketToClose";
				sender.sendMessage(plugin.getMessage(messageName).replace("&arg", idText));
				return true;
			}
		} catch (Exception e) {
			if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
				messageName = "IdeaNotExist";
			else
				messageName = "TicketNotExist";

			if (e.toString().contains("ResultSet closed")) {
				sender.sendMessage(plugin.getMessage(messageName).replace("&arg", idText));
				return true;
			} else if (e.toString().contains("empty")) {
				sender.sendMessage(plugin.getMessage(messageName).replace("&arg", idText));
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
	}
}
