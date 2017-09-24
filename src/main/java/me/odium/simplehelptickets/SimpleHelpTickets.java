package me.odium.simplehelptickets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.odium.simplehelptickets.commands.checkticket;
import me.odium.simplehelptickets.commands.closeticket;
import me.odium.simplehelptickets.commands.delticket;
import me.odium.simplehelptickets.commands.purgetickets;
import me.odium.simplehelptickets.commands.replyticket;
import me.odium.simplehelptickets.commands.replyclose;
import me.odium.simplehelptickets.commands.sht;
import me.odium.simplehelptickets.commands.taketicket;
import me.odium.simplehelptickets.commands.ticket;
import me.odium.simplehelptickets.commands.tickets;
import me.odium.simplehelptickets.commands.findtickets;
import me.odium.simplehelptickets.listeners.PListener;

import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.threads.TicketReminder;
import me.odium.test.SimpleMailPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.bc.BungeeChat;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.scheduler.BukkitTask;

public class SimpleHelpTickets extends JavaPlugin {

	public Logger log;

	public ChatColor GREEN = ChatColor.GREEN;
	public ChatColor RED = ChatColor.RED;
	public ChatColor GOLD = ChatColor.GOLD;
	public ChatColor GRAY = ChatColor.GRAY;
	public ChatColor WHITE = ChatColor.WHITE;
	public ChatColor AQUA = ChatColor.AQUA;
	public ChatColor BLUE = ChatColor.BLUE;

	DBConnection service = DBConnection.getInstance();
	TicketManager manager;
	public TicketReminder reminder;
	private BukkitTask reminderTask;
	public boolean useMail =false;
	public SimpleMailPlugin mailPlugin;


    public TicketManager getManager() {
        return manager;
    }
	// Custom Config
	private FileConfiguration OutputConfig = null;
	private File OutputConfigFile = null;

	public void reloadOutputConfig() {
		if (OutputConfigFile == null) {
			OutputConfigFile = new File(getDataFolder(), "output.yml");
		}
		OutputConfig = YamlConfiguration.loadConfiguration(OutputConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("output.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
			OutputConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getOutputConfig() {
		if (OutputConfig == null) {
			reloadOutputConfig();
		}
		return OutputConfig;
	}

	public void saveOutputConfig() {
		if (OutputConfig == null || OutputConfigFile == null) {
			return;
		}
		try {
			OutputConfig.save(OutputConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + OutputConfigFile, ex);
		}
	}

	// End Custom Config

	public String replaceColorMacros(String str) {
		str = str.replace("&r", ChatColor.RED.toString());
		str = str.replace("&R", ChatColor.DARK_RED.toString());
		str = str.replace("&y", ChatColor.YELLOW.toString());
		str = str.replace("&Y", ChatColor.GOLD.toString());
		str = str.replace("&g", ChatColor.GREEN.toString());
		str = str.replace("&G", ChatColor.DARK_GREEN.toString());
		str = str.replace("&c", ChatColor.AQUA.toString());
		str = str.replace("&C", ChatColor.DARK_AQUA.toString());
		str = str.replace("&b", ChatColor.BLUE.toString());
		str = str.replace("&B", ChatColor.DARK_BLUE.toString());
		str = str.replace("&p", ChatColor.LIGHT_PURPLE.toString());
		str = str.replace("&P", ChatColor.DARK_PURPLE.toString());
		str = str.replace("&0", ChatColor.BLACK.toString());
		str = str.replace("&1", ChatColor.DARK_GRAY.toString());
		str = str.replace("&2", ChatColor.GRAY.toString());
		str = str.replace("&w", ChatColor.WHITE.toString());
		str = str.replace("%bold", ChatColor.BOLD.toString());
		str = str.replace("%italic", ChatColor.ITALIC.toString());
		str = str.replace("%underline", ChatColor.UNDERLINE.toString());
		str = str.replace("%strike", ChatColor.STRIKETHROUGH.toString());
		str = str.replace("%reset", ChatColor.RESET.toString());
		return str;
	}

	public MySQLConnection mysql;

	public void onEnable() {
	    log = Bukkit.getLogger();
		log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
		FileConfiguration cfg = getConfig();
		FileConfigurationOptions cfgOptions = cfg.options();
		cfgOptions.copyDefaults(true);
		cfgOptions.copyHeader(true);
		saveConfig();
		// Load Custom Config
		FileConfiguration ccfg = getOutputConfig();
		FileConfigurationOptions ccfgOptions = ccfg.options();
		ccfgOptions.copyDefaults(true);
		ccfgOptions.copyHeader(true);
		saveOutputConfig();
		manager =  new TicketManager(this);
		// declare new listener
		PListener listener = new PListener(manager);
		Bukkit.getServer().getPluginManager().registerEvents(listener,this);
		// declare executors
		this.getCommand("sht").setExecutor(new sht(this));
		this.getCommand("ticket").setExecutor(new ticket(this));
		this.getCommand("tickets").setExecutor(new tickets(this));
		this.getCommand("checkticket").setExecutor(new checkticket(this));
		this.getCommand("replyticket").setExecutor(new replyticket(this));
		this.getCommand("replyclose").setExecutor(new replyclose(this));
		this.getCommand("taketicket").setExecutor(new taketicket(this));
		this.getCommand("delticket").setExecutor(new delticket(this));
		this.getCommand("closeticket").setExecutor(new closeticket(this));
		this.getCommand("purgetickets").setExecutor(new purgetickets(this));
		this.getCommand("findtickets").setExecutor(new findtickets(this));

		// If MySQL
		if (this.getConfig().getBoolean("MySQL.USE_MYSQL")) {
			// Get Database Details
			String hostname = this.getConfig().getString("MySQL.hostname");
			String hostport = this.getConfig().getString("MySQL.hostport");
			String database = this.getConfig().getString("MySQL.database");
			String user = this.getConfig().getString("MySQL.user");
			String password = this.getConfig().getString("MySQL.password");
			// Get Connection
			mysql = new MySQLConnection(hostname, hostport, database, user, password);
			// Open Connection
			try {
				mysql.open();
				log.info("[SimpleHelpTickets] Connected to MySQL Database");
				mysql.createTable();
			} catch (Exception e) {
				log.info(e.getMessage());
			}

		} else {
			// Create connection & table
			try {
				service.setPlugin(this);
				service.setConnection();
				service.createTable();
			} catch (Exception e) {
				log.info("[SimpleHelpTickets] " + "Error: " + e);
			}
		}
		if(this.getConfig().getBoolean("useSimpleMail", false)){
            Plugin plugin = Bukkit.getPluginManager().getPlugin("SimpleMail");
            if(plugin != null && plugin instanceof SimpleMailPlugin){
                mailPlugin = (SimpleMailPlugin) plugin;
                useMail = true;
            }
        }
		// Check for and delete any expired tickets, display progress.
		log.info("[SimpleHelpTickets] " + expireTickets() + " Expired Tickets Deleted");

		if (getConfig().getBoolean("BungeeChatIntegration", false)) {
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}
		if(this.getConfig().getBoolean("TicketReminder.SetReminder",true)) {
			enableReminder();
		}
	}

	public void onDisable() {
		// Check for and delete any expired tickets, display progress.
		int expiredTicketCount = expireTickets();
		int expiredIdeaCount = expireIdeas();

		if (expiredTicketCount == 0 && expiredIdeaCount == 0) {
			log.info("[SimpleHelpTickets] No expired tickets or ideas were found");
		} else {
			log.info("[SimpleHelpTickets] " + expiredTicketCount + " Expired Tickets Deleted");
			log.info("[SimpleHelpTickets] " + expiredIdeaCount + " Expired Ideas Deleted");
		}
        if(this.getConfig().getBoolean("TicketReminder.SetReminder",true)) {
            disableReminder(null);
        }
		service.closeConnection();

		// mysql.close();

		log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled.");
	}

	public String myGetPlayerName(String name) {
		Player caddPlayer = getServer().getPlayerExact(name);
		String pName;
		if (caddPlayer == null) {
			caddPlayer = getServer().getPlayer(name);
			if (caddPlayer == null) {
				pName = name;
			} else {
				pName = caddPlayer.getName();
			}
		} else {
			pName = caddPlayer.getName();
		}
		return pName;
	}

	public String getCurrentDTG(String string) {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat dtgFormat = null;
		if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
			dtgFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		} else {
			dtgFormat = new SimpleDateFormat("dd/MMM/yy HH:mm");
		}
		return dtgFormat.format(currentDate.getTime());
	}

	public String getExpiration(String date) {
		int expire = getConfig().getInt("TicketExpiration");
		// for (char c : ticketExpiration.toCharArray()) {
		// if (!Character.isDigit(c)) {
		// ticketExpiration = "7";
		// }
		// }
		SimpleDateFormat dtgFormat = null;
		// int expire = Integer.parseInt(ticketExpiration);
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		cal.add(Calendar.DAY_OF_WEEK, expire);
		java.util.Date expirationDate = cal.getTime();
		if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
			dtgFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		} else {
			dtgFormat = new SimpleDateFormat("dd/MMM/yy HH:mm");
		}
		return dtgFormat.format(expirationDate);
	}

	public int expireTickets() {
		return expireItems(Utilities.TICKET_TABLE_NAME);
	}

	public int expireIdeas() {
		return expireItems(Utilities.IDEA_TABLE_NAME);
	}

	private int expireItems(String targetTable) {
		ResultSet rs = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Connection con = null;
		int expirations = 0;
		Date dateNEW;
		Date expirationNEW;
		try {
			if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
				con = mysql.getConnection();
			} else {
				con = service.getConnection();
			}
			stmt = con.createStatement();
			stmt2 = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + targetTable);
			while (rs.next()) {
				String exp = rs.getString("expiration");
				String id = rs.getString("id");
				// IF AN EXPIRATION HAS BEEN APPLIED
				if (exp != null) {
					// CONVERT DATE-STRINGS FROM DB TO DATES
					if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
						Date date = rs.getTimestamp("date");
						Date expiration = rs.getTimestamp("expiration");
						dateNEW = date;
						expirationNEW = expiration;
					} else {
						String date = rs.getString("date");
						String expiration = rs.getString("expiration");

						dateNEW = new SimpleDateFormat("dd/MMM/yy HH:mm", Locale.ENGLISH).parse(getCurrentDTG(date));
						expirationNEW = new SimpleDateFormat("dd/MMM/yy HH:mm", Locale.ENGLISH).parse(expiration);
					}

					// COMPARE STRINGS
					int HasExpired = dateNEW.compareTo(expirationNEW);
					if (HasExpired >= 0) {
						expirations++;
						stmt2.executeUpdate("DELETE FROM " + targetTable + " WHERE id='" + id + "'");
					}
				}
			}
			// return expirations;
		} catch (Exception e) {
			log.info("[SimpleHelpTickets] " + "Error: " + e);
		} finally {
			try {
				if (rs != null) { rs.close(); rs = null; }
				if (stmt != null) { stmt.close(); stmt = null; }
				if (stmt2 != null) { stmt2.close(); stmt2 = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
				e.printStackTrace();
			}
		}
		return expirations;
	}

	public void displayHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "Tickets" + " v" + getDescription().getVersion() + ChatColor.RESET + ChatColor.GOLD + " ]");
		sender.sendMessage(getMessage("UserCommandsMenu-helptickets"));
		sender.sendMessage(getMessage("UserCommandsMenu-helpme"));
		sender.sendMessage(getMessage("UserCommandsMenu-Title"));
		sender.sendMessage(getMessage("UserCommandsMenu-ticket"));
		sender.sendMessage(getMessage("UserCommandsMenu-tickets"));
		sender.sendMessage(getMessage("UserCommandsMenu-checkticket"));
		sender.sendMessage(getMessage("UserCommandsMenu-replyticket"));
		sender.sendMessage(getMessage("UserCommandsMenu-closeticket"));
		sender.sendMessage(getMessage("UserCommandsMenu-delticket"));
		sender.sendMessage(getMessage("UserCommandsMenu-idea"));
		sender.sendMessage(getMessage("UserCommandsMenu-ideacommands"));

		if (sender == null || sender.hasPermission("sht.admin")) {
			sender.sendMessage(getMessage("AdminCommandsMenu-Title"));
			sender.sendMessage(getMessage("AdminCommandsMenu-tickets"));
			sender.sendMessage(getMessage("AdminCommandsMenu-taketicket"));
			sender.sendMessage(getMessage("AdminCommandsMenu-replyticket"));
			sender.sendMessage(getMessage("AdminCommandsMenu-closeticket"));
			sender.sendMessage(getMessage("AdminCommandsMenu-replyclose"));
			sender.sendMessage(getMessage("AdminCommandsMenu-delticket"));
			sender.sendMessage(getMessage("AdminCommandsMenu-purgeticket"));
			sender.sendMessage(getMessage("AdminCommandsMenu-reload"));
			sender.sendMessage(getMessage("AdminCommandsMenu-ideacommands"));
			sender.sendMessage(getMessage("AdminCommandsMenu-aliases"));
		}
	}

	public String getMessage(String phrase) {
		String prefix;
		String output;
		String message;

		switch (phrase) {
			case "AdminCommandsMenu-reload":
				prefix = ChatColor.DARK_AQUA + " /helptickets reload";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-reload"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-purgeticket":
				prefix = ChatColor.DARK_AQUA + " /purgetickets [-c|-a]";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-purgeticket"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-delticket":
				prefix = ChatColor.DARK_AQUA + " /delticket <#>";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-delticket"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-closeticket":
				prefix = ChatColor.DARK_AQUA + " /closeticket [-r] <#>";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-closeticket"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-replyticket":
				prefix = ChatColor.DARK_AQUA + " /replyticket <#> <Reply>";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-replyticket"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-replyclose":
				prefix = ChatColor.DARK_AQUA + " /replyclose <#> <Reply>";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-replyclose"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-taketicket":
				prefix = ChatColor.DARK_AQUA + " /taketicket <#>";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-taketicket"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-gototicket":
				prefix = ChatColor.DARK_AQUA + " /gototicket <#>";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-gototicket"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-tickets":
				prefix = ChatColor.DARK_AQUA + " /tickets [-v|-a|-c]";
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-tickets"));
				message = prefix + output;
				return message;
			case "AdminCommandsMenu-Title":
				output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-Title"));
				message = output;
				return message;
			case "AdminCommandsMenu-ideacommands":
				output = ChatColor.DARK_AQUA + " /checkidea, /takeidea, /closeidea, /replycloseidea";
				message = output;
				return message;
			case "AdminCommandsMenu-aliases":
				output = ChatColor.BLUE + " Aliases:\n" +
						"   Check with /chticket or /cht\n" +
						"   Reply with /rticket\n" +
						"   Close with /clt\n" +
						"   Replyclose with /rct";
				message = output;
				return message;
			case "UserCommandsMenu-delticket":
				prefix = ChatColor.GREEN + " /delticket <#>";
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-delticket"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-closeticket":
				prefix = ChatColor.GREEN + " /closeticket <#>";
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-closeticket"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-replyticket":
				prefix = ChatColor.GREEN + " /replyticket <#> <Reply>";
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-replyticket"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-checkticket":
				prefix = ChatColor.GREEN + " /checkticket <#>";
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-checkticket"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-tickets":
				prefix = ChatColor.GREEN + " /tickets";
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-tickets"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-ticket":
				prefix = ChatColor.GREEN + " /ticket <Description>";
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-ticket"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-Title":
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-Title"));
				message = output;
				return message;
			case "UserCommandsMenu-helpme":
				prefix = ChatColor.DARK_GREEN + " /helpme" + ChatColor.WHITE;
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-helpme"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-helptickets":
				prefix = ChatColor.DARK_GREEN + " /helptickets" + ChatColor.WHITE;
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-helptickets"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-idea":
				prefix = ChatColor.DARK_GREEN + " /idea" + ChatColor.WHITE;
				output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-idea"));
				message = prefix + output;
				return message;
			case "UserCommandsMenu-ideacommands":
				output = ChatColor.DARK_GREEN + " /ideas, /checkidea, /replyidea, /closeidea, /delidea";
				message = output;
				return message;
			case "InvalidTicketNumber":
			case "InvalidIdeaNumber":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "Error":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("Error"));
				message = prefix + output;
				return message;
			case "TicketNotExist":
			case "IdeaNotExist":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "NotYourTicketToCheck":
			case "NotYourIdeaToCheck":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "NotYourTicketToClose":
			case "NotYourIdeaToClose":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "NewConfig":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("NewConfig"));
				message = prefix + output;
				return message;
			case "NewOutput":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("NewOutput"));
				message = prefix + output;
				return message;
			case "ConfigReloaded":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("ConfigReloaded"));
				message = prefix + output;
				return message;
			case "NotEnoughInformation":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("NotEnoughInformation"));
				message = prefix + output;
				return message;
			case "NoPermission":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("NoPermission"));
				message = prefix + output;
				return message;
			case "TicketAlreadyClosed":
			case "IdeaAlreadyClosed":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketClosed":
			case "IdeaClosed":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "IdeaClosedMail":
			case "TicketClosedMail":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketClosedOWNER":
			case "IdeaClosedOWNER":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketClosedADMIN":
			case "IdeaClosedADMIN":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketNotClosed":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("TicketNotClosed"));
				message = prefix + output;
				return message;
			case "TicketReopened":
			case "IdeaReopened":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketReopenedOWNER":
			case "IdeaReopenedOWNER":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketReopenedADMIN":
			case "IdeaReopenedADMIN":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "AllClosedTicketsPurged":
			case "AllClosedIdeasPurged":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "AllTicketsPurged":
			case "AllIdeasPurged":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "AdminRepliedToTicket":
			case "AdminRepliedToIdea":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "AdminRepliedToTicketOWNER":
			case "AdminRepliedToIdeaOWNER":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "UserRepliedToTicket":
			case "UserRepliedToIdea":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "CannotTakeClosedTicket":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("CannotTakeClosedTicket"));
				message = prefix + output;
				return message;
			case "InvalidWorld":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString("InvalidWorld"));
				message = prefix + output;
				return message;
			case "TakeTicketOWNER":
			case "TakeIdeaOWNER":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TakeTicketADMIN":
			case "TakeIdeaADMIN":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketOpen":
			case "IdeaOpen":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketOpenADMIN":
			case "IdeaOpenADMIN":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketMax":
			case "IdeaMax":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "TicketTooSoon":
			case "IdeaTooSoon":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "NoTickets":
			case "NoIdeas":
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = output;
				return message;
			case "AdminJoin":
			case "AdminJoinIdeas":
				prefix = replaceColorMacros(getOutputConfig().getString("Prefix"));
				output = replaceColorMacros(getOutputConfig().getString(phrase));
				message = prefix + output;
				return message;
			case "UserJoin":
				prefix = ChatColor.GOLD + "* ";
				output = replaceColorMacros(getOutputConfig().getString("UserJoin"));
				message = prefix + output;
				return message;
			case "UserJoin-TicketReplied":
				prefix = ChatColor.GOLD + "* ";
				output = replaceColorMacros(getOutputConfig().getString("UserJoin-TicketReplied"));
				message = prefix + output;
				return message;
			case "HelpMe_Line1":
				output = replaceColorMacros(getOutputConfig().getString("HelpMe_Line1"));
				message = output;
				return message;
			case "HelpMe_Line2":
				output = replaceColorMacros(getOutputConfig().getString("HelpMe_Line2"));
				message = output;
				return message;
            default:
                return "Error: unknown message name";

		}
	}

	public void notifyAdmins(String msg, CommandSender sender) {
		//System.out.println("notifyAdmins(): => " + msg);
		UUID uuid = null;
		if (sender instanceof Player) {
			Player ply = (Player) sender;
			uuid = ply.getUniqueId();
		}

		if (getConfig().getBoolean("BungeeChatIntegration", false)) {
			// Ensure staff see the message regardless of which server they are on
			BungeeChat.mirrorChat(msg, getConfig().getString("BungeeChatStaffChannel"));
		}

		// Send to all players with permission sht.admin
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();

		for (Player p : players) {
			// Only notify ticket admins, but DO NOT notify the "excluded"
			// player
			// (This is in case the command executor needs to be given a
			// different message)
			if (p.hasPermission("sht.admin") && ((uuid == null) || (!p.getUniqueId().equals(uuid)))) {
				p.sendMessage(msg);
			}
		}
	}

	public void notifyUser(String msg, String player) {
		// System.out.println("notifyUser(): " + player + " => " + msg);
		Player ply = Bukkit.getPlayerExact(player);
		if (ply != null) {
			ply.sendMessage(msg);
		} else {
			if (getConfig().getBoolean("BungeeChatIntegration", false)) {
				SendPluginMessage("Message", player, msg);
			}
		}
	}

	public void SendPluginMessage(String subchannel, String data1, String data2) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(subchannel);
		out.writeUTF(data1);
		out.writeUTF(data2);
		Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
		if (player == null)
		{
			// No players are online; cannot send a message to the player using BungeeCord
		} else {
			player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		}
	}

	public void disableReminder(){
	    disableReminder(null);
    }

	public void disableReminder(CommandSender sender){
	    try {
            reminderTask.cancel();
            reminder = null;
        }catch (IllegalStateException e){
	        if(sender != null){
	            sender.sendMessage("Reminder Task was not scheduled");
            }else{
	            log.severe("SHT Reminder Task was cancelled but never scheduled");
            }
        }

    }
    public void enableReminder(){
	    enableReminder(null);
    }


    public void enableReminder(CommandSender sender){
        reminder = new TicketReminder(manager);
        long minutes = this.getConfig().getLong("TicketReminder.PeriodInMinutes");
        long period = minutes * 60 * 60;
        reminderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, reminder, period, period);
        if(sender != null){
           sender.sendMessage("Reminder has been scheduled to run in " + period + " minutes");
        }else{
            log.info("SHT Reminder scheduled: "+ period + " minutes");
        }
    }

    public void forceReminderRun(CommandSender sender){
        sender.sendMessage("Running SHT Reminder for all online");
        Bukkit.getScheduler().runTaskAsynchronously(this, reminder);
    }

    public void sendMailOnClose(CommandSender sender, String target, String message) {
        if (!useMail) return;
        String senderUsername = sender.getName();
        UUID senderUUID = null;
        if (sender instanceof Player) {
            senderUUID = ((Player)sender).getUniqueId();
        }

        try {
            mailPlugin.SendMailMessage(sender, senderUsername, senderUUID, target, message);
            sender.sendMessage(replaceColorMacros("[Tickets] &wMail has been sent to " + target + " informing them: ") + message);
        } catch (Exception ex) {
            sender.sendMessage(replaceColorMacros("[Tickets] &wUnable to send mail to " + target + " informing them of the closed item: ") + ex.getMessage());
        }

    }

}
