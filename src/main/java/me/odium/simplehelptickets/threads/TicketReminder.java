package me.odium.simplehelptickets.threads;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.odium.simplehelptickets.manager.TicketManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;


/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 18/07/2017.
 */
public class TicketReminder implements Runnable {

    Cache<CommandSender, Integer> responses;
    TicketManager manager;


    public TicketReminder(TicketManager manager) {
        this.manager = manager;
        this.responses = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .build();
    }

    public void addResponse(CommandSender sender){
        Integer r = responses.getIfPresent(sender);
        if(r==null)r=0;
        responses.put(sender,r+1);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        responses.cleanUp();
        for (Player player: Bukkit.getOnlinePlayers()){
            if(player.hasPermission("sht.admin")){
                if(!responses.asMap().containsKey(player)){
                    manager.ShowAdminTickets(player);
                }
            }
        }
    }
}
