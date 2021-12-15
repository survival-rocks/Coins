package me.justeli.coins.spigot.handler;

import me.justeli.coins.spigot.Coins;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;

/** by Eli at July 30, 2021 **/
public record CoinDropHandler (Coins plugin)
        implements Listener
{
    private static final Random RANDOM = new Random();

    @EventHandler
    public void on ()
    {

    }
}
