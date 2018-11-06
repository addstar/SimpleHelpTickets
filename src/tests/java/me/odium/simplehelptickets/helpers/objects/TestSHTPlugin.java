package me.odium.simplehelptickets.helpers.objects;

import me.odium.simplehelptickets.SimpleHelpTickets;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/11/2018.
 */
public class TestSHTPlugin extends SimpleHelpTickets {
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public String myGetPlayerName(String name) {
        return super.myGetPlayerName(name);
    }

    @Override
    protected File getFile() {
        return super.getFile();
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }
}
