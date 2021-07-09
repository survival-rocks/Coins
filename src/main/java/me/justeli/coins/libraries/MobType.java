package me.justeli.coins.libraries;

import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Wolf;

/**
 * Created by Eli on August 05, 2020.
 * Coins: me.justeli.coins.libraries
 */
public class MobType
{
    // Hostile mobs are mobs that can do damage to players.
    // -> Boss: Wither, Dragon
    // -> Flying: Phantom, Ghast
    // -> Golem: IronGolem, Shulker, Snowman
    public static boolean hostile (Entity entity)
    {
        return entity instanceof Monster || boss(entity) || entity instanceof Flying || entity instanceof Slime ||
                (entity instanceof Golem && !(entity instanceof Snowman)) || entity instanceof Wolf;
    }

    public static boolean passive (Entity entity)
    {
        return !hostile(entity) && mob(entity);
    }

    // Mobs are living entities with simple AI, unlike entities like Armor Stands and Item Frames.
    public static boolean mob (Entity entity)
    {
        return entity instanceof Mob;
    }

    // Bosses are Ender Dragons and Withers, currently.
    public static boolean boss (Entity entity)
    {
        return entity instanceof Boss;
    }
}
