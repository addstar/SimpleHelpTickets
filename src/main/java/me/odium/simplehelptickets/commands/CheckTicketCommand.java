package me.odium.simplehelptickets.commands;

import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckTicketCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public CheckTicketCommand(SimpleHelpTickets plugin) {
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
        String targetTable = TicketManager.getTableNamefromCommandString(label);
		String itemName = Utilities.GetTargetItemName(targetTable);
		String messageName;

		if (args.length != 1) {
			// Show syntax: /checkticket or /checkidea
			sender.sendMessage(ChatColor.WHITE + "/check" + itemName + " <#>");
			return true;
		} else {

            if (checkInvalidNumber(sender, args, targetTable, plugin)) return true;

			int ticketNumber = Integer.parseInt(args[0]);
            List<Ticket> tickets = plugin.getManager().getTickets(targetTable, "id = " + ticketNumber, 1);
            if (player == null || player.hasPermission("sht.admin") || tickets.get(0).getOwner() == player.getUniqueId()) {
                Utilities.displayTicket(sender, itemName, tickets.get(0), SimpleHelpTickets.instance.getConfig().getBoolean("MultiWorld", false));
            }
            if (tickets.size() == 0) {
                if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
                    messageName = "IdeaNotExist";
                else
                    messageName = "TicketNotExist";
                sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
                return true;
            }
			return true;
		}
	}
}
