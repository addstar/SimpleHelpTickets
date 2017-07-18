package me.odium.simplehelptickets.listeners;

import me.odium.simplehelptickets.manager.TicketManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PListener implements Listener {

  TicketManager manager;

  public PListener(TicketManager manager) {
    this.manager = manager;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoin(PlayerJoinEvent event) {      
    Player player = event.getPlayer();
    manager.runOnJoin(player);
  }
}