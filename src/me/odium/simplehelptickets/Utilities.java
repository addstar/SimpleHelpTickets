package me.odium.simplehelptickets;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utilities {

	public static String IDEA_TABLE_NAME = "SHT_Ideas";
	public static String TICKET_TABLE_NAME = "SHT_Tickets";

	private static SimpleDateFormat mShortDateFormater;

	static {
		// Static constructor
		mShortDateFormater = new SimpleDateFormat("yyyy-MM-dd");
	}

	public static String DateToString(long milliSecondTime, SimpleDateFormat dateFormatter) {
		return dateFormatter.format(milliSecondTime);
	}

	public static String DateToString(Calendar cal, SimpleDateFormat dateFormatter) {
		return DateToString(cal.getTime(), dateFormatter);
	}

	public static String DateToString(Date date, SimpleDateFormat dateFormatter) {
		return dateFormatter.format(date.getTime());
	}

	public static String Capitalize(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

	public static String CheckPlural(String pluralName, int itemCount) {

		// pluralName is assumed to end in "s"
		// If itemCount is 0 or > 1, return pluralName
		// Otherwise, return pluralName without the final "s"

		if (itemCount == 1) {
			return pluralName.substring(0, pluralName.length() - 1);
		}

		return pluralName;
	}

	public static String GetTargetItemName(String targetTable) {
		if (targetTable == Utilities.IDEA_TABLE_NAME)
			return "idea";
		else
			return "ticket";
	}

	public static String GetTargetTableName(String commandName, List<String> ideaCommands) {

		// Look for cmd in ideaCommands
		// If a match, return the Ideas table, otherwise return the Tickets table

		for(String item: ideaCommands) {
			if(item.equalsIgnoreCase(commandName))
				return IDEA_TABLE_NAME;
		}

		return TICKET_TABLE_NAME;
	}

	public static String NumToString(int value) {
		return String.format("%d", value);
	}

	public static void ShowTicketInfo(
			CommandSender sender,
			ResultSet rs,
			int maxDescriptionLength,
			int maxReplyLength,
			boolean showDates,
			boolean showReplies) throws Exception {

		String desc = TruncateLongText(rs.getString("description"), maxDescriptionLength);
		String adminReply = TruncateLongText(rs.getString("adminreply"), maxReplyLength);
		String userReply = TruncateLongText(rs.getString("userreply"), maxReplyLength);

		String ticketID = Utilities.NumToString(rs.getInt("id"));
		String ownerName = rs.getString("owner");

		String formattedTicketID = ChatColor.GOLD + "(";
		String formattedOwnerName;

		if (rs.getString("status").equalsIgnoreCase("OPEN")) {
			// Open Ticket
			formattedTicketID += ChatColor.WHITE + ticketID;
			formattedOwnerName = ChatColor.DARK_GREEN + ownerName + ": ";
		} else {
			// Closed Ticket
			formattedTicketID += ChatColor.GRAY + ticketID;
			formattedOwnerName = ChatColor.DARK_GRAY + ownerName + ": ";
		}

		if (showDates) {
			Date ticketDate = rs.getDate("date");
			formattedTicketID += ", " + ChatColor.DARK_AQUA + DateToString(ticketDate, mShortDateFormater);
		}
		formattedTicketID += ChatColor.GOLD + ") ";

		if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) {
			// ONLY ADMIN HAS REPLIED
			sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.YELLOW + desc);

		} else if (!rs.getString("userreply").equalsIgnoreCase("NONE") && rs.getString("adminreply").equalsIgnoreCase("NONE")) {
			// ONLY USER HAS REPLIED
			sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.YELLOW + desc);

		} else if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && !rs.getString("userreply").equalsIgnoreCase("NONE")) {
			// BOTH ADMIN AND USER HAVE REPLIED
			sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.GOLD + desc);

		} else if (rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) {
			// NEITHER HAVE REPLIED
			if (rs.getString("status").equalsIgnoreCase("OPEN")) {
				// TICKET IS OPEN AND NEITHER HAVE REPLIED
				sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.WHITE + desc);
			} else {
				// TICKET IS CLOSED AND NEITHER HAVE REPLIED
				sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.GRAY + desc);
			}
		}

		if (showReplies) {
			if (!adminReply.equalsIgnoreCase("none"))
				sender.sendMessage(ChatColor.BLUE + "Staff: " + ChatColor.GRAY + adminReply);

			if (!userReply.equalsIgnoreCase("none"))
				sender.sendMessage(ChatColor.RED + "User: " + ChatColor.GRAY + userReply);
		}

	}

	public static void ShowTicketInfo(CommandSender sender, ResultSet rs, boolean verboseMode) throws Exception {

		int maxDescriptionLength = 70;
		int maxReplyLength = 70;
		boolean showDates = false;
		boolean showReplies = false;

		if (verboseMode) {
			maxDescriptionLength = 100;
			maxReplyLength = 150;
			showDates = true;
			showReplies = true;
		}

		ShowTicketInfo(sender, rs, maxDescriptionLength, maxReplyLength, showDates, showReplies);

	}

	private static String TruncateLongText(String text, Integer maxLength) {
		if (text.length() > maxLength)
			return text.substring(0, maxLength) + "...";
		else
			return text;

	}

}
