package me.justeli.coins.prevent;

import me.justeli.coins.Coins;
import me.justeli.coins.settings.CoinsConfig;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class UnfairMobs // done
        implements Listener
{
    private final MetadataValue metadataValue;

    public UnfairMobs (Coins instance)
    {
        this.metadataValue = new FixedMetadataValue(instance, 1);
    }

    @EventHandler
    public void spawners (CreatureSpawnEvent e)
    {
        if (CoinsConfig.disabledInWorld(e.getLocation().getWorld()))
            return;

        if (e.getSpawnReason() != SpawnReason.SPAWNER && e.getEntityType() != EntityType.CAVE_SPIDER)
            return;

        e.getEntity().setMetadata("fromSpawner", metadataValue);
    }

    @EventHandler
    public void slimeSplits (CreatureSpawnEvent e)
    {
        if (CoinsConfig.disabledInWorld(e.getLocation().getWorld()))
            return;

        if (e.getSpawnReason() != SpawnReason.SLIME_SPLIT)
            return;

        e.getEntity().setMetadata("fromSlimeSplit", metadataValue);
    }

    public static boolean fromSpawner (Entity entity)
    {
        return entity.hasMetadata("fromSpawner");
    }

    public static boolean fromSlimeSplit (Entity entity)
    {
        return entity.hasMetadata("fromSlimeSplit");
    }
}
