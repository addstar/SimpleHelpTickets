package me.odium.simplehelptickets.helpers;

import me.odium.simplehelptickets.helpers.objects.TestServer;
import me.odium.simplehelptickets.helpers.objects.TestWorld;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.objects.TicketLocation;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class TestHelper {

    static TestServer instance = new TestServer();
    ;

    public static Server getServer() {
        return instance;
    }

    public static Plugin createPlugin() {
        return new Plugin() {
            @Override
            public File getDataFolder() {
                return new File(".");
            }

            @Override
            public PluginDescriptionFile getDescription() {
                return null;
            }

            @Override
            public FileConfiguration getConfig() {
                return null;
            }

            @Override
            public InputStream getResource(String s) {
                return null;
            }

            @Override
            public void saveConfig() {

            }

            @Override
            public void saveDefaultConfig() {

            }

            @Override
            public void saveResource(String s, boolean b) {

            }

            @Override
            public void reloadConfig() {

            }

            @Override
            public PluginLoader getPluginLoader() {
                return null;
            }

            @Override
            public Server getServer() {
                return instance;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void onDisable() {

            }

            @Override
            public void onLoad() {

            }

            @Override
            public void onEnable() {

            }

            @Override
            public boolean isNaggable() {
                return false;
            }

            @Override
            public void setNaggable(boolean b) {

            }

            @Override
            public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
                return null;
            }

            @Override
            public Logger getLogger() {
                return Logger.getLogger("TEST");
            }

            @Override
            public String getName() {
                return "TEST_PLUGIN";
            }

            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
                return true;
            }

            @Override
            public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
                return new ArrayList<>();
            }
        };
    }

    public static PermissionAttachment createPA() {
        return new PermissionAttachment(null, new Permissible() {
            @Override
            public boolean isPermissionSet(String s) {
                return false;
            }

            @Override
            public boolean isPermissionSet(Permission permission) {
                return false;
            }

            @Override
            public boolean hasPermission(String s) {
                return false;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return false;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int i) {
                return null;
            }

            @Override
            public void removeAttachment(PermissionAttachment permissionAttachment) {

            }

            @Override
            public void recalculatePermissions() {

            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return null;
            }

            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }
        });
    }
    
    public static Ticket createTestTicket(boolean withid, Player sender) {
        TicketLocation tLoc = new TicketLocation(sender.getLocation(), "TEST_SERVER");
        if (withid) {
            Ticket ticket = new Ticket(1, sender.getUniqueId(), "This is a Test ticket",
                    new Date(System.currentTimeMillis()), tLoc);
            ticket.setOwnerName(sender.getDisplayName());
            return ticket;
        }
        Ticket ticket = new Ticket(sender.getUniqueId(), "This is a Test ticket",
                new Date(System.currentTimeMillis()), tLoc);
        ticket.setOwnerName(sender.getDisplayName());
        return ticket;
    }
    
    public static TestWorld createTestWorld() {
        return new TestWorld();
    }

}
