package me.justeli.coins.prevent;

import me.justeli.coins.Coins;
import me.justeli.coins.item.ParseCoin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Eli on 2 mei 2019.
 * spigotPlugins: me.justeli.coins.cancel
 */
public class Inventories
        implements Listener
{
    private final Coins instance;

    public Inventories (Coins instance)
    {
        this.instance = instance;
    }

    private boolean containsCoin (ItemStack[] items)
    {
        for (ItemStack stack : items)
            if (new ParseCoin(stack).is())
                return true;

        return false;
    }

    @EventHandler
    public void crafting (CraftItemEvent e)
    {
        if (containsCoin(e.getInventory().getContents()))
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void prepareCraft (PrepareItemCraftEvent e)
    {
        if (containsCoin(e.getInventory().getContents()))
        {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void prepareAnvil (PrepareAnvilEvent e)
    {
        if (e.getResult() == null)
            return;

        if (new ParseCoin(e.getResult()).is())
        {
            e.setResult(null);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void hoppers (InventoryPickupItemEvent e)
    {
        if (!e.getInventory().getType().equals(InventoryType.HOPPER))
            return;

        ParseCoin coin = new ParseCoin(e.getItem().getItemStack());
        if (!coin.is() || coin.withdrawn())
            return;

        if (OldConfig.get(OldConfig.BOOLEAN.DISABLE_HOPPERS))
        {
            e.setCancelled(true);
        }
        else if (coin.isUnique())
        {
            e.getItem().setItemStack(new Coin(coin.worth()).create());
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void inventoryClick (InventoryClickEvent e)
    {
        if (OldConfig.get(OldConfig.ARRAY.DISABLED_WORLDS).contains(e.getWhoClicked().getWorld().getName()))
            return;

        if (!(e.getWhoClicked() instanceof Player))
            return;

        ItemStack item = e.getCurrentItem();
        if (item == null)
            return;

        ParseCoin coin = new ParseCoin(item);
        if (!coin.is() || coin.withdrawn())
            return;

        Player player = (Player) e.getWhoClicked();

        e.setCancelled(true);
        instance.getCoinsPickup().giveReward(item.getAmount(), coin, player);
        e.getCurrentItem().setAmount(0);
    }
}
