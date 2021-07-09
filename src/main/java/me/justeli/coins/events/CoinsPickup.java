package me.justeli.coins.events;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Format;
import me.justeli.coins.item.CheckCoin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class CoinsPickup
        implements Listener
{
    private final Coins instance;

    public CoinsPickup (Coins instance)
    {
        this.instance = instance;
    }

    private final Set<Integer> thrown = new HashSet<>();

    @EventHandler
    public void onPickup (PickupEvent e)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(e.getPlayer().getWorld().getName()))
            return;

        CheckCoin coin = new CheckCoin(e.getItem().getItemStack());

        if (!coin.isFor(e.getPlayer()))
        {
            e.setCancelled(true);
            return;
        }

        if (coin.is())
        {
            e.setCancelled(true);

            // if coin is already thrown up then it shouldn't be picked up again
            if (thrown.contains(e.getItem().getEntityId()))
                return;

            Player p = e.getPlayer();
            if (!p.hasPermission("coins.disable") || p.isOp() || p.hasPermission("*"))
                giveCoin(e.getItem(), coin, e.getPlayer());
        }
    }

    private void giveCoin (Item item, CheckCoin coin, Player p)
    {
        thrown.add(item.getEntityId());
        item.setVelocity(new Vector(0, 0.4, 0));

        giveReward(item.getItemStack().getAmount(), coin, p);

        if (Config.get(Config.BOOLEAN.PICKUP_SOUND))
        {
            float volume = Config.get(Config.DOUBLE.SOUND_VOLUME).floatValue();
            float pitch = Config.get(Config.DOUBLE.SOUND_PITCH).floatValue();

            p.playSound(p.getEyeLocation(), Settings.getSound(), volume == 0? 0.3f : volume, pitch == 0? 0.3f : pitch);
        }

        instance.delayed(5, () ->
        {
            item.remove();
            thrown.remove(item.getEntityId());
        });
    }

    public void giveReward (int amount, CheckCoin coin, Player p)
    {
        addMoney(p, amount * coin.worth());
    }

    public void addMoney (Player p, Double amount)
    {
        instance.getEconomy().depositPlayer(p, Format.number(amount));
    }
}
