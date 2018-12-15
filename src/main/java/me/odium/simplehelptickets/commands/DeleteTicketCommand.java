package me.odium.simplehelptickets.commands;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Pair;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteTicketCommand implements CommandExecutor {

	private final SimpleHelpTickets plugin;

    public DeleteTicketCommand(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		// Use the command name to determine if we are working with a ticket or an idea
		Table table = TicketManager.getTableFromCommandString(label);
		String itemName = table.type;

		String messageName;
		String notExistMessageName;
		if (Objects.equals(table, Table.IDEA)) {
			messageName = "InvalidIdeaNumber";
			notExistMessageName = "IdeaNotExist";
		} else {
			messageName = "InvalidTicketNumber";
			notExistMessageName = "TicketNotExist";
		}

		if (args.length == 0) {
			// Show syntax: /delticket or /delidea
			sender.sendMessage(ChatColor.WHITE + "/del" + itemName + " <#>");
			return true;
		} else if (args.length == 1) {

			for (char c : args[0].toCharArray()) {
				if (!Character.isDigit(c)) {
					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
					return true;
				}
			}

			itemName = Utilities.capitalize(itemName);
			if (player == null) {
                Integer id;
				try {
                    id = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                    return true;
                }
				Pair<Integer, Long> result = plugin.getManager().getTicketCount(null, table, null, id);
				if (result.object1 == 0) {
                    sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
                    return true;
                }
				Integer deleted = plugin.getManager().deleteTicketbyId(table, id);
                if (deleted > 0) {
                    sender.sendMessage(plugin.GRAY + "[Tickets] " + plugin.WHITE + itemName + " " + ChatColor.GOLD + args[0] + ChatColor.WHITE + "" +
                            " Deleted");
				}
			} else {
					if (!player.hasPermission("sht.purgetickets")) {
						sender.sendMessage(plugin.getMessage("NoPermission"));
						return true;
					}
                Integer id;
                try {
                    id = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                    return true;
					}
				Pair<Integer, Long> result = plugin.getManager().getTicketCount(null, table, null, id);
				if (result.object1 == 0) {
						sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
						return true;
					}
				List<Ticket> tickets = plugin.getManager().getTickets(table, "id = " + id, 1);
                if (!player.hasPermission("sht.purgetickets") && !(tickets.get(0).getOwner().equals(player.getUniqueId()))) {
                    sender.sendMessage(plugin.GRAY + "[SimpleHelpTickets] " + plugin.RED + itemName + " " + tickets.get(0).getId() + " is not your ticket to delete.");
						return true;
                }

				if (plugin.getManager().deleteTicketbyId(table, id) > 0) {
						sender.sendMessage(plugin.GRAY + "[Tickets] " + plugin.WHITE + itemName + " " + ChatColor.GOLD + args[0] + ChatColor.WHITE + " Deleted");
						return true;
                }
			}
		}
		return false;
	}
}
