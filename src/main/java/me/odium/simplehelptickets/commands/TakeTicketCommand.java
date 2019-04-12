package me.odium.simplehelptickets.commands;

import java.util.List;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TakeTicketCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public TakeTicketCommand(SimpleHelpTickets plugin) {
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
            notExistMessageName = "IdeaNotExist";
        } else {
            notExistMessageName = "TicketNotExist";
        }
        if (player == null) {
            sender.sendMessage(plugin.RED + "This command can only be run by a player, use /check" + itemName + " instead.");
            return true;
        }

        if (args.length == 0) {
            // Show syntax: /taketicket or /takeidea
            sender.sendMessage(ChatColor.WHITE + "/take" + itemName + " <#>");
            return true;
        }

        for (char c : args[0].toCharArray()) {
            if (!Character.isDigit(c)) {
                sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[0]));
                return true;
            }
        }

        int ticketNumber = Integer.parseInt(args[0]);
        List<Ticket> found = plugin.getManager().getTickets(table, "id = " + ticketNumber, 1);
        if (found.size() == 0) {
            sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
            return true;
        }
        Ticket ticket = found.get(0);
        if (ticket.getLocation() == null) {
            sender.sendMessage(plugin.getMessage("InvalidWorld").replace("%world%", ticket.getLocation().getWorld()));
        }
        World world = null;
        if (ticket.getLocation().getLocation() == null) {
            world = Bukkit.getWorld(ticket.getLocation().getWorld());
        } else {
            try {
                world = ticket.getLocation().getLocation().getWorld();
            } catch (NullPointerException e) {
                plugin.log.warning("Location probably null: " + e.getMessage());
            }
        }
        if (world == null) {
            String server = ticket.getLocation().getServer();
            sender.sendMessage(plugin.getMessage("InvalidWorld").replace("%world%", ticket.getLocation().getWorld()));
            sender.sendMessage("Ticket was made on server " + server);
            return true;
        }
        Location location = ticket.getLocation().getLocation();
        Utilities.displayTicket(sender, itemName, ticket, SimpleHelpTickets.instance.getConfig().getBoolean("MultiWorld", false));
        int id = ticket.getId();
        String owner = ticket.getOwnerName();
        Ticket.Status status = ticket.getStatus();
        if (status == Ticket.Status.CLOSE) {
            sender.sendMessage(plugin.getMessage("CannotTakeClosedTicket").replace("&arg", String.valueOf(id)));
            return true;
        }

        // TELEPORT ADMIN
        if (!owner.equalsIgnoreCase("CONSOLE")) {
            player.teleport(location);
        }

        // NOTIFY ADMIN AND USERS
        String admin = player.getName();

        // ASSIGN ADMIN
        ticket.setAdmin(player.getDisplayName());
        if (plugin.getManager().saveTicket(ticket, table)) {
            if (Objects.equals(table, Table.IDEA))
                messageName = "TakeIdeaADMIN";
            else
                messageName = "TakeTicketADMIN";

            String msg = plugin.getMessage(messageName).replace("&arg", String.valueOf(id)).replace("&admin", admin);
            plugin.notifyAdmins(msg, player);

            // NOTIFY USER
            if (Objects.equals(table, Table.IDEA))
                messageName = "TakeIdeaOWNER";
            else
                messageName = "TakeTicketOWNER";

            msg = plugin.getMessage(messageName).replace("&arg", String.valueOf(id)).replace("&admin", admin);
            plugin.notifyUser(msg, owner);

            return true;
        }
        return false;

    }
}
