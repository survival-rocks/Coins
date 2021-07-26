package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import me.justeli.coins.cancel.PreventSpawner;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * Created by Eli on May 17, 2020.
 * Coins: me.justeli.coins.economy
 */
public class CoinsAPI
{
    public static double receivedWhileOffline (UUID uuid)
    {
        return Coins.getInstance().getCoinStorage().getStorage(uuid).getDouble("offlineBalance");
    }

    public static void playEffect (Location location, int amount)
    {
        Coins.getInstance().getCoinsEffect().coinsEffect(location, amount);
    }

    public static boolean mobFromSpawner (Entity entity)
    {
        return PreventSpawner.fromSpawner(entity);
    }

    public static void particles (Location location, double radius, int amount)
    {
        Coins.getInstance().getCoinParticles().dropCoins(location, radius, amount);
    }
}
