package me.odium.simplehelptickets.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyTicketCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public ReplyTicketCommand(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

    public static boolean ReplyItem(SimpleHelpTickets plugin, CommandSender sender, Table table, int id, String[] args) {
        String idText = String.valueOf(id);

        String notExistMessageName;
        if (Objects.equals(table, Table.IDEA)) {
            notExistMessageName = "IdeaNotExist";
        } else {
            notExistMessageName = "TicketNotExist";
        }

            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }

            StringBuilder sb = new StringBuilder();
            for (String arg : args)
                sb.append(arg).append(" ");
            String[] temp = sb.toString().split(" ");
            String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
            sb.delete(0, sb.length());
            for (String details : temp2) {
                sb.append(details);
                sb.append(" ");
            }
            String details = sb.toString().replace("'", "''");
        List<Ticket> found = plugin.getManager().getTickets(table, "id=" + id, 1);
        if (found.size() == 0) {
            sender.sendMessage(plugin.getMessage(notExistMessageName).replace("%arg%", args[0]));
            return true;
        }
        Ticket ticket = found.get(0);
            if (player == null) {
                String admin = sender.getName();
                ticket.setAdmin(admin);
                ticket.setAdminReply(admin + ": " + details);
                if (plugin.getManager().saveTicket(ticket, table)) {
                    NotifyReplied(plugin, sender, id, table);
                    NotifyOwnerOfReply(plugin, table, id, admin, ticket.getOwnerName());
                    return true;
                }
            } else {
                if (!player.hasPermission("sht.ticket") && !player.hasPermission("sht.admin")) {
                    sender.sendMessage(plugin.getMessage("NoPermission"));
                    return true;
                }
                String admin = player.getName();
                if (!player.hasPermission("sht.ticket") && !player.hasPermission("sht.admin")) {
                    sender.sendMessage(plugin.getMessage("NoPermission"));
                    return true;
                }
                if (ticket.getOwner() != null && ticket.getOwner().equals(player.getUniqueId())) {
                    ticket.setUserReply(details);
                    if (plugin.getManager().saveTicket(ticket, table)) {
                        NotifyReplied(plugin, sender, id, table);
                        String messageName;
                        if (Objects.equals(table, Table.IDEA))
                            messageName = "UserRepliedToIdea";
                        else
                            messageName = "UserRepliedToTicket";

                        String msg = plugin.getMessage(messageName).replace("%player%", player.getName()).replace("%arg%", idText);
                        plugin.notifyAdmins(msg, sender);
                        return true;
                    }
                } else {
                    if (!player.hasPermission("sht.admin")) {
                        sender.sendMessage(plugin.getMessage("NoPermission"));
                        return true;
                    }
                    ticket.setAdmin(admin);
                    ticket.setAdminReply(admin + ": " + details);
                    if (plugin.getManager().saveTicket(ticket, table)) {
                        String messageName;
                        if (Objects.equals(table, Table.IDEA))
                            messageName = "AdminRepliedToIdea";
                        else
                            messageName = "AdminRepliedToTicket";

                        String msg = plugin.getMessage(messageName).replace("%arg%", idText);
                        plugin.notifyAdmins(msg, null);
                        return true;
                    }
                }
                return false;
            }
            return false;
    }

    private static void NotifyOwnerOfReply(SimpleHelpTickets plugin, Table table, int id, String admin, String owner) {
        String messageName;
        if (Objects.equals(table, Table.IDEA))
            messageName = "AdminRepliedToIdeaOWNER";
        else
            messageName = "AdminRepliedToTicketOWNER";

        plugin.notifyUser(plugin.getMessage(messageName).replace("%arg%", String.valueOf(id)).replace("%admin%", admin), owner);

    }

    private static void NotifyReplied(SimpleHelpTickets plugin, CommandSender sender, int id, Table table) {

        String messageName;
        if (Objects.equals(table, Table.IDEA))
            messageName = "AdminRepliedToIdea";
        else
            messageName = "AdminRepliedToTicket";

        sender.sendMessage(plugin.getMessage(messageName).replace("%arg%", String.valueOf(id)));

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Use the command name to determine if we are working with a ticket or an idea
        Table table = TicketManager.getTableFromCommandString(label);
        String itemName = table.type;

        if (args.length <= 1) {
            // Show syntax: /replyticket or /replyidea
            sender.sendMessage("/reply" + itemName + " <#> <reply>");
            return true;
        } else {

            String messageName;
            if (Objects.equals(table, Table.IDEA)) {
                messageName = "InvalidIdeaNumber";
            } else {
                messageName = "InvalidTicketNumber";
            }

            for (char c : args[0].toCharArray()) {
                if (!Character.isDigit(c)) {
                    sender.sendMessage(plugin.getMessage(messageName).replace("%arg%", args[0]));
                    return true;
                }
            }

            int id = Integer.parseInt(args[0]);

            boolean success = ReplyItem(plugin, sender, table, id, args);
            if (success) plugin.reminder.addResponse(sender);
            return success;
        }

    }
}
