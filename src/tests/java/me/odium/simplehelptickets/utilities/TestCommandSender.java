package me.odium.simplehelptickets.utilities;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class TestCommandSender implements CommandSender {
    @Override
    public void sendMessage(String s) {
        System.out.println(s);
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String s : strings) {
            sendMessage(s);
        }
    }

    @Override
    public Server getServer() {
        return TestHelper.createServer();
    }

    @Override
    public String getName() {
        return "TEST_LOG";
    }

    @Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public boolean isPermissionSet(String s) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return TestHelper.createPA();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return TestHelper.createPA();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return TestHelper.createPA();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return TestHelper.createPA();
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
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }
}
