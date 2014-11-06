package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class taketicket implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public taketicket(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	java.sql.Statement stmt = null;
	Connection con = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (player == null) {
			sender.sendMessage(plugin.RED + "This command can only be run by a player, use /checkticket instead.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.WHITE + "/taketicket <#>");
			return true;
		}

		for (char c : args[0].toCharArray()) {
			if (!Character.isDigit(c)) {
				sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[0]));
				return true;
			}
		}

		int ticketNumber = Integer.parseInt(args[0]);

		try {
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				con = plugin.mysql.getConnection();
			} else {
				con = service.getConnection();
			}
			stmt = con.createStatement();

			rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='" + ticketNumber + "'");
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				rs.next(); // sets pointer to first record in result set
			}
			String worldName = null;
			String date;

			// compile location
			World world = Bukkit.getWorld(rs.getString("world"));
			if (world == null) {
				sender.sendMessage(plugin.getMessage("InvalidWorld").replace("%world%", rs.getString("world")));
				return true;
			}
			double x = rs.getDouble("x");
			double y = rs.getDouble("y");
			double z = rs.getDouble("z");
			float p = (float) rs.getDouble("p");
			float f = (float) rs.getDouble("f");
			final Location locc = new Location(world, x, y, z, f, p);
			rs.close();

			// Display Ticket
			rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='" + ticketNumber + "'");
			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				rs.next(); // sets pointer to first record in result set
			}

			String id = rs.getString("id");
			// String owner = rs.getString("owner");
			String owner = rs.getString("owner");
			String strloc = null;
			if (plugin.getConfig().getBoolean("MultiWorld") == true) {
				worldName = rs.getString("world");
				strloc = "(" + rs.getInt("x") + ", " + rs.getInt("y") + ", " + rs.getInt("z") + ")";
			}

			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				date = new SimpleDateFormat("dd/MMM/yy HH:mm").format(rs.getTimestamp("date"));
			} else {
				date = rs.getString("date");
			}

			String Assignedadmin = rs.getString("admin");
			String adminreply = rs.getString("adminreply");
			String userreply = rs.getString("userreply");
			String description = rs.getString("description");
			String status = rs.getString("status");

			if (status.equalsIgnoreCase("CLOSED")) {
				sender.sendMessage(plugin.getMessage("CannotTakeClosedTicket").replace("&arg", id));
				return true;
			}

			sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + ChatColor.BOLD + "Ticket " + id + ChatColor.RESET + ChatColor.GOLD + " ]");
			sender.sendMessage(ChatColor.BLUE + " Owner: " + ChatColor.WHITE + owner);
			sender.sendMessage(ChatColor.BLUE + " Date: " + ChatColor.WHITE + date);
			if (plugin.getConfig().getBoolean("MultiWorld") == true) {
				sender.sendMessage(ChatColor.BLUE + " Location: " + ChatColor.WHITE + worldName + " " + ChatColor.GRAY + strloc);
			}
			if (status.contains("OPEN")) {
				sender.sendMessage(ChatColor.BLUE + " Status: " + ChatColor.GREEN + status);
			} else {
				sender.sendMessage(ChatColor.BLUE + " Status: " + ChatColor.RED + status);
			}
			sender.sendMessage(ChatColor.BLUE + " Assigned: " + ChatColor.WHITE + Assignedadmin);
			sender.sendMessage(ChatColor.BLUE + " Ticket: " + ChatColor.GOLD + description);
			if (adminreply.equalsIgnoreCase("NONE")) {
				sender.sendMessage(ChatColor.BLUE + " Admin Reply: " + ChatColor.WHITE + "(none)");
			} else {
				sender.sendMessage(ChatColor.BLUE + " Admin Reply: " + ChatColor.YELLOW + adminreply);
			}
			if (userreply.equalsIgnoreCase("NONE")) {
				sender.sendMessage(ChatColor.BLUE + " User Reply: " + ChatColor.WHITE + "(none)");
			} else {
				sender.sendMessage(ChatColor.BLUE + " User Reply: " + ChatColor.YELLOW + userreply);
			}

			// TELEPORT ADMIN
			if (!owner.equalsIgnoreCase("CONSOLE")) {
				player.teleport(locc);
			}
			// NOTIFY ADMIN AND USERS
			String admin = player.getName();
			// ASSIGN ADMIN
			stmt.executeUpdate("UPDATE SHT_Tickets SET admin='" + admin + "' WHERE id='" + id + "'");
			// NOTIFY -OTHER- ADMINS
			String msg = plugin.getMessage("TakeTicketADMIN").replace("&arg", id).replace("&admin", admin);
			plugin.notifyAdmins(msg, player);
			// NOTIFY USER
			msg = plugin.getMessage("TakeTicketOWNER").replace("&arg", id).replace("&admin", admin);
			plugin.notifyUser(msg, owner);

			return true;
		} catch (Exception e) {
			if (e.toString().contains("ResultSet closed")) {
				sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
				return true;
			} else if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
				sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
				return true;
			} else if (e.toString().contains("empty result set.")) {
				sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
				return true;
			} else {
				sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
				return true;
			}
		} finally {
			try {
				if (rs != null) { rs.close(); rs = null; }
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
				e.printStackTrace();
			}
		}
	}
}