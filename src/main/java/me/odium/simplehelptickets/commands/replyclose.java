package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Objects;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class replyclose implements CommandExecutor {

	private final SimpleHelpTickets plugin;

	public replyclose(SimpleHelpTickets plugin) {
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
		String targetTable = Utilities.GetTargetTableName(label);
		String itemName = Utilities.GetTargetItemName(targetTable);

		if (args.length <= 1) {
			// Show syntax: /replycloseticket or /replycloseidea
			sender.sendMessage(ChatColor.WHITE + "/replyclose" + itemName + " <#> <reply>");
			return true;
		} else {

			String messageName;
			String notExistMessageName;
			if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME)) {
				messageName = "InvalidIdeaNumber";
				notExistMessageName = "IdeaNotExist";
			} else {
				messageName = "InvalidTicketNumber";
				notExistMessageName = "TicketNotExist";
			}

			for (char c : args[0].toCharArray()) {
				if (!Character.isDigit(c)) {
					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
					return true;
				}
			}

			// Make sure the ticket or idea is not already closed
			int id = Integer.parseInt(args[0]);

			// OPEN CONNECTION
			try {
				Connection con = plugin.service.getConnection();
				stmt = con.createStatement();

				// CHECK IF TICKET EXISTS
				rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM " + targetTable + " WHERE id='" + id + "'");
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					rs.next(); // sets pointer to first record in result set
				}
				if (rs.getInt("ticketTotal") == 0) {
					sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
					return true;
				}

				// GET TICKET FROM DB
				rs = stmt.executeQuery("SELECT * FROM " + targetTable + " WHERE id='" + id + "'");
				if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
					rs.next(); // sets pointer to first record in result set
				}
				// CHECK TICKET STATUS IS NOT ALREADY CLOSED, IF SO END HERE.
				if (rs.getString("status").equalsIgnoreCase("CLOSED")) {
					if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
						messageName = "IdeaAlreadyClosed";
					else
						messageName = "TicketAlreadyClosed";

					sender.sendMessage(plugin.getMessage(messageName).replace("&arg", args[0]));
					return true;
				}

				// REPLY TO THE TICKET OR IDEA
				boolean success = replyticket.ReplyItem(plugin, sender, targetTable, id, args);
				if(success)plugin.reminder.addResponse(sender);
				if (!success) return false;

				// CLOSE THE TICKET OR IDEA
				success = closeticket.CloseItem(plugin, sender, targetTable, id);
				return success;

			} catch (Exception e) {
				if (e.toString().contains("ResultSet closed")) {
					sender.sendMessage(plugin.getMessage(notExistMessageName).replace("&arg", args[0]));
					return true;
				} else {
					sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
					return true;
				}
			} finally {
				closeticket.closeResources(rs, stmt);
			}

		}

	}

}