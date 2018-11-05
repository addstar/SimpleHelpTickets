package me.odium.simplehelptickets.commands;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.odium.simplehelptickets.SimpleHelpTickets;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.utilities.TimeParser;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FindTicketsCommand implements CommandExecutor {

    private final SimpleHelpTickets plugin;

    public FindTicketsCommand(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

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
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            if (ticketOwnerOverride.isEmpty())
                ShowSyntax(sender, true);
            else
                ShowSyntax(sender, false);

            return true;
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

        StringBuilder searchPhrase = new StringBuilder();
        boolean wildCardsAdded = false;

        boolean showDates = false;
        boolean showReplies = false;

        int ticketsToShow = 10;

        int maxRecordsToReturn;
        if (atConsole)
            maxRecordsToReturn = 500;
        else
            maxRecordsToReturn = 100;
        try {

            for (String arg : args) {
                boolean searchWord = true;

                // optionLength is the length of the option, including the colon; 0 if no match
                int optionLength;

                // Match both o: and p: for ticket owner/player
                optionLength = FindOption(arg, "o");
                if (optionLength == 0)
                    optionLength = FindOption(arg, "p");

                if (optionLength > 0) {
                    searchWord = false;
                    ticketOwner = arg.substring(optionLength);
                }

                // Match both s: and a: for staff/admin responder
                optionLength = FindOption(arg, "s");
                if (optionLength == 0)
                    optionLength = FindOption(arg, "a");

                if (optionLength > 0) {
                    // Staff filter (aka admin filter)
                    searchWord = false;
                    staffName = arg.substring(optionLength);
                }

                optionLength = FindOption(arg, "t");
                if (optionLength > 0) {
                    searchWord = false;

                    String timeSpec = arg.substring(optionLength);

                    recentTimeStartMillisec = GetSearchStartTime(sender, timeSpec);
                    if (recentTimeStartMillisec == 0) {
                        sender.sendMessage(ChatColor.RED + "Invalid time interval, " + arg);
                        return true;
                    }

                    mostRecentTimeDefined = true;
                }

                optionLength = FindOption(arg, "from");
                if (optionLength > 0) {
                    searchWord = false;

                    String datePart = arg.substring(optionLength);
                    Date parsedDate = Utilities.parseDate(datePart);

                    if (parsedDate == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid date value, " + arg);
                        sender.sendMessage(ChatColor.AQUA + "Example dates: 2015-11-15 or 2015-11-15T3:00");
                        return true;
                    }

                    startDate = Utilities.DateToString(parsedDate, dateFormatter);
                    dateRangeDefined = true;
                }

                optionLength = FindOption(arg, "to");
                if (optionLength > 0) {
                    searchWord = false;

                    String datePart = arg.substring(optionLength);
                    Date parsedDate = Utilities.parseDate(datePart);

                    if (parsedDate == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid date value, " + arg);
                        sender.sendMessage(ChatColor.AQUA + "Example dates: 2015-11-15 or 2015-11-15T3:00");
                        return true;
                    }

                    endDate = Utilities.DateToString(parsedDate, dateFormatter);
                    dateRangeDefined = true;
                }

                optionLength = FindOption(arg, "first");
                if (optionLength > 0) {
                    searchWord = false;

                    try {
                        ticketIDStart = Integer.parseInt(arg.substring(optionLength));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid starting ID, " + arg);
                        return true;
                    }
                }

                optionLength = FindOption(arg, "last");
                if (optionLength > 0) {
                    searchWord = false;

                    try {
                        ticketIDEnd = Integer.parseInt(arg.substring(optionLength));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid ending ID, " + arg);
                        return true;
                    }
                }

                optionLength = FindOption(arg, "limit");
                if (optionLength > 0) {
                    searchWord = false;

                    try {
                        ticketsToShow = Integer.parseInt(arg.substring(optionLength));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid record count, " + arg);
                        return true;
                    }
                }

                optionLength = FindOption(arg, "dates");
                if (optionLength > 0) {
                    searchWord = false;

                    if (arg.substring(optionLength).equalsIgnoreCase("on")) {
                        showDates = true;
                    } else if (arg.substring(optionLength).equalsIgnoreCase("off")) {
                        showDates = false;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Use dates:on or dates:off, not " + arg);
                        return true;
                    }
                }

                optionLength = FindOption(arg, "reply");
                if (optionLength > 0) {
                    searchWord = false;

                    if (arg.substring(optionLength).equalsIgnoreCase("on")) {
                        showReplies = true;
                    } else if (arg.substring(optionLength).equalsIgnoreCase("off")) {
                        showReplies = false;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Use reply:on or reply:off, not " + arg);
                        return true;
                    }
                }

                optionLength = FindOption(arg, "sort");
                if (optionLength > 0) {
                    searchWord = false;

                    if (arg.substring(optionLength).equalsIgnoreCase("asc")) {
                        sortDirection = "asc";
                    } else if (arg.substring(optionLength).equalsIgnoreCase("desc")) {
                        sortDirection = "desc";
                    } else {
                        sender.sendMessage(ChatColor.RED + "Use sort:asc or sort:desc, not " + arg);
                        return true;
                    }
                }

                if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("-verbose")) {
                    // Verbose switch
                    searchWord = false;
                    showReplies = true;
                    showDates = true;
                }

                if (searchWord) {
                    if (searchPhrase.length() == 0)
                        searchPhrase = new StringBuilder(arg);
                    else
                        searchPhrase.append(" ").append(arg);
                }
            }

            if (!ticketOwnerOverride.isEmpty()) {
                ticketOwner = ticketOwnerOverride;
            } else {
                ticketOwner = PossiblyAddWildcards(ticketOwner);
            }
            staffName = PossiblyAddWildcards(staffName);
            if (searchPhrase.length() == 0)
                searchPhrase = new StringBuilder("%");
            else {
                String updatedSearchPhrase = PossiblyAddWildcards(searchPhrase.toString());
                if (updatedSearchPhrase.length() > searchPhrase.length()) {
                    // Added % wildcards
                    wildCardsAdded = true;
                    searchPhrase = new StringBuilder(updatedSearchPhrase);
                }
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
            return true;
        }


        // Use the command name to determine if we are working with a ticket or an idea
        Table table = TicketManager.getTableFromCommandString(label);
        String itemNamePlural = table.tableName + "s";
        sender.sendMessage(plugin.GOLD + "[ " + plugin.WHITE + ChatColor.BOLD + "Matching " + itemNamePlural + ChatColor.RESET + plugin.GOLD + " ]");

        if (ticketsToShow > maxRecordsToReturn) {
            ticketsToShow = maxRecordsToReturn;
            sender.sendMessage(plugin.AQUA + "Note: Maximum record count set to " + ticketsToShow);
        }
        // Query the database
        List<Ticket> found = plugin.getManager().findTickets(
                table,
                ticketOwner,
                staffName,
                mostRecentTimeDefined,
                dateRangeDefined,
                dateFormatter,
                startDate, endDate, cal, searchPhrase.toString(), sortDirection, ticketsToShow, ticketIDStart, ticketIDEnd);
        try {
            Integer maxDescriptionLength = 100;
            Integer maxReplyLength = 150;

            int ticketsFound = found.size();
            for (Ticket ticket : found) {
                Utilities.ShowTicketInfo(sender, ticket, maxDescriptionLength, maxReplyLength, showDates, showReplies);
            }
            if (ticketsFound == 0) {
                int newMaxDays = (int) Math.ceil((System.currentTimeMillis() - recentTimeStartMillisec) / 1000.0 / 86400.0) * 2;

                String ticketContentDescription = "";
                if ((searchPhrase.length() > 0) && !searchPhrase.toString().equalsIgnoreCase("%")) {
                    if (wildCardsAdded)
                        ticketContentDescription = " with text '" + searchPhrase.substring(1, searchPhrase.length() - 1) + "'";
                    else
                        ticketContentDescription = " matching '" + searchPhrase + "'";
                }

                if (dateRangeDefined)
                    sender.sendMessage(plugin.GREEN + "No " + itemNamePlural + " found" + ticketContentDescription + "; change from: and to: or adjust the filters");
                else
                    sender.sendMessage(plugin.GREEN + "No " + itemNamePlural + " found" + ticketContentDescription + "; try t:" + newMaxDays + "d or adjust the filters");

                sender.sendMessage(plugin.GREEN + "See also " + plugin.AQUA + "/find" + itemNamePlural + " help");

            } else if (ticketsFound > 2) {
                if (ticketsFound < ticketsToShow || ticketsToShow >= maxRecordsToReturn)
                    sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " " + Utilities.CheckPlural(itemNamePlural, ticketsFound) + " found");
                else {
                    int newMax = ticketsToShow * 2;
                    if (newMax > maxRecordsToReturn)
                        newMax = maxRecordsToReturn;

                    sender.sendMessage(plugin.GREEN + Utilities.NumToString(ticketsFound) + " " + itemNamePlural + " found; use limit:" + newMax + " to see more");
                }
            }

            return true;

        } catch (Exception e) {
            sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
            return true;
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
        sender.sendMessage(plugin.GOLD + "For ideas use " + plugin.GREEN + "/findideas");
    }

}
