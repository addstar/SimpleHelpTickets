package me.odium.simplehelptickets.commands;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Pair;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.objects.TicketLocation;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateTicketCommand implements CommandExecutor {

	private final SimpleHelpTickets plugin;

    public CreateTicketCommand(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	String reply;

	private ResultSet rs = null;
	private java.sql.Statement stmt = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length == 0) {
			sender.sendMessage(plugin.GOLD + "[ Tickets ]");
			sender.sendMessage(plugin.getMessage("HelpMe_Line1"));
			sender.sendMessage(plugin.getMessage("HelpMe_Line2"));
		} else if (args.length > 0) {

			// Use the command name to determine if we are working with a ticket or an idea
			Table table = TicketManager.getTableFromCommandString(label);
            String details = Utilities.santitizeTicketDetails(args);
			// Check for incomplete ticket / idea descriptions
			if (details.length() < 10 || !details.contains(" ")) {
				sender.sendMessage(plugin.getMessage("NotEnoughInformation"));
				return true;
			}

			boolean atConsole;
			String userreply;
			String expire;
			String adminreply;
			String admin;
            Ticket.Status status;
            Location location;
			String owner;
            UUID uuid;
			LocalDateTime date;
			if (player == null) {
				// SET VARIABLES FOR CONSOLE
                date = Utilities.getCurrentLocalTime();
                uuid = null;
				owner = sender.getName();
                location = null;
				adminreply = "NONE";
				userreply = "NONE";
                status = Ticket.Status.OPEN;
				admin = "NONE";
				expire = null;
				atConsole = true;
			} else {
				// SET VARIABLES FOR PLAYER
                date = Utilities.getCurrentLocalTime();
                uuid = player.getUniqueId();
				owner = player.getName();
                location = player.getLocation();
				adminreply = "NONE";
				userreply = "NONE";
                status = Ticket.Status.OPEN;
				admin = "NONE";
				expire = null;
				atConsole = false;
			}

			// Get ticket/idea limits from the config
			int maxTickets = plugin.getConfig().getInt("MaxTickets");
			int maxIdeas = plugin.getConfig().getInt("MaxIdeas");

			// Get cooldown limits from the config (minimum time between new tickets / ideas)
			int ticketCooldownSeconds = plugin.getConfig().getInt("TicketCooldownSeconds");
			int ideaCooldownSeconds = plugin.getConfig().getInt("IdeaCooldownSeconds");

			// REFERENCE CONNECTION AND ADD DATA
			if (!atConsole && !player.hasPermission("sht.admin")) {
				// CHECK MAX TICKETS OR IDEAS
				// ALSO CHECK COOLDOWN
				Pair<Integer, Long> result = plugin.getManager().getTicketCount(player, table, Ticket.Status.OPEN, null);
				int itemTotal = result.object1;
				if (ItemLimitReached(itemTotal, table, maxTickets, maxIdeas, sender)) {
						return true;
				}

				// Get Unix time (in seconds) of the newest ticket or idea
				if (result.object2 != null) {
					long newestItem = result.object2;
					if (WaitingForCooldown(newestItem, table, ticketCooldownSeconds, ideaCooldownSeconds, sender)) {
						return true;
					}
				}
			}
			return insertRecord(sender, table, details, userreply, expire, adminreply, admin, status, location, owner, uuid, date);
        }
        return true;
    }

	private boolean insertRecord(CommandSender sender, Table table, String details, String userreply, String expire, String adminreply, String admin, Ticket.Status status, Location loc, String owner, UUID uuid, LocalDateTime date) {
		TicketLocation location = new TicketLocation(loc, Bukkit.getServer().getServerId());
		Ticket ticket = new Ticket(uuid, details, date, location);
        ticket.setStatus(status);
        ticket.setAdmin(admin);
        ticket.setUserReply(userreply);
        ticket.setAdminReply(adminreply);
        ticket.setOwnerName(owner);
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			if (plugin.getManager().saveTickets(tickets, table)) {
				NotifyItemOpened(sender, table);
            } else {
                plugin.getLogger().warning("Failed to save Tickets....");
            }
        });
        return true;
	}

	private boolean ItemLimitReached(int itemTotal, Table table, int maxTickets, int maxIdeas, CommandSender sender) {

		if (Objects.equals(table, Table.IDEA)) {
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

	private void NotifyItemOpened(CommandSender sender, Table table) {

		String messageName;

		// Message player
		if (Objects.equals(table, Table.IDEA))
			messageName = "IdeaOpen";
		else
			messageName = "TicketOpen";

		sender.sendMessage(plugin.getMessage(messageName));

		// Notify admins of new ticket
		if (Objects.equals(table, Table.IDEA))
			messageName = "IdeaOpenADMIN";
		else
			messageName = "TicketOpenADMIN";

		String msg = plugin.getMessage(messageName).replace("%player", sender.getName());
		plugin.notifyAdmins(msg, sender);

	}

	private boolean WaitingForCooldown(
			long lastItemTime, Table table,
			int ticketCooldownSeconds, int ideaCooldownSeconds,
			CommandSender sender) {

		long currentTime = System.currentTimeMillis() / 1000;
		long itemTimeThreshold;
		String messageName;

		if (Objects.equals(table, Table.IDEA)) {
			itemTimeThreshold = lastItemTime + ideaCooldownSeconds;
			messageName = "IdeaTooSoon";
		}
		else {
			itemTimeThreshold = lastItemTime + ticketCooldownSeconds;
			messageName = "TicketTooSoon";
		}

		if (currentTime < itemTimeThreshold) {
			double minutesUntilNewItem = (itemTimeThreshold - currentTime) / 60.0;
			long minutesRemaining = (long)Math.floor(minutesUntilNewItem);
			int secondsRemaining = (int)Math.round((minutesUntilNewItem - minutesRemaining) * 60.0);

			String remainingTime = Long.toString(minutesRemaining) + "m " + Integer.toString(secondsRemaining) + "s";

			sender.sendMessage(plugin.getMessage(messageName).replace("&arg", remainingTime));

			return true;
		}

		return false;
	}

}