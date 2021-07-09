package me.justeli.coins.cancel;

import me.justeli.coins.Coins;
import me.justeli.coins.item.CheckCoin;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
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
public class CancelInventories
        implements Listener
{
    private final Coins instance;

    public CancelInventories (Coins instance)
    {
        this.instance = instance;
    }

    @EventHandler
    public void avoidCraftingTable (CraftItemEvent e)
    {
        for (ItemStack stack : e.getInventory().getContents())
        {
            if (new CheckCoin(stack).is())
            {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void on (PrepareItemCraftEvent e)
    {
        for (ItemStack stack : e.getInventory().getContents())
        {
            if (new CheckCoin(stack).is())
            {
                e.getInventory().setResult(null);
                break;
            }
        }
    }

    @EventHandler
    public void on (PrepareAnvilEvent e)
    {
        if (e.getResult() != null && new CheckCoin(e.getResult()).is())
            e.setResult(null);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent e)
    {
        if (!e.getInventory().getType().equals(InventoryType.HOPPER))
            return;

        CheckCoin coin = new CheckCoin(e.getItem().getItemStack());
        if (!coin.is() || coin.withdrawed())
            return;

        if (Config.get(Config.BOOLEAN.DISABLE_HOPPERS))
        {
            e.setCancelled(true);
        }
        else if (coin.isUnique())
        {
            e.getItem().setItemStack(new Coin(coin.worth()).create());
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void coinInventory (InventoryClickEvent e)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(e.getWhoClicked().getWorld().getName()))
            return;

        if (!(e.getWhoClicked() instanceof Player))
            return;

        ItemStack item = e.getCurrentItem();
        if (item == null)
            return;

        CheckCoin coin = new CheckCoin(item);
        if (!coin.is() || coin.withdrawed())
            return;

        Player p = (Player) e.getWhoClicked();

        e.setCancelled(true);
        instance.getCoinsPickup().giveReward(item.getAmount(), coin, p);
        e.getCurrentItem().setAmount(0);
    }
}
