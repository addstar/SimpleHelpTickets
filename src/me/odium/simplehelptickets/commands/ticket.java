package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.DBConnection;

import me.odium.simplehelptickets.Utilities;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ticket implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public ticket(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	String date;
	String uuid;
	String owner;
	String world;
	double locX;
	double locY;
	double locZ;
	double locP;
	double locF;
	String reply;
	String status;
	String admin;
	String adminreply;
	String expire;
	String userreply;

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	Connection con = null;
	java.sql.Statement stmt = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length == 0) {
			sender.sendMessage(plugin.GOLD + "[ SimpleHelpTickets ]");
			sender.sendMessage(plugin.getMessage("HelpMe_Line1"));
			sender.sendMessage(plugin.getMessage("HelpMe_Line2"));
		} else if (args.length > 0) {

			// Use the command name to determine if we are working with a ticket or an idea
			String targetTable = Utilities.GetTargetTableName(label);

			// Build the command string
			StringBuilder sb = new StringBuilder();
			for (String arg : args)
				sb.append(arg + " ");
			String[] temp = sb.toString().split(" ");
			String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
			sb.delete(0, sb.length());
			for (String details : temp2) {
				sb.append(details);
				sb.append(" ");
			}
			String details = sb.toString().trim();

			// Check for incomplete ticket / idea descriptions
			if (details.length() < 10 || details.indexOf(" ") < 0) {
				sender.sendMessage(plugin.getMessage("NotEnoughInformation"));
				return true;
			}

			if (player == null) {
				// SET VARIABLES FOR CONSOLE
				date = plugin.getCurrentDTG("date");
				uuid = "CONSOLE";
				owner = "CONSOLE";
				world = "NONE";
				locX = 00;
				locY = 00;
				locZ = 00;
				locP = 00;
				locF = 00;
				adminreply = "NONE";
				userreply = "NONE";
				status = "OPEN";
				admin = "NONE";
				expire = null;
			} else {
				// SET VARIABLES FOR PLAYER
				date = plugin.getCurrentDTG("date");
				uuid = player.getUniqueId().toString();
				owner = player.getName();
				world = player.getWorld().getName();
				locX = player.getLocation().getX();
				locY = player.getLocation().getY();
				locZ = player.getLocation().getZ();
				locP = player.getLocation().getPitch();
				locF = player.getLocation().getYaw();
				adminreply = "NONE";
				userreply = "NONE";
				status = "OPEN";
				admin = "NONE";
				expire = null;
			}

			int maxTickets = plugin.getConfig().getInt("MaxTickets"); // Get ticket limit from config
			int maxIdeas = plugin.getConfig().getInt("MaxIdeas");     // Get idea limit from config

			// REFERENCE CONNECTION AND ADD DATA
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				// MySQL
				// CHECK MAX TICKETS OR IDEAS
				try {
					con = plugin.mysql.getConnection();
					stmt = con.createStatement();
					rs = stmt.executeQuery("SELECT COUNT(uuid) AS itemTotal FROM " + targetTable + " WHERE uuid='" + uuid + "' AND status='OPEN'");
					rs.next(); // sets pointer to first record in result set
								// (NEED FOR MySQL)

					int itemTotal = rs.getInt("itemTotal");
					if (ItemLimitReached(itemTotal, targetTable, maxTickets, maxIdeas, sender)) {
						return true;
					}

				} catch (SQLException e) {
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

				// END CHECK MAX TICKETS

				try {
					con = plugin.mysql.getConnection();
					stmt = con.createStatement();
					PreparedStatement statement = con
							.prepareStatement("insert into " + targetTable + " (description, date, uuid, owner, world, x, y, z, p, f, adminreply, userreply, status, admin, expiration) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

					statement.setString(1, details);
					statement.setString(2, date);
					statement.setString(3, uuid);
					statement.setString(4, owner);
					statement.setString(5, world);
					statement.setDouble(6, locX);
					statement.setDouble(7, locY);
					statement.setDouble(8, locZ);
					statement.setDouble(9, locP);
					statement.setDouble(10, locF);
					statement.setString(11, adminreply);
					statement.setString(12, userreply);
					statement.setString(13, status);
					statement.setString(14, admin);
					statement.setString(15, expire);

					statement.executeUpdate();
					statement.close();

					NotifyItemOpened(sender, targetTable);

				} catch (SQLException e) {
					sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
					e.printStackTrace();
				} finally {
					try {
						if (rs != null) { rs.close(); rs = null; }
						if (stmt != null) { stmt.close(); stmt = null; }
					} catch (SQLException e) {
						System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
						e.printStackTrace();
					}
				}

			} else {
				// SQLite
				// CHECK MAX TICKETS OR IDEAS
				try {
					con = service.getConnection();
					stmt = con.createStatement();
					rs = stmt.executeQuery("SELECT COUNT(uuid) AS itemTotal FROM " + targetTable + " WHERE uuid='" + uuid + "' AND status='OPEN'");

					int itemTotal = rs.getInt("itemTotal");
					if (ItemLimitReached(itemTotal, targetTable, maxTickets, maxIdeas, sender)) {
						return true;
					}

				} catch (SQLException e) {
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
				// END CHECK MAX TICKETS

				try {
					con = service.getConnection();
					stmt = con.createStatement();
					PreparedStatement statement = con.prepareStatement("insert into " + targetTable + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
					// description, date, uuid, owner, world, x, y, z, p, f,
					// reply, status, admin

					statement.setString(2, details);
					statement.setString(3, date);
					statement.setString(4, uuid);
					statement.setString(5, owner);
					statement.setString(6, world);
					statement.setDouble(7, locX);
					statement.setDouble(8, locY);
					statement.setDouble(9, locZ);
					statement.setDouble(10, locP);
					statement.setDouble(11, locF);
					statement.setString(12, adminreply);
					statement.setString(13, userreply);
					statement.setString(14, status);
					statement.setString(15, admin);
					statement.setString(16, expire);

					statement.executeUpdate();
					statement.close();

					NotifyItemOpened(sender, targetTable);

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
			}
		}
		return true;
	}

	private boolean ItemLimitReached(int itemTotal, String targetTable, int maxTickets, int maxIdeas, CommandSender sender) {

		if (targetTable == Utilities.IDEA_TABLE_NAME) {
			if (itemTotal >= maxIdeas) {
				sender.sendMessage(plugin.getMessage("IdeaMax").replace("&arg", Integer.toString(maxIdeas)));
				return true;
			}
		}
		else if (itemTotal >= maxTickets) {
			sender.sendMessage(plugin.getMessage("TicketMax").replace("&arg", Integer.toString(maxTickets)));
			return true;
		}

		return false;
	}

	private void NotifyItemOpened(CommandSender sender, String targetTable) {

		String messageName;

		// Message player
		if (targetTable == Utilities.IDEA_TABLE_NAME)
			messageName = "IdeaOpen";
		else
			messageName = "TicketOpen";

		sender.sendMessage(plugin.getMessage(messageName));

		// Notify admins of new ticket
		if (targetTable == Utilities.IDEA_TABLE_NAME)
			messageName = "IdeaOpenADMIN";
		else
			messageName = "TicketOpenADMIN";

		String msg = plugin.getMessage(messageName).replace("%player", sender.getName());
		plugin.notifyAdmins(msg, sender);

	}
}