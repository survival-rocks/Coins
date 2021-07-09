package me.justeli.coins.events;

import me.justeli.coins.Coins;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * Created by Eli on June 16, 2020.
 * Coins: me.justeli.coins.events
 */
public class BukkitEvents implements Listener
{
    private final Coins instance;

    public BukkitEvents (Coins instance)
    {
        this.instance = instance;
    }

    @EventHandler (ignoreCancelled = true)
    public void on (EntityPickupItemEvent e)
    {
        if (!(e.getEntity() instanceof Player))
            return;

        PickupEvent event = new PickupEvent((Player) e.getEntity(), e.getItem());
        instance.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            e.setCancelled(true);
    }
}
