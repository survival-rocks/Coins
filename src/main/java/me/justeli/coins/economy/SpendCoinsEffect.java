package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import me.justeli.coins.libraries.Format;
import me.justeli.coins.settings.CoinsConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
public class SpendCoinsEffect
        implements Listener
{
    private final Coins instance;
    private final ThreadLocalRandom localRandom;

    public SpendCoinsEffect (Coins instance)
    {
        this.instance = instance;
        this.localRandom = ThreadLocalRandom.current();
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void createAccount (PlayerJoinEvent e)
    {
        if (CoinsConfig.economyIntegration && !instance.getEconomy().hasAccount(e.getPlayer()))
            instance.getEconomy().createPlayerAccount(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void deleteOfflineBalance (PlayerJoinEvent e)
    {
        instance.getCoinStorage().setStorage(e.getPlayer().getUniqueId(), "offlineBalance", null);
    }

    // Cache to allow adding up the total amount picked up for 30 ticks.
    private final HashMap<UUID, Double> pickupAmountCache = new HashMap<>();

    @EventHandler
    public void spendEffect (BalanceChangeEvent e)
    {
        if (!e.getPlayer().isOnline() || e.getPlayer().getPlayer() == null)
            return;

        Player player = e.getPlayer().getPlayer();
        double amount = e.getTransactionAmount();

        if (amount == 0)
            return;

        final UUID uuid = player.getUniqueId();
        pickupAmountCache.put(uuid, amount + (pickupAmountCache.containsKey(uuid)? pickupAmountCache.get(uuid) : 0));
        final Double newAmount = pickupAmountCache.get(uuid);

        String format = instance.getEconomy().format(Math.abs(newAmount));
        String bar = Format.color((amount > 0? CoinsConfig.depositMessage : CoinsConfig.withdrawMessage).replace("${display.currency}", format));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));

        // todo change to millis tracking instead of tasks
        instance.delayed(30, () ->
        {
            if (pickupAmountCache.containsKey(uuid) && pickupAmountCache.get(uuid).equals(newAmount))
                pickupAmountCache.remove(uuid);
        });

        if (amount < 0 && CoinsConfig.displayCoinEffects)
            coinsEffect(player.getEyeLocation(), (int) -amount);
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

    public void coinsEffect (Location location, int amount)
    {
        int calculation = amount < 10? amount : (int) Math.log10(amount) * 10;

        World world = location.getWorld();
        if (world == null)
            return;

        for (int i = 0; i < calculation; i++)
        {
            Item item = world.dropItem(location.clone().subtract(0, 0.7, 0), getCoin());
            item.setPickupDelay(10000);
            item.setVelocity(location.getDirection().add(new Vector(
                    (localRandom.nextDouble() - 0.5) / 4,
                    0.5 + ((localRandom.nextDouble() - 0.5) / 4),
                    (localRandom.nextDouble() - 0.5) / 4)).multiply(0.3));

            instance.delayed(localRandom.nextInt(1, 8), item::remove);
        }
    }
}
