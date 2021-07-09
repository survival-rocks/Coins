package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Eli on May 17, 2020.
 * Coins: me.justeli.coins.economy
 */
public class CoinsAPI
{
    private static final Coins instance;

    static
    {
        instance = Coins.get();
    }

    public static double receivedWhileOffline (UUID uniqueId)
    {
        return instance.getCoinStorage().getStorage(uniqueId).getDouble("offlineBalance");
    }

    public static void playEffect (Location location, int amount)
    {
        instance.getCoinsEffect().coinsEffect(location, amount);
    }

    public static void dropCoins (Location location, double radius, int amount)
    {
        instance.getCoinParticles().dropCoins(location, radius, amount);
    }
}
