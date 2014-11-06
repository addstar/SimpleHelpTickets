package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.DBConnection;

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
			String details = sb.toString();

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

			// REFERENCE CONNECTION AND ADD DATA
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {

				// CHECK MAX TICKETS
				int maxTickets = plugin.getConfig().getInt("MaxTickets"); // Get ticket limit from config
				try {
					con = plugin.mysql.getConnection();
					stmt = con.createStatement();
					rs = stmt.executeQuery("SELECT COUNT(uuid) AS ticketTotal FROM SHT_Tickets WHERE uuid='" + uuid + "' AND status='OPEN'");
					rs.next(); // sets pointer to first record in result set
								// (NEED FOR MySQL)

					int ticketTotal = rs.getInt("ticketTotal"); // GET TOTAL
																// NUMBER OF
																// PLAYERS
																// TICKETS
					if (ticketTotal >= maxTickets) { // IF MAX TICKETS REACHED
						sender.sendMessage(plugin.getMessage("TicketMax").replace("&arg", Integer.toString(maxTickets)));
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
							.prepareStatement("insert into SHT_Tickets(description, date, uuid, owner, world, x, y, z, p, f, adminreply, userreply, status, admin, expiration) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

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

					// Message player and finish
					sender.sendMessage(plugin.getMessage("TicketOpen"));
					// Notify admin of new ticket
					String msg = plugin.getMessage("TicketOpenADMIN").replace("%player", sender.getName());
					plugin.notifyAdmins(msg, sender);
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

				// CHECK MAX TICKETS
				int maxTickets = plugin.getConfig().getInt("MaxTickets"); // Get ticket limit from config
				try {
					con = service.getConnection();
					stmt = con.createStatement();
					rs = stmt.executeQuery("SELECT COUNT(uuid) AS ticketTotal FROM SHT_Tickets WHERE uuid='" + uuid + "' AND status='OPEN'");

					int ticketTotal = rs.getInt("ticketTotal"); // Get total number of players tickets
					if (ticketTotal >= maxTickets) { // IF MAX TICKETS REACHED
						sender.sendMessage(plugin.getMessage("TicketMax").replace("&arg", Integer.toString(maxTickets)));
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
					PreparedStatement statement = con.prepareStatement("insert into SHT_Tickets values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
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

					// Message player and finish
					sender.sendMessage(plugin.getMessage("TicketOpen"));
					// Notify admin of new ticket
					String msg = plugin.getMessage("TicketOpenADMIN").replace("%player", sender.getName());
					plugin.notifyAdmins(msg, sender);
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
}