package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Format;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Eli on 26 aug 2019.
 * survivalRocks: me.justeli.payment
 */
public class CoinsEffect implements Listener
{
    private final Coins instance;

    public CoinsEffect (Coins instance)
    {
        this.instance = instance;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void createAccount (PlayerJoinEvent e)
    {
        if (Config.get(Config.BOOLEAN.COINS_ECONOMY) && !instance.getEconomy().hasAccount(e.getPlayer()))
            instance.getEconomy().createPlayerAccount(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void deleteOfflineBalance (PlayerJoinEvent e)
    {
        instance.getCoinStorage().setStorage(e.getPlayer().getUniqueId(), "offlineBalance", null);
    }

    private final HashMap<UUID, Double> pickup = new HashMap<>();

    @EventHandler
    public void coins (BalanceChangeEvent e)
    {
        if (!e.getPlayer().isOnline() || e.getPlayer().getPlayer() == null)
            return;

        Player p = e.getPlayer().getPlayer();
        double amount = e.getTransactionAmount();

        if (amount == 0)
            return;

        instance.getCoinStorage().setServerData("inCirculation",
                instance.getCoinStorage().getCachedServerData().getDouble("inCirculation") + amount);

        final UUID uuid = p.getUniqueId();
        pickup.put(uuid, amount + (pickup.containsKey(uuid)? pickup.get(uuid) : 0));
        final Double newAmount = pickup.get(uuid);

        String format = instance.getEconomy().format(Math.abs(newAmount));
        String bar = Format.color(Config.get(amount > 0? Config.STRING.DEPOSIT_MESSAGE : Config.STRING.WITHDRAW_MESSAGE).replace("{display}", format));
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));

        instance.delayed(Config.get(Config.BOOLEAN.DROP_EACH_COIN)? 30 : 10, () ->
        {
            if (pickup.containsKey(uuid) && pickup.get(uuid).equals(newAmount))
                pickup.remove(uuid);
        });

        if (amount < 0 && Config.get(Config.BOOLEAN.COINS_EFFECT))
            coinsEffect(p.getEyeLocation(), (int) -amount);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent e)
    {
        if (e.getInventory().getType().equals(InventoryType.HOPPER))
        {
            ItemStack item = e.getItem().getItemStack();
            if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
            {
                String pickupName = item.getItemMeta().getDisplayName();
                if (pickupName.startsWith("Glitch Coin")) e.setCancelled(true);
            }
        }
    }

    private final ItemStack coin = new Coin(0).create();

    private ItemStack getCoin ()
    {
        ItemMeta meta = coin.getItemMeta();
        if (meta != null) meta.setDisplayName("Glitch Coin " + ThreadLocalRandom.current().nextDouble());
        coin.setItemMeta(meta);

        return coin;
    }

    void coinsEffect (Location location, int amount)
    {
        int calculation = amount < 10? amount : (int) Math.log10(amount) * 10;

        World world = location.getWorld();
        if (world == null)
            return;

        for (int i = 0; i < calculation; i++)
        {
            Item item = world.dropItem(location.clone().subtract(0, 0.7, 0), getCoin());
            item.setPickupDelay(10000);
            item.setVelocity(location.getDirection()
                    .add(new Vector((Math.random() - 0.5) / 4, 0.5 + ((Math.random() - 0.5) / 4), (Math.random() - 0.5) / 4)).multiply(0.3));

            instance.delayed(ThreadLocalRandom.current().nextInt(1, 8), item::remove);
        }
    }
}
