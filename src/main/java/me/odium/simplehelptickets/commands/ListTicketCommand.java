package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListTicketCommand implements CommandExecutor {

	private final SimpleHelpTickets plugin;

    public ListTicketCommand(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	private ResultSet rs = null;
	private java.sql.Statement stmt = null;

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
		Table table = TicketManager.getTableFromCommandString(label);
		String itemNamePlural = table.type + "s";
        List<Ticket> tickets;
		Connection con = null;
		if (player == null || player.hasPermission("sht.admin")) {
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
					tickets = plugin.getManager().getTickets(table, "status='OPEN'", maxRecordsToReturn);
                    int itemsFound = tickets.size();

					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Open " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

                    for (Ticket ticket : tickets) {
                        Utilities.ShowTicketInfo(sender, ticket, verboseMode);
					}

					if (itemsFound == 0) {
						ReportNoItems(sender, table);
					} else {
						if (itemsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " open " + Utilities.CheckPlural(itemNamePlural, itemsFound));
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " most recent open " + itemNamePlural + "; filter with /find" + itemNamePlural);
					}

					if (itemNamePlural.contentEquals("tickets")) {
						// Also report the number of open ideas

						tickets = plugin.getManager().getTickets(Table.IDEA, "status='OPEN'", maxRecordsToReturn);
                        int ideasFound = tickets.size();
						if (ideasFound > 0) {
							sender.sendMessage(plugin.BLUE + Utilities.NumToString(ideasFound) + " open " + Utilities.CheckPlural("ideas", ideasFound));
						}
					}

					return true;

				} else if (allTickets) {
					// DISPLAY ALL TICKETS OR IDEAS
					tickets = plugin.getManager().getTickets(table, "", maxRecordsToReturn);
                    int itemsFound = tickets.size();
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "All " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

                    for (Ticket ticket : tickets) {
                        Utilities.ShowTicketInfo(sender, ticket, verboseMode);
					}

					if (itemsFound == 0) {
						ReportNoItems(sender, table);
					} else {
						if (itemsFound < maxRecordsToReturn)
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " " + Utilities.CheckPlural(itemNamePlural, itemsFound));
						else
							sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " most recent " + itemNamePlural + "; filter with /find" + itemNamePlural);
					}

					return true;

				} else if (allClosed) {
					// DISPLAY CLOSED TICKETS OR IDEAS
					tickets = plugin.getManager().getTickets(table, "status='CLOSED'", maxRecordsToReturn);
                    int itemsFound = tickets.size();
					sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Closed " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

                    for (Ticket ticket : tickets) {
                        Utilities.ShowTicketInfo(sender, ticket, verboseMode);
					}

					if (itemsFound == 0) {
						ReportNoItems(sender, table);
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

		} else {
			// DISPLAY USER TICKETS OR IDEAS
			tickets = plugin.getManager().getTickets(table, "uuid='" + player.getUniqueId().toString() + "'", maxRecordsToReturn);
            int itemsFound = tickets.size();
				sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Your " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

            for (Ticket ticket : tickets) {
                Utilities.ShowTicketInfo(sender, ticket, false);
            }
				if (itemsFound == 0) {
					ReportNoItems(sender, table);
				} else {
					sender.sendMessage(plugin.GREEN + Utilities.NumToString(itemsFound) + " " + Utilities.CheckPlural(itemNamePlural, itemsFound));
				}
				return true;
		}

	}

	private void ReportNoItems(CommandSender sender, Table table) {
		if (Objects.equals(table, Table.IDEA))
			sender.sendMessage(plugin.getMessage("NoIdeas"));
		else
			sender.sendMessage(plugin.getMessage("NoTickets"));
	}

}
