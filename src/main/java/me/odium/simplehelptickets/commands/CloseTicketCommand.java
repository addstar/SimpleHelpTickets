package me.odium.simplehelptickets.commands;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javafx.scene.control.Tab;
import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Pair;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseTicketCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public CloseTicketCommand(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

    public static boolean CloseItem(SimpleHelpTickets plugin, CommandSender sender, Table table, int id) {

        String idText = String.valueOf(id);

        String messageName;
        String mailmessageName;
        String notExistMessageName;
        if (Objects.equals(table, Table.IDEA)) {
            notExistMessageName = "IdeaNotExist";
        } else {
            notExistMessageName = "TicketNotExist";
        }

        // OPEN CONNECTION
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        Pair<Integer, Long> result = plugin.getManager().getTicketCount(null, table, Ticket.Status.OPEN, id);
        if (result.object1 == 0) {
            sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", idText));
            return true;
        }
        List<Ticket> tickets = plugin.getManager().getTickets(table, "id = " + id, 1);
        Ticket ticket = tickets.get(0);
        if (!ticket.isOpen()) {
            if (Objects.equals(table, Table.IDEA))
                messageName = "IdeaAlreadyClosed";
            else
                messageName = "TicketAlreadyClosed";

            sender.sendMessage(plugin.getMessage(messageName).replace("&arg", idText));
            return true;
        }

        if (player == null || (ticket.getOwner() == player.getUniqueId()) || player.hasPermission("sht.admin")) {
            String admin = "ADMIN";

            if (player == null) {
                admin = sender.getName();
            } else if (ticket.getOwner().equals(player.getUniqueId()) || player.hasPermission("sht.admin")) {
                admin = player.getName();
            }
            ticket.setStatus(Ticket.Status.CLOSE);
            ticket.setAdmin(admin);
            ticket.setExpirationDate(new Timestamp(plugin.getExpiration().getTime()));
            if (plugin.getManager().saveTicket(ticket, table)) {
                if (Objects.equals(table, Table.IDEA)) {
                    messageName = "IdeaClosed";
                    mailmessageName = "IdeaClosedMail";
                } else {
                    messageName = "TicketClosed";
                    mailmessageName = "TicketClosedMail";
                }
                sender.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id));
                plugin.sendMailOnClose(sender, ticket.getOwnerName(), plugin.getMessage(mailmessageName).replace("&arg", "" + id));

                if (Objects.equals(table, Table.IDEA)) {
                    messageName = "IdeaClosedADMIN";
                } else {
                    messageName = "TicketClosedADMIN";
                }

                String msg = plugin.getMessage(messageName).replace("&arg", "" + idText).replace("&admin", admin);
                plugin.notifyAdmins(msg, null);
            } else {
                sender.sendMessage("Unknown  error closing Ticket");
                return false;
            }
        }
        return true;
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

        if (args.length == 0) {
            // Show syntax: /closeticket or /closeidea
            sender.sendMessage(ChatColor.WHITE + "/close" + itemName + " <#>");
            sender.sendMessage(ChatColor.GRAY + "(reopen with /close" + itemName + " -r <#>)");
            return true;
        } else if (args.length == 1) {
            // CLOSING TICKET
            // CHECK TICKETNUMBER IS A DIGIT
            if (CheckTicketCommand.checkInvalidNumber(sender, args, table, plugin)) return true;

            int id = Integer.parseInt(args[0]);

            boolean success = CloseItem(plugin, sender, table, id);
            if (success) plugin.reminder.addResponse(sender);
            return success;

        } else if (args.length == 2 && args[0].equalsIgnoreCase("-r")) {

            // REOPENING A TICKET
            // CHECK TICKETNUMBER IS A DIGIT
            for (char c : args[1].toCharArray()) {
                if (!Character.isDigit(c)) {
                    if (Objects.equals(table, Table.IDEA))
                        messageName = "InvalidIdeaNumber";
                    else
                        messageName = "InvalidTicketNumber";

                    sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[1]));
                    return true;
                }
            }

            int id = Integer.parseInt(args[1]);
            List<Ticket> tickets = plugin.getManager().getTickets(table, "id = " + id, 1);
            if (!(tickets.size() == 1)) {
                sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[1]));
                return false;
                }
            Ticket ticket = tickets.get(0);
            Player target = Bukkit.getPlayer(ticket.getOwner());
            if (player == null || player.hasPermission("sht.admin")) {
                // SET THE ADMIN VARIABLE TO REFLECT CONSOLE/ADMIN
                String admin = "ADMIN";
                if (player == null) {
                    admin = sender.getName();
                    ticket.setStatus(Ticket.Status.OPEN);
                    ticket.setExpirationDate(null);
                } else if (ticket.getOwner().equals(player.getUniqueId()) || player.hasPermission("sht.admin")) {
                    admin = player.getName();
                    ticket.setStatus(Ticket.Status.OPEN);
                    ticket.setExpirationDate(null);
                }
                plugin.getManager().saveTicket(ticket, table);
                if (Objects.equals(table, Table.IDEA))
                    messageName = "IdeaReopened";
                else
                    messageName = "TicketReopened";
                sender.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id).replace("&admin", admin));
                if (target != null && target != player) {

                    if (Objects.equals(table, Table.IDEA))
                        messageName = "IdeaReopenedOWNER";
                    else
                        messageName = "TicketReopenedOWNER";
                    target.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id).replace("&admin", admin));
                }
                if (Objects.equals(table, Table.IDEA))
                    messageName = "IdeaReopenedADMIN";
                else
                    messageName = "TicketReopenedADMIN";

                // INFORM ADMINS OF CHANGES TO TICKET
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                for (Player op : players) {
                    if (op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose") && op != sender) {
                        op.sendMessage(plugin.getMessage(messageName).replace("&arg", "" + id).replace("&admin", admin));
                    }
                }

            } else {
                sender.sendMessage(plugin.getMessage("NoPermission"));
                return true;
            }
        }
        return false;
    }
}
