package me.odium.simplehelptickets.commands;

import java.io.File;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;

public class SimpleHelpTicketCommand implements CommandExecutor {

	private final SimpleHelpTickets plugin;

	public SimpleHelpTicketCommand(SimpleHelpTickets plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length == 0) {
			plugin.displayHelp(sender);
			return true;
		} else if (args.length == 1) {
			switch (args[0].toLowerCase()){
				case "reload":
					if (player == null || player.hasPermission("sht.manager")) {
						// Reload Config
						File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
						if (!file.exists()) {
							sender.sendMessage(plugin.getMessage("NewConfig"));
							FileConfiguration cfg = plugin.getConfig();
							FileConfigurationOptions cfgOptions = cfg.options();
							cfgOptions.copyDefaults(true);
							cfgOptions.copyHeader(true);
							plugin.saveConfig();
						}
						plugin.reloadConfig();

						// Reload Output Config
						File file2 = new File(plugin.getDataFolder() + File.separator + "output.yml");
						if (!file2.exists()) {
							sender.sendMessage(plugin.getMessage("NewOutput"));
							FileConfiguration cfg = plugin.getOutputConfig();
							FileConfigurationOptions cfgOptions = cfg.options();
							cfgOptions.copyDefaults(true);
							cfgOptions.copyHeader(true);
							plugin.saveOutputConfig();

						}
						plugin.reloadOutputConfig();

						sender.sendMessage(plugin.getMessage("ConfigReloaded"));
						return true;
					} else {
						sender.sendMessage(plugin.getMessage("NoPermission"));
						return true;
					}
				case "check":
					if (player != null) player.sendMessage(ChatColor.RED + "This feature is disabled.");
					break;
				case "stopreminder":
					if(sender.hasPermission("sht.manager")) {
						plugin.disableReminder(sender);
					}else{
						sender.sendMessage(plugin.getMessage("NoPermission"));
					}
					break;
				case "startreminder":
					if(sender.hasPermission("sht.manager")) {
						plugin.enableReminder(sender);
					}else{
						sender.sendMessage(plugin.getMessage("NoPermission"));
					}
					break;
				case "runreminder":
					if(sender.hasPermission("sht.manager")) {
						plugin.forceReminderRun(sender);
					}else{
						sender.sendMessage(plugin.getMessage("NoPermission"));
					}
					break;
				case "help":
				default:
					displayHelp(sender);
					return false;
			}
		}
		return true;
	}

	private void displayHelp(CommandSender sender) {
		sender.sendMessage( "&cUsage:");
		sender.sendMessage("&c/sht check - disabled feature");
		if(sender.hasPermission("sht.manager")) {
			sender.sendMessage("&c/sht reload - reloads the plugin");
			sender.sendMessage("&c/sht stopreminder - turns of the reminder task");
			sender.sendMessage("&c/sht startreminder - turns on the reminder task");
			sender.sendMessage("&c/sht runreminder -  runs the reminder task immdediately");
		}
			sender.sendMessage("&c/sht help - shows this message");
	}


}