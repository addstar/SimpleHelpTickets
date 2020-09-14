package me.odium.simplehelptickets.commands;

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

public class ReplyCloseCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public ReplyCloseCommand(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

    private ResultSet rs = null;
    private java.sql.Statement stmt = null;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        // Use the command name to determine if we are working with a ticket or an idea
        Table table = TicketManager.getTableFromCommandString(label);
        String itemName = table.type;

        if (args.length <= 1) {
            // Show syntax: /replycloseticket or /replycloseidea
            sender.sendMessage(ChatColor.WHITE + "/replyclose" + itemName + " <#> <reply>");
            return true;
        } else {

            String messageName;
            String notExistMessageName;
            if (Objects.equals(table, Table.IDEA)) {
                messageName = "InvalidIdeaNumber";
                notExistMessageName = "IdeaNotExist";
            } else {
                messageName = "InvalidTicketNumber";
                notExistMessageName = "TicketNotExist";
            }

            for (char c : args[0].toCharArray()) {
                if (!Character.isDigit(c)) {
                    sender.sendMessage(plugin.getMessage(messageName).replace("%arg%", args[0]));
                    return true;
                }
            }

            // Make sure the ticket or idea is not already closed
            int id = Integer.parseInt(args[0]);
            List<Ticket> found = plugin.getManager().getTickets(table, "id = " + id, 1);
            if (found.size() == 0) {
                sender.sendMessage(plugin.getMessage(notExistMessageName).replace("%arg%", args[0]));
                return true;
            }
            Ticket ticket = found.get(0);
            if (!ticket.isOpen()) {
                if (Objects.equals(table, Table.IDEA))
                    messageName = "IdeaAlreadyClosed";
                else
                    messageName = "TicketAlreadyClosed";

                sender.sendMessage(plugin.getMessage(messageName).replace("%arg%", args[0]));
                return true;
            }
            boolean success = ReplyTicketCommand.ReplyItem(plugin, sender, table, id, args);
            if (success) plugin.reminder.addResponse(sender);
            if (!success) return false;
            success = CloseTicketCommand.CloseItem(plugin, sender, table, id);
            return success;
        }

    }

}
