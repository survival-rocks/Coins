package me.justeli.coins.cancel;

import me.justeli.coins.settings.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PreventSpawner
        implements Listener
{
    private final static Set<UUID> CACHED_SPLIT_SLIMES = new HashSet<>();
    private final static Set<UUID> CACHED_SPAWNER_MOBS = new HashSet<>();

    @EventHandler
    public void preventSpawnerCoin (CreatureSpawnEvent e)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(e.getEntity().getWorld().getName()))
            return;

        if (e.getSpawnReason().equals(SpawnReason.SPAWNER) || e.getEntityType().equals(EntityType.CAVE_SPIDER))
        {
            if (!Config.get(Config.BOOLEAN.SPAWNER_DROP))
            {
                CACHED_SPAWNER_MOBS.add(e.getEntity().getUniqueId());
            }
        }
    }

    @EventHandler
    public void splitPrevent (CreatureSpawnEvent e)
    {
        if (e.getSpawnReason().equals(SpawnReason.SLIME_SPLIT))
        {
            if (Config.get(Config.BOOLEAN.PREVENT_SPLITS))
            {
                CACHED_SPLIT_SLIMES.add(e.getEntity().getUniqueId());
            }
        }
    }

    public static boolean fromSplit (Entity m)
    {
        return CACHED_SPLIT_SLIMES.contains(m.getUniqueId());
    }

    public static boolean fromSpawner (Entity m)
    {
        return CACHED_SPAWNER_MOBS.contains(m.getUniqueId());
    }

    public static void removeFromList (Entity m)
    {
        CACHED_SPLIT_SLIMES.remove(m.getUniqueId());
        CACHED_SPAWNER_MOBS.remove(m.getUniqueId());
    }
}
