package me.justeli.coins.events;

import me.justeli.coins.settings.Config;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

/**
 * Created by Eli on June 15, 2020.
 * Coins: me.justeli.coins.events
 */
public class PaperEvents implements Listener
{
    @EventHandler
    public void on (PlayerAttemptPickupItemEvent e)
    {
        PickupEvent event = new PickupEvent(e.getPlayer(), e.getItem());
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled() && !Config.get(Config.BOOLEAN.PICKUP_EFFECT))
            e.setCancelled(true);
    }
}
