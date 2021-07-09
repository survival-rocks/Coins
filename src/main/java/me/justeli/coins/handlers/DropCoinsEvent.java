package me.justeli.coins.handlers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Eli on November 15, 2020.
 * Coins: me.justeli.coins.events
 */
public class DropCoinsEvent
        extends Event implements Cancellable
{
    private final Location dropLocation;
    private final Player player;
    private final EventType type;
    private final Entity killedEntity;

    public DropCoinsEvent (Location dropLocation, Player player, Entity killedEntity, EventType type)
    {
        this.dropLocation = dropLocation;
        this.player = player;
        this.killedEntity = killedEntity;
        this.type = type;
    }

    public Location getDropLocation ()
    {
        return dropLocation;
    }

    public Player getPlayer ()
    {
        return player;
    }

    public Entity getKilledEntity ()
    {
        return killedEntity;
    }

    public EventType getType ()
    {
        return type;
    }


    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    public HandlerList getHandlers ()
    {
        return handlers;
    }

    public static HandlerList getHandlerList ()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled ()
    {
        return cancelled;
    }

    @Override
    public void setCancelled (boolean cancel)
    {
        cancelled = cancel;
    }
}
