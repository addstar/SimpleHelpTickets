package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.TimeParser;
import me.odium.simplehelptickets.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class findtickets implements CommandExecutor {

	public SimpleHelpTickets plugin;

	public findtickets(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs = null;
	java.sql.Statement stmt = null;
	Connection con = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;

		boolean atConsole = true;
		String ticketOwnerOverride = "";

		if (sender instanceof Player) {
			player = (Player) sender;
			atConsole = false;

			if (!player.hasPermission("sht.admin")) {
				// Only allow the user to search / view their own tickets
				ticketOwnerOverride = player.getName();
			}

		}

		// Set defaults

		String ticketOwner = "%";
		String staffName = "%";

		boolean mostRecentTimeDefined = false;
		boolean dateRangeDefined = false;

		// Last 24 hours
		long recentTimeStartMillisec = System.currentTimeMillis() - 86400 * 1000;

		// Set the start date
		String startDate = "1970-01-01";

		// Set the end date to 1 day in the future
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String endDate = Utilities.DateToString(cal, dateFormatter);

		String sortDirection = "asc";

		int ticketIDStart = 0;
		int ticketIDEnd = Integer.MAX_VALUE;

		String searchPhrase = "";
		boolean wildCardsAdded = false;

		boolean showDates = false;
		boolean showReplies = false;

		int ticketsToShow = 10;

		int maxRecordsToReturn;
		if (atConsole)
			maxRecordsToReturn = 500;
		else
			maxRecordsToReturn = 100;

		// Parse the arguments
		try {
			if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
				if (ticketOwnerOverride.isEmpty())
					ShowSyntax(sender, true);
				else
					ShowSyntax(sender, false);

				return true;
			}


			// Step through the arguments
			for (int i = 0; i < args.length; i++) {
				boolean searchWord = true;

				// optionLength is the length of the option, including the colon; 0 if no match
				int optionLength;

				// Match both o: and p: for ticket owner/player
				optionLength = FindOption(args[i], "o");
				if (optionLength == 0)
					optionLength = FindOption(args[i], "p");

				if (optionLength > 0) {
					searchWord = false;
					ticketOwner = args[i].substring(optionLength);
				}

				// Match both s: and a: for staff/admin responder
				optionLength = FindOption(args[i], "s");
				if (optionLength == 0)
					optionLength = FindOption(args[i], "a");

				if (optionLength > 0) {
					// Staff filter (aka admin filter)
					searchWord = false;
					staffName = args[i].substring(optionLength);
				}

				optionLength = FindOption(args[i], "t");
				if (optionLength > 0) {
					searchWord = false;

					String timeSpec = args[i].substring(optionLength);

					recentTimeStartMillisec = GetSearchStartTime(sender, timeSpec);
					if (recentTimeStartMillisec == 0) {
						sender.sendMessage(ChatColor.RED + "Invalid time interval, " + args[i]);
						return true;
					}

					mostRecentTimeDefined = true;
				}

				optionLength = FindOption(args[i], "from");
				if (optionLength > 0) {
					searchWord = false;

					String datePart = args[i].substring(optionLength);
					Date parsedDate = ParseDate(datePart);

					if (parsedDate == null) {
						sender.sendMessage(ChatColor.RED + "Invalid date value, " + args[i]);
						sender.sendMessage(ChatColor.AQUA + "Example dates: 2015-11-15 or 2015-11-15T3:00");
						return true;
					}

					startDate = Utilities.DateToString(parsedDate, dateFormatter);
					dateRangeDefined = true;
				}

				optionLength = FindOption(args[i], "to");
				if (optionLength > 0) {
					searchWord = false;

					String datePart = args[i].substring(optionLength);
					Date parsedDate = ParseDate(datePart);

					if (parsedDate == null) {
						sender.sendMessage(ChatColor.RED + "Invalid date value, " + args[i]);
						sender.sendMessage(ChatColor.AQUA + "Example dates: 2015-11-15 or 2015-11-15T3:00");
						return true;
					}

					endDate = Utilities.DateToString(parsedDate, dateFormatter);
					dateRangeDefined = true;
				}

				optionLength = FindOption(args[i], "first");
				if (optionLength > 0) {
					searchWord = false;

					try {
						ticketIDStart = Integer.parseInt(args[i].substring(optionLength));
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Invalid starting ticket ID, " + args[i]);
						return true;
					}
				}

				optionLength = FindOption(args[i], "last");
				if (optionLength > 0) {
					searchWord = false;

					try {
						ticketIDEnd = Integer.parseInt(args[i].substring(optionLength));
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Invalid ending ticket ID, " + args[i]);
						return true;
					}
				}

				optionLength = FindOption(args[i], "limit");
				if (optionLength > 0) {
					searchWord = false;

					try {
						ticketsToShow = Integer.parseInt(args[i].substring(optionLength));
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Invalid record count, " + args[i]);
						return true;
					}
				}

				optionLength = FindOption(args[i], "dates");
				if (optionLength > 0) {
					searchWord = false;

					if (args[i].substring(optionLength).equalsIgnoreCase("on")) {
						showDates = true;
					} else if (args[i].substring(optionLength).equalsIgnoreCase("off")) {
						showDates = false;
					} else {
						sender.sendMessage(ChatColor.RED + "Use dates:on or dates:off, not " + args[i]);
						return true;
					}
				}

				optionLength = FindOption(args[i], "reply");
				if (optionLength > 0) {
					searchWord = false;

					if (args[i].substring(optionLength).equalsIgnoreCase("on")) {
						showReplies = true;
					} else if (args[i].substring(optionLength).equalsIgnoreCase("off")) {
						showReplies = false;
					} else {
						sender.sendMessage(ChatColor.RED + "Use reply:on or reply:off, not " + args[i]);
						return true;
					}
				}

				optionLength = FindOption(args[i], "sort");
				if (optionLength > 0) {
					searchWord = false;

					if (args[i].substring(optionLength).equalsIgnoreCase("asc")) {
						sortDirection = "asc";
					} else if (args[i].substring(optionLength).equalsIgnoreCase("desc")) {
						sortDirection = "desc";
					} else {
						sender.sendMessage(ChatColor.RED + "Use sort:asc or sort:desc, not " + args[i]);
						return true;
					}
				}

				if (args[i].equalsIgnoreCase("-v") || args[i].equalsIgnoreCase("-verbose")) {
					// Verbose switch
					searchWord = false;
					showReplies = true;
					showDates = true;
				}

				if (searchWord) {
					if (searchPhrase.isEmpty())
						searchPhrase = args[i];
					else
						searchPhrase += " " + args[i];
				}
			}

			if (!ticketOwnerOverride.isEmpty()) {
				ticketOwner = ticketOwnerOverride;
			} else {
				ticketOwner = PossiblyAddWildcards(ticketOwner);
			}

			staffName = PossiblyAddWildcards(staffName);

			if (searchPhrase.isEmpty())
				searchPhrase = "%";
			else {
				String updatedSearchPhrase = PossiblyAddWildcards(searchPhrase);
				if (updatedSearchPhrase.length() > searchPhrase.length() ) {
					// Added % wildcards
					wildCardsAdded = true;
					searchPhrase = updatedSearchPhrase;
				}
			}

		} catch (Exception e) {
			sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
			return true;
		}

		// Query the database
		try {
			sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Matching tickets" + ChatColor.RESET + plugin.GOLD + " ]");

			if (ticketsToShow > maxRecordsToReturn) {
				ticketsToShow = maxRecordsToReturn;
				sender.sendMessage(plugin.AQUA + "Note: Maximum record count set to " + ticketsToShow);
			}

			if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
				con = plugin.mysql.getConnection();
			} else {
				con = service.getConnection();
			}

			stmt = con.createStatement();

			String sqlStatement =
					"SELECT * FROM SHT_Tickets " +
							"WHERE id >= ? AND id <= ? AND owner LIKE ? AND admin LIKE ? AND " +
							" (description LIKE ? OR userreply LIKE ? OR adminreply LIKE ?) AND ";

			String recentTimeStartDate = Utilities.DateToString(recentTimeStartMillisec, dateFormatter);

			if (dateRangeDefined) {


				if ((startDate.contains("00:00:00") && endDate.contains("00:00:00"))) {
					// Start and end dates do not contain a time component
					// Add 24 hours to the end date

					Date parsedDate = ParseDate(endDate);

					cal.setTime(parsedDate);
					cal.add(Calendar.DATE, 1);

					endDate = Utilities.DateToString(cal, dateFormatter);
				}

				if (dateRangeDefined && mostRecentTimeDefined)
					sqlStatement += "(date BETWEEN '" + startDate + "' AND '" + endDate + "' AND " +
							" date >= '" + recentTimeStartDate + "') ";
				else
					sqlStatement += "date BETWEEN '" + startDate + "' AND '" + endDate + "' ";
			} else {
				sqlStatement += "date >= '" + recentTimeStartDate + "' ";
			}

			sqlStatement += "ORDER BY id " + sortDirection + " LIMIT " + ticketsToShow + ";";

			// Uncomment to debug
			// sender.sendMessage(plugin.GOLD + "[ " + "Debug SQL" + " ]" + plugin.WHITE + sqlStatement);

			PreparedStatement statement = con.prepareStatement(sqlStatement);

			statement.setString(1, Utilities.NumToString(ticketIDStart));
			statement.setString(2, Utilities.NumToString(ticketIDEnd));
			statement.setString(3, ticketOwner);
			statement.setString(4, staffName);
			statement.setString(5, searchPhrase);
			statement.setString(6, searchPhrase);
			statement.setString(7, searchPhrase);

			rs = statement.executeQuery();

			Integer maxDescriptionLength = 100;
			Integer maxReplyLength = 150;

			int ticketsFound = 0;
			while (rs.next()) {
				ticketsFound++;

				Utilities.ShowTicketInfo(sender, rs, maxDescriptionLength, maxReplyLength, showDates, showReplies);
			}

			if (ticketsFound == 0) {
				int newMaxDays = (int) Math.ceil((System.currentTimeMillis() - recentTimeStartMillisec) / 1000.0 / 86400.0) * 2;

				String ticketContentDescription = "";
				if (!searchPhrase.isEmpty() && !searchPhrase.equalsIgnoreCase("%")) {
					if (wildCardsAdded)
						ticketContentDescription = " with text '" + searchPhrase.substring(1, searchPhrase.length() - 1) + "'";
					else
						ticketContentDescription = " matching '" + searchPhrase + "'";
				}

				if (dateRangeDefined)
					sender.sendMessage(plugin.GREEN + "No tickets found" + ticketContentDescription + "; change from: and to: or adjust the filters");
				else
					sender.sendMessage(plugin.GREEN + "No tickets found" + ticketContentDescription + "; try t:" + newMaxDays + "d or adjust the filters");

				sender.sendMessage(plugin.GREEN + "See also " + plugin.AQUA + "/findtickets help");
			} else if (ticketsFound > 2) {
				if (ticketsFound < ticketsToShow || ticketsToShow >= maxRecordsToReturn)
					sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " tickets found");
				else {
					int newMax = ticketsToShow * 2;
					if (newMax > maxRecordsToReturn)
						newMax = maxRecordsToReturn;

					sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " tickets found; use limit:" + newMax + " to see more");
				}
			}

			return true;

		} catch (Exception e) {
			sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
			return true;
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
				e.printStackTrace();
			}
		}

	}

	private int FindOption(String argument, String keyWord) {

		// optionLength is the length of the keyWord plus a colon; 0 if no match
		int optionLength = keyWord.length() + 1;

		if (argument.length() > optionLength && argument.startsWith(keyWord + ":"))
			return optionLength;
		else
			return 0;
	}

	/**
	 * Converts time string in the format 5d3h2m3s to millisecs and returns it as a long.
	 * using w(eeks), d(ays), h(ours), m(inutes) and s(econds) For example: 4d8m2s -> 4 days, 8 minutes and 2 seconds
	 *
	 * @param sender the command sender
	 * @param time   a string in the format
	 * @return long which is the number of milliseconds since January 1, 1970
	 */
	private long GetSearchStartTime(CommandSender sender, String time) {
		try {
			long timemillisec = TimeParser.parseStringtoMillisec(time);
			if (timemillisec > 0) {
				return System.currentTimeMillis() - timemillisec;
			}
		} catch (NumberFormatException e) {
			return 0;
		}
		return 0;
	}

	private Date ParseDate(String dateValue) {

		Date parsedDate = ParseDate(dateValue, "yyyy-MM-dd'T'HH:mm:ss");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = ParseDate(dateValue, "yyyy-MM-dd'T'HH:mm");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = ParseDate(dateValue, "yyyy-MM-dd HH:mm:ss");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = ParseDate(dateValue, "yyyy-MM-dd HH:mm");
		if (parsedDate != null) {
			return parsedDate;
		}

		parsedDate = ParseDate(dateValue, "yyyy-MM-dd");
		if (parsedDate != null) {
			return parsedDate;
		}

		return null;
	}

	private Date ParseDate(String dateValue, String dateFormat) {

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		try {
			Date parsedDate = sdf.parse(dateValue);
			return parsedDate;

		} catch (ParseException ex) {
			return null;
		}

	}

	private String PossiblyAddWildcards(String searchTerm) {
		if (searchTerm.contains("%"))
			return searchTerm;
		else
			return "%" + searchTerm + "%";
	}

	private void ShowSyntax(CommandSender sender, boolean allowPlayerNameFilter) {
		sender.sendMessage(plugin.GOLD + "[Supported arguments]");
		sender.sendMessage(plugin.GREEN + " t:30d" + plugin.WHITE + " - Created within the last 30 days");

		if (allowPlayerNameFilter)
			sender.sendMessage(plugin.GREEN + " p:PlayerName" + plugin.WHITE + " - Created by player (use % for wildcard)");

		sender.sendMessage(plugin.GREEN + " s:StaffName" + plugin.WHITE + " - Name of staff member who responded");
		sender.sendMessage(plugin.GREEN + " first:4000" + plugin.WHITE + " - First ticket ID is 4000");
		sender.sendMessage(plugin.GREEN + " last:4015" + plugin.WHITE + " - Last ticket ID is 4015");
		sender.sendMessage(plugin.GREEN + " from:StartDate" + plugin.WHITE + " - Earliest ticket date (year-month-day)");
		sender.sendMessage(plugin.GREEN + " to:EndDate" + plugin.WHITE + " - Latest ticket date (year-month-day)");
		sender.sendMessage(plugin.GREEN + " limit:10" + plugin.WHITE + " - Show up to 10 tickets");
		sender.sendMessage(plugin.GREEN + " sort:desc" + plugin.WHITE + " - Sort by descending date");
		sender.sendMessage(plugin.GREEN + " dates:on" + plugin.WHITE + " - Show ticket dates");
		sender.sendMessage(plugin.GREEN + " reply:on" + plugin.WHITE + " - Show replies to ticket");
		sender.sendMessage(plugin.GREEN + " -v      " + plugin.WHITE + " - Verbose mode; enables dates:on and reply:on");
		sender.sendMessage(plugin.AQUA + "Additional arguments are the word or phrase to find");
		sender.sendMessage(plugin.AQUA + "Allowed date formats: " + plugin.WHITE + "yyyy-MM-dd, yyyy-MM-ddThh:mm, or yyyy-MM-ddThh:mm:ss");
		sender.sendMessage(plugin.AQUA + "Defaults:  t:1d limit:10 sort:asc dates:off reply:off");
		sender.sendMessage(plugin.GOLD + "[Example searches]");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " grief");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " diamond pick");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " t:48h");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " t:30d lost");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " t:30d limit:5 sort:desc %turn%off%");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " first:7000 last:7015");
		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " from:2015-11-01 to:2015-11-15");

		if (allowPlayerNameFilter)
			sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " p:PlayerName");

		sender.sendMessage(plugin.GREEN + "/findtickets" + plugin.WHITE + " s:StaffName");
	}

}
