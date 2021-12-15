package me.justeli.coins.spigot.item;

import me.justeli.coins.spigot.Coins;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/** by Eli at July 30, 2021 **/
public class Coin
{
    private final Coins plugin;

    public Coin (Coins plugin)
    {
        this.plugin = plugin;
    }

    public static CoinBuilder of ()
    {
        return new CoinBuilder();
    }

    public static boolean is (ItemStack item)
    {
        return ;
    }

    public static boolean is (Item item)
    {
        return ;
    }
}
