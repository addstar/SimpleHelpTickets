package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

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

		// Use the command name to determine if we are working with a ticket or an idea
		String targetTable = Utilities.GetTargetTableName(label);
		String itemNamePlural = Utilities.GetTargetItemName(targetTable) + "s";

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

				for (String arg : args) {
					if (arg.equalsIgnoreCase("-v"))
						verboseMode = true;

					if (arg.equalsIgnoreCase("-a"))
						allTickets = true;

					if (arg.equalsIgnoreCase("-c"))
						allClosed = true;
				}

				if (args.length > 0 && !(verboseMode || allTickets || allClosed)) {
					sender.sendMessage(plugin.GOLD + "[Supported arguments]");
					sender.sendMessage(plugin.GREEN + "/tickets      " + plugin.WHITE + " - Open tickets");
					sender.sendMessage(plugin.GREEN + "/tickets -v   " + plugin.WHITE + " - Open tickets (verbose)");
					sender.sendMessage(plugin.GREEN + "/tickets -a   " + plugin.WHITE + " - All tickets (most recent " + maxRecordsToReturn + ")");
					sender.sendMessage(plugin.GREEN + "/tickets -c   " + plugin.WHITE + " - Closed tickets (most recent " + maxRecordsToReturn + ")");
					sender.sendMessage(plugin.GREEN + "For submitted ideas use /ideas");
					return true;
				}

				if (args.length == 0 || args.length == 1 && verboseMode) {
					// DISPLAY OPEN TICKETS OR IDEAS
					rs = stmt.executeQuery(GetItemSelectQuery(targetTable, "status='OPEN'", maxRecordsToReturn));
					int itemsFound = 0;

					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Open " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

					while (rs.next()) {
						itemsFound++;

						Utilities.ShowTicketInfo(sender, rs, verboseMode);
					}

					if (itemsFound == 0) {
						ReportNoItems(sender, targetTable);
					} else {
						if (itemsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " open " + Utilities.CheckPlural(itemNamePlural, itemsFound));
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " most recent open " + itemNamePlural + "; filter with /find" + itemNamePlural);
					}

					if (itemNamePlural.contentEquals("tickets")) {
						// Also report the number of open ideas

						ResultSet rsIdeas = stmt.executeQuery(GetItemSelectQuery(Utilities.IDEA_TABLE_NAME, "status='OPEN'", maxRecordsToReturn));
						int ideasFound = 0;

						while (rsIdeas.next()) {
							ideasFound++;
						}

						if (ideasFound > 0) {
							sender.sendMessage(plugin.BLUE + Utilities.NumToString(ideasFound) + " open " + Utilities.CheckPlural("ideas", ideasFound));
						}
					}

					return true;

				} else if (allTickets) {
					// DISPLAY ALL TICKETS OR IDEAS
					rs = stmt.executeQuery(GetItemSelectQuery(targetTable, "", maxRecordsToReturn));
					int itemsFound = 0;
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "All " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

					while (rs.next()) {
						itemsFound++;
						Utilities.ShowTicketInfo(sender, rs, verboseMode);
					}

					if (itemsFound == 0) {
						ReportNoItems(sender, targetTable);
					} else {
						if (itemsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " " + Utilities.CheckPlural(itemNamePlural, itemsFound));
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " most recent " + itemNamePlural + "; filter with /find" + itemNamePlural);
					}

					return true;

				} else if (allClosed) {
					// DISPLAY CLOSED TICKETS OR IDEAS
					rs = stmt.executeQuery(GetItemSelectQuery(targetTable, "status='CLOSED'", maxRecordsToReturn));
					int itemsFound = 0;
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Closed " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

					while (rs.next()) {
						itemsFound++;
						Utilities.ShowTicketInfo(sender, rs, verboseMode);
					}

					if (itemsFound == 0) {
						ReportNoItems(sender, targetTable);
						return true;
					} else {
						if (itemsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " " + Utilities.CheckPlural(itemNamePlural, itemsFound));
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " most recent closed " + itemNamePlural + "; filter with /find" + itemNamePlural);
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
			// DISPLAY USER TICKETS OR IDEAS
			try {
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					con = plugin.mysql.getConnection();
				} else {
					con = service.getConnection();
				}
				stmt = con.createStatement();
				rs = stmt.executeQuery(GetItemSelectQuery(targetTable, "uuid='" + player.getUniqueId().toString() + "'", maxRecordsToReturn));
				int itemsFound = 0;
				sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Your " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

				while (rs.next()) {
					itemsFound++;

					Utilities.ShowTicketInfo(sender, rs, false);
				}

				if (itemsFound == 0) {
					ReportNoItems(sender, targetTable);
				} else {
					sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " " + Utilities.CheckPlural(itemNamePlural, itemsFound));
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

	private void ReportNoItems(CommandSender sender, String targetTable) {
		if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
			sender.sendMessage(plugin.getMessage("NoIdeas"));
		else
			sender.sendMessage(plugin.getMessage("NoTickets"));
	}

	private String GetItemSelectQuery(String targetTable, String whereClause, int maxRecordsToReturn) {
		String innerQuery = "SELECT * FROM " + targetTable;
		if (!whereClause.isEmpty())
			innerQuery += " WHERE " + whereClause;

		innerQuery += " ORDER BY id DESC LIMIT " + maxRecordsToReturn;

		return "SELECT * FROM (" + innerQuery + ") AS SelectQ ORDER BY id ASC";
	}

}
