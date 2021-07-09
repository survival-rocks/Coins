package me.justeli.coins.events;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Extras;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.UUID;

public class DropCoin
        implements Listener
{
    private final Coins instance;

    public DropCoin (Coins instance)
    {
        this.instance = instance;
    }

    private final HashMap<Location, Integer> locationTracker = new HashMap<>();

    // Drop coins when mob is killed.
    @EventHandler
    public void onDeath (EntityDeathEvent e)
    {
        Entity m = e.getEntity();

        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(m.getWorld().getName()))
            return;

        int setLimit = Config.get(Config.DOUBLE.LIMIT_FOR_LOCATION).intValue();
        if (setLimit >= 1)
        {
            final Location location = m.getLocation().getBlock().getLocation().clone();
            int killAmount = locationTracker.getOrDefault(location, 0);
            locationTracker.put(location, killAmount + 1);

            instance.delayed(144000, () -> locationTracker.put(location, locationTracker.getOrDefault(location, 0) - 1)); // subtract an hour later

            if (killAmount > setLimit)
                return;
        }

        if (!Config.get(Config.BOOLEAN.DROP_WITH_ANY_DEATH))
        {
            AttributeInstance maxHealth = ((Attributable) m).getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = Config.get(Config.DOUBLE.PERCENTAGE_PLAYER_HIT);

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(m.getUniqueId())/maxHealth.getValue() < hitSetting)
                return;
        }

        Player killer = getKiller(e.getEntity());
        if (killer != null)
        {
            if ((m instanceof Monster || m instanceof Slime || m instanceof Ghast || m instanceof EnderDragon || m instanceof Shulker || m instanceof Phantom)
                    || ((m instanceof Animals || m instanceof Squid || m instanceof Snowman || m instanceof IronGolem
                    || m instanceof Villager || m instanceof Ambient) && Config.get(Config.BOOLEAN.PASSIVE_DROP))
                    || (m instanceof Player && Config.get(Config.BOOLEAN.PLAYER_DROP) && instance.getEconomy().getBalance((Player) m) >= 0))
            {
                dropMobCoin(m, killer);
            }
        }
        else if (Config.get(Config.BOOLEAN.DROP_WITH_ANY_DEATH))
        {
            dropMobCoin(m, null);
        }

        if (m instanceof Player && Config.get(Config.BOOLEAN.LOSE_ON_DEATH))
        {
            Player p = (Player) e.getEntity();

            if (instance.getEconomy().getBalance(p) < Config.get(Config.DOUBLE.DONT_LOSE_BELOW))
                return;

            double second = Config.get(Config.DOUBLE.MONEY_TAKEN__FROM);
            double first = Config.get(Config.DOUBLE.MONEY_TAKEN__TO) - second;

            double random = Math.random() * first + second;
            double take = Config.get(Config.BOOLEAN.TAKE_PERCENTAGE)? (random / 100) * instance.getEconomy().getBalance(p) : random;

            if (take > 0 && instance.getEconomy().withdrawPlayer(p, (long) take).transactionSuccess())
            {
                p.sendTitle("", color(Config.get(Config.STRING.WITHDRAW_MESSAGE).replace("{display}", instance.getEconomy().format(take))
                        .replace("{$}", Config.get(Config.STRING.CURRENCY_SYMBOL))), 20, 100, 20);

                if (Config.get(Config.BOOLEAN.DROP_ON_DEATH) && p.getLocation().getWorld() != null)
                {
                    p.getWorld().dropItem(p.getLocation(), new Coin(take).create());
                }
            }
        }
    }

    //todo doesn't work
    private Player getKiller (LivingEntity entity)
    {
        if (entity == null)
            return null;

        if (entity.getKiller() instanceof Projectile)
        {
            ProjectileSource killer = ((Projectile) entity.getKiller()).getShooter();
            if (killer instanceof Player)
            {
                return (Player) killer;
            }
        }

        return entity.getKiller();
    }

    private String color (String color)
    {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    private void dropMobCoin (Entity m, Player p)
    {
        if (p != null && m instanceof Player && Config.get(Config.BOOLEAN.PREVENT_ALTS))
        {
            Player player = (Player) m;
            if (p.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (PreventSpawner.fromSplit(m))
        {
            PreventSpawner.removeFromList(m);
            return;
        }

        if (!PreventSpawner.fromSpawner(m)
                || (p == null && Config.get(Config.BOOLEAN.SPAWNER_DROP))
                || (p != null && p.hasPermission("coins.spawner")) )
        {
            if (Math.random() <= Config.get(Config.DOUBLE.DROP_CHANCE))
            {
                int amount = 1;
                if (Settings.getMultiplier().containsKey(m.getType()))
                    amount = Settings.getMultiplier().get(m.getType());

                dropCoin(amount, p, m.getLocation());
            }
        }
        else
        {
            PreventSpawner.removeFromList(m);
        }
    }

    @EventHandler (ignoreCancelled = true,
                   priority = EventPriority.MONITOR)
    public void onMine (BlockBreakEvent e)
    {
        if (!Config.get(Config.BOOLEAN.ONLY_EXPERIENCE_BLOCKS))
        {
            dropBlockCoin(e.getBlock(), e.getPlayer());
            return;
        }

        if (e.getExpToDrop() > 0)
            dropBlockCoin(e.getBlock(), e.getPlayer());
    }

    private void dropBlockCoin (Block block, Player p)
    {
        if (Math.random() <= Config.get(Config.DOUBLE.MINE_PERCENTAGE))
            instance.delayed(1, () -> dropCoin(1, p, block.getLocation().clone().add(0.5, 0.5, 0.5)));
    }

    private void dropCoin (int amount, Player p, Location location)
    {
        if (Config.get(Config.BOOLEAN.DROP_EACH_COIN))
        {
            int second = Config.get(Config.DOUBLE.MONEY_AMOUNT__FROM).intValue();
            int first = Config.get(Config.DOUBLE.MONEY_AMOUNT__TO).intValue() + 1 - second;

            amount *= (Math.random() * first + second);
        }

        if (p != null)
            amount *= Extras.getMultiplier(p);

        boolean stack = !Config.get(Config.BOOLEAN.DROP_EACH_COIN) && Config.get(Config.BOOLEAN.STACK_COINS);
        for (int i = 0; i < amount; i++)
        {
            ItemStack coin = new Coin().stack(stack).create();
            location.getWorld().dropItem(location, coin);
        }
    }

    private final HashMap<UUID, Double> damages = new HashMap<>();

    private Double getPlayerDamage (UUID uuid)
    {
        return damages.getOrDefault(uuid, 0D);
    }

    @EventHandler
    public void registerHits (EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player))
            return;

        double playerDamage = damages.getOrDefault(e.getEntity().getUniqueId(), 0D);
        damages.put(e.getEntity().getUniqueId(), playerDamage + e.getFinalDamage());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void unregisterHits (EntityDeathEvent e)
    {
        damages.remove(e.getEntity().getUniqueId());
    }
}
