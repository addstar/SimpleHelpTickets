package me.odium.simplehelptickets.utilities;


import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.objects.Ticket;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Utilities {

	public static final String IDEA_TABLE_NAME = "SHT_Ideas";
	public static final String TICKET_TABLE_NAME = "SHT_Tickets";

	private static final SimpleDateFormat mShortDateFormater;

	static {
		// Static constructor
		mShortDateFormater = new SimpleDateFormat("yyyy-MM-dd");
	}

    public static String dateToString(java.sql.Date date) {
        return mShortDateFormater.format(date);
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

	public static String capitalize(String text) {
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
		if (Objects.equals(targetTable, Utilities.IDEA_TABLE_NAME))
			return "idea";
		else
			return "ticket";
	}

	public static String GetTargetTableName(String commandName) {
		if (commandName.toLowerCase().contains("idea"))
			return IDEA_TABLE_NAME;
		else
			return TICKET_TABLE_NAME;
	}

	public static String NumToString(int value) {
		return String.format("%d", value);
	}

	public static void ShowTicketInfo(
            CommandSender sender,
            Ticket ticket,
            int maxDescriptionLength,
            int maxReplyLength,
            boolean showDates,
            boolean showReplies) {

        String desc = TruncateLongText(ticket.getDescription(), maxDescriptionLength);
        String adminReply = TruncateLongText(ticket.getAdminReply(), maxReplyLength);
        String userReply = TruncateLongText(ticket.getUserReply(), maxReplyLength);

        String ticketID = Utilities.NumToString(ticket.getId());
        String ownerName = ticket.getOwnerName();

		String formattedTicketID = ChatColor.GOLD + "(";
		String formattedOwnerName;

        if (ticket.isOpen()) {
			// Open Ticket
			formattedTicketID += ChatColor.WHITE + ticketID;
			formattedOwnerName = ChatColor.DARK_GREEN + ownerName + ": ";
		} else {
			// Closed Ticket
			formattedTicketID += ChatColor.GRAY + ticketID;
			formattedOwnerName = ChatColor.DARK_GRAY + ownerName + ": ";
		}

		if (showDates) {
            Date ticketDate = ticket.getCreatedDate();
			formattedTicketID += ", " + ChatColor.DARK_AQUA + DateToString(ticketDate, mShortDateFormater);
		}
		formattedTicketID += ChatColor.GOLD + ") ";

        if (ticket.hasAdminReply() || ticket.hasUserReply()) {
			// ONLY ADMIN HAS REPLIED
			sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.YELLOW + desc);

        } else if (ticket.hasAdminReply() && ticket.hasUserReply()) {
			// BOTH ADMIN AND USER HAVE REPLIED
			sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.GOLD + desc);

        } else if (!ticket.hasAdminReply() && !ticket.hasUserReply()) {
			// NEITHER HAVE REPLIED
            if (ticket.getStatus() == Ticket.Status.OPEN) {
				// TICKET IS OPEN AND NEITHER HAVE REPLIED
				sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.WHITE + desc);
			} else {
				// TICKET IS CLOSED AND NEITHER HAVE REPLIED
				sender.sendMessage(formattedTicketID + formattedOwnerName + ChatColor.GRAY + desc);
			}
		}

		if (showReplies) {
			if (ticket.hasAdminReply())
				sender.sendMessage(ChatColor.BLUE + "Staff: " + ChatColor.GRAY + adminReply);

			if (ticket.hasUserReply())
				sender.sendMessage(ChatColor.RED + "User: " + ChatColor.GRAY + userReply);
		}

	}

	public static void displayTicket(CommandSender sender, String type, Ticket ticket, boolean multiworld) {
        sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + ChatColor.BOLD + type + " " + ticket.getId() + ChatColor.RESET + ChatColor.GOLD + " ]");
        sender.sendMessage(ChatColor.BLUE + " Owner: " + ChatColor.WHITE + ticket.getOwnerName());
        sender.sendMessage(ChatColor.BLUE + " Date: " + ChatColor.WHITE + Utilities.dateToString(ticket.getCreatedDate()));
		if (multiworld) {
            String strloc = "(" + ticket.getLocation().getX() + ", " + ticket.getLocation().getY() + ", " + ticket.getLocation().getZ() + ")";
			sender.sendMessage(ChatColor.BLUE + " Location: " + ChatColor.WHITE + ticket.getLocation().getWorld() + " " + ChatColor.GRAY + strloc);
        }
        if (ticket.isOpen()) {
            sender.sendMessage(ChatColor.BLUE + " Status: " + ChatColor.GREEN + ticket.getStatus().name());
        } else {
            sender.sendMessage(ChatColor.BLUE + " Status: " + ChatColor.RED + ticket.getStatus().name());
        }
        sender.sendMessage(ChatColor.BLUE + " Assigned: " + ChatColor.WHITE + ticket.getAdmin());
		sender.sendMessage(ChatColor.BLUE + " " + capitalize(type) + ": " + ChatColor.GOLD + ticket.getDescription());
		if (!ticket.hasAdminReply()) {
            sender.sendMessage(ChatColor.BLUE + " Staff Reply: " + ChatColor.WHITE + "(none)");
        } else {
            sender.sendMessage(ChatColor.BLUE + " Staff Reply: " + ChatColor.YELLOW + ticket.getAdminReply());
        }
		if (!ticket.hasUserReply()) {
            sender.sendMessage(ChatColor.BLUE + " User Reply: " + ChatColor.WHITE + "(none)");
        } else {
            sender.sendMessage(ChatColor.BLUE + " User Reply: " + ChatColor.YELLOW + ticket.getUserReply());
        }
        if (ticket.getExpirationDate() != null) {
            String expiration = Utilities.dateToString(new java.sql.Date(ticket.getExpirationDate().getTime()));
            sender.sendMessage(ChatColor.BLUE + " Expiration: " + ChatColor.WHITE + expiration);
        }
    }

    public static void ShowTicketInfo(CommandSender sender, Ticket rs, boolean verboseMode) {

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

    public static java.sql.Date getCurrentDTG() {
        return new java.sql.Date(Calendar.getInstance().getTime().getTime());
    }

	public static java.util.Date parseDate(String dateValue, String dateFormat) {

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		try {
			return sdf.parse(dateValue);

		} catch (ParseException ex) {
			return null;
		}

	}

	public static java.util.Date parseDate(String dateValue) {

		java.util.Date parsedDate = parseDate(dateValue, "yyyy-MM-dd'T'HH:mm:ss");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = parseDate(dateValue, "yyyy-MM-dd'T'HH:mm");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = parseDate(dateValue, "yyyy-MM-dd HH:mm:ss");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = parseDate(dateValue, "yyyy-MM-dd HH:mm");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = parseDate(dateValue, "yyyy-MM-dd");
		if (parsedDate != null) {
			return parsedDate;
		}

		return null;
	}

	public static String santitizeTicketDetails(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String arg : args)
			sb.append(arg).append(" ");
		String[] temp = sb.toString().split(" ");
		String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
		sb.delete(0, sb.length());
		for (String details : temp2) {
			sb.append(details);
			sb.append(" ");
		}
		String details = sb.toString().trim();
		details = details.replace("'", "''");
		return details;
	}

}
