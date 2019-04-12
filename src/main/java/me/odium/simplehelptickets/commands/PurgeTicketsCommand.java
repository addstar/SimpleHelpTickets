package me.odium.simplehelptickets.commands;

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

public class PurgeTicketsCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public PurgeTicketsCommand(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("sht.purgetickets")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                return true;
            }

        }

        // Use the command name to determine if we are working with a ticket or an idea
        Table table = TicketManager.getTableFromCommandString(label);
        String itemNamePlural = table.type + "s";

        if (args.length == 0) {
            // PURGE EXPIRED TICKETS OR IDEAS

            int expiredItems;
            if (Objects.equals(table, Table.IDEA))
                expiredItems= plugin.expireIdeas();
            else
                expiredItems= plugin.expireTickets();

            sender.sendMessage(plugin.GRAY + "[Tickets] " + ChatColor.GOLD + expiredItems + ChatColor.WHITE + " expired " + Utilities.checkPlural(itemNamePlural, expiredItems) + " purged");

            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("-c")) {
            sender.sendMessage(plugin.GRAY + "[Tickets] " + ChatColor.GOLD + "This will delete all CLOSED " + itemNamePlural + "!\n To confirm, use " + ChatColor.GREEN + "/" + itemNamePlural + " -c confirm");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("-a")) {
            sender.sendMessage(plugin.GRAY + "[Tickets] " + ChatColor.GOLD + "This will delete ALL " + itemNamePlural + "!\n To confirm, use " + ChatColor.GREEN + "/" + itemNamePlural + " -a confirm");
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("-c") && args[1].equalsIgnoreCase("confirm")) {
            // PURGE CLOSED TICKETS OR IDEAS
            plugin.getManager().deleteTickets(table, Ticket.Status.CLOSE);

                String messageName;
            if (Objects.equals(table, Table.IDEA))
                    messageName = "AllClosedIdeasPurged";
                else
                    messageName = "AllClosedTicketsPurged";
                sender.sendMessage(plugin.getMessage(messageName));


            return true;

        } else if (args.length == 2 && args[0].equalsIgnoreCase("-a") && args[1].equalsIgnoreCase("confirm")) {
            // PURGE ALL TICKETS OR IDEAS
            if (plugin.databaseService.clearTable(table)) {
                String messageName;
                if (Objects.equals(table, Table.IDEA))
                    messageName = "AllIdeasPurged";
                else
                    messageName = "AllTicketsPurged";
                sender.sendMessage(plugin.getMessage(messageName));
            } else {
                return false;
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.WHITE + "/purge" + itemNamePlural + " [-c|-a]");
            return true;
        }

    }
}
