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

public class tickets implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public tickets(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	java.sql.Statement stmt = null;
	Connection con = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		boolean atConsole = true;

		if (sender instanceof Player) {
			player = (Player) sender;
			atConsole = false;
		}

		int maxRecordsToReturn;
		if (atConsole)
			maxRecordsToReturn = 500;
		else
			maxRecordsToReturn = 100;

		if (player == null || player.hasPermission("sht.admin")) {
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();

				boolean verboseMode = false;
				boolean allTickets = false;
				boolean allClosed = false;

				for (int i = 0; i < args.length; i++) {
					if (args[i].equalsIgnoreCase("-v"))
						verboseMode = true;

					if (args[i].equalsIgnoreCase("-a"))
						allTickets = true;

					if (args[i].equalsIgnoreCase("-c"))
						allClosed = true;
				}

				if (args.length > 0 && !(verboseMode || allTickets || allClosed)) {
					sender.sendMessage(plugin.GOLD + "[Supported arguments]");
					sender.sendMessage(plugin.GREEN + "/tickets      " + plugin.WHITE + " - Open tickets");
					sender.sendMessage(plugin.GREEN + "/tickets -v   " + plugin.WHITE + " - Open tickets (verbose)");
					sender.sendMessage(plugin.GREEN + "/tickets -a   " + plugin.WHITE + " - All tickets (most recent " + maxRecordsToReturn + ")");
					sender.sendMessage(plugin.GREEN + "/tickets -c   " + plugin.WHITE + " - Closed tickets (most recent " + maxRecordsToReturn + ")");
					return true;
				}

				if (args.length == 0 || args.length == 1 && verboseMode) {
					// DISPLAY OPEN TICKETS
					rs = stmt.executeQuery(GetTicketSelectQuery("status='OPEN'", maxRecordsToReturn));
					int ticketsFound = 0;
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Open Tickets" + ChatColor.RESET + plugin.GOLD + " ]");

					while (rs.next()) {
						ticketsFound++;

						Utilities.ShowTicketInfo(sender, rs, verboseMode);
					}

					if (ticketsFound == 0) {
						sender.sendMessage(plugin.getMessage("NoTickets"));
						return true;
					} else {
						if (ticketsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " open tickets");
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " most recent open tickets; filter with /findtickets");
					}

					return true;

				} else if (allTickets) {
					// DISPLAY ALL TICKETS
					rs = stmt.executeQuery(GetTicketSelectQuery("", maxRecordsToReturn));
					int ticketsFound = 0;
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "All Tickets" + ChatColor.RESET + plugin.GOLD + " ]");

					while (rs.next()) {
						ticketsFound++;
						Utilities.ShowTicketInfo(sender, rs, verboseMode);
					}

					if (ticketsFound == 0) {
						sender.sendMessage(plugin.getMessage("NoTickets"));
						return true;
					} else {
						if (ticketsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " tickets");
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " most recent tickets; filter with /findtickets");
					}

					return true;

				} else if (allClosed) {
					// DISPLAY CLOSED TICKETS
					rs = stmt.executeQuery(GetTicketSelectQuery("status='CLOSED'", maxRecordsToReturn));
					int ticketsFound = 0;
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Closed Tickets" + ChatColor.RESET + plugin.GOLD + " ]");

					while (rs.next()) {
						ticketsFound++;
						Utilities.ShowTicketInfo(sender, rs, verboseMode);
					}

					if (ticketsFound == 0) {
						sender.sendMessage(plugin.getMessage("NoTickets"));
						return true;
					} else {
						if (ticketsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " tickets");
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " most recent closed tickets; filter with /findtickets");
					}
					return true;

				} else {
					return false;
				}

			} catch (Exception e) {
				sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
				return true;
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}
				} catch (SQLException e) {
					System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
					e.printStackTrace();
				}
			}
		} else {
			// DISPLAY USER TICKETS
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();
				rs = stmt.executeQuery(GetTicketSelectQuery("uuid='" + player.getUniqueId().toString() + "'", maxRecordsToReturn));
				int ticketsFound = 0;
				sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Your Tickets" + ChatColor.RESET + plugin.GOLD + " ]");

				while (rs.next()) {
					ticketsFound++;

					Utilities.ShowTicketInfo(sender, rs, false);
				}

				if (ticketsFound == 0) {
					sender.sendMessage(plugin.getMessage("NoTickets"));
				} else {
					sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " tickets");
				}
				return true;

			} catch (Exception e) {
				sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
				return true;
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}
				} catch (SQLException e) {
					System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
					e.printStackTrace();
				}
			}
		}

	}

	private String GetTicketSelectQuery(String whereClause, int maxRecordsToReturn) {
		String innerQuery = "SELECT * FROM SHT_Tickets";
		if (!whereClause.isEmpty())
			innerQuery += " WHERE " + whereClause;

		innerQuery += " ORDER BY id DESC LIMIT " + maxRecordsToReturn;

		return "SELECT * FROM (" + innerQuery + ") AS SelectQ ORDER BY id ASC";
	}

}
