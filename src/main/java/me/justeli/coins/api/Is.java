package me.justeli.coins.api;

import org.bukkit.entity.*;

/**
 * Created by Eli on August 05, 2020.
 * Coins: me.justeli.coins.api
 */
public class Is
{
    // Hostile mobs are mobs that can do damage to players.
    // -> Boss: Wither, Dragon
    // -> Flying: Phantom, Ghast
    // -> Golem: IronGolem, Shulker, Snowman
    public static boolean hostile (Entity entity)
    {
        return entity instanceof Monster || entity instanceof Boss || entity instanceof Flying || entity instanceof Slime ||
                (entity instanceof Golem && !(entity instanceof Snowman)) || entity instanceof Wolf;
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
