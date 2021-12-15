package me.justeli.coins.spigot.handler;

import me.justeli.coins.spigot.Coins;
import me.justeli.coins.spigot.item.Coin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/** by Eli at July 30, 2021 **/
public record InventoryHandler (Coins plugin)
        implements Listener
{
    private boolean containsNoCoins (ItemStack[] items)
    {
        return Arrays.stream(items).noneMatch(Coin::is);
    }

    @EventHandler
    public void on (CraftItemEvent event)
    {
        if (containsNoCoins(event.getInventory().getContents()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void on (PrepareItemCraftEvent event)
    {
        if (containsNoCoins(event.getInventory().getContents()))
            return;

        event.getInventory().setResult(null);
    }

    @EventHandler
    public void on (PrepareAnvilEvent event)
    {
        if (event.getResult() == null || !Coin.is(event.getResult()))
            return;

        event.setResult(null);
    }

    @EventHandler
    public void on (InventoryPickupItemEvent event)
    {
        if (event.getInventory().getType() != InventoryType.HOPPER)
            return;
    }
}
