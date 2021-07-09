package me.justeli.coins.events;

import me.justeli.coins.Coins;
import me.justeli.coins.item.Coin;
import me.justeli.coins.prevent.UnfairMobs;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DropCoin
        implements Listener
{
    private final Coins instance;
    private final ThreadLocalRandom localRandom;

    public DropCoin (Coins instance)
    {
        this.instance = instance;
        this.localRandom = ThreadLocalRandom.current();
    }

    private final HashMap<Location, Integer> locationTracker = new HashMap<>();

    @EventHandler
    public void fish (PlayerFishEvent e)
    {

    }

    @EventHandler
    public void advancement (PlayerAdvancementDoneEvent e) // PlayerRecipeDiscoverEvent
    {
        e.getAdvancement().getCriteria();
        // and check if it's either new recipe or an actual achievement
    }

    // Drop coins when mob is killed.
    @EventHandler
    public void onDeath (EntityDeathEvent e)
    {
        Entity m = e.getEntity();

        if (OldConfig.get(OldConfig.ARRAY.DISABLED_WORLDS).contains(m.getWorld().getName()))
            return;

        int setLimit = OldConfig.get(OldConfig.DOUBLE.LIMIT_FOR_LOCATION).intValue();
        if (setLimit >= 1)
        {
            final Location location = m.getLocation().getBlock().getLocation().clone();
            int killAmount = locationTracker.getOrDefault(location, 0);
            locationTracker.put(location, killAmount + 1);

            instance.delayed(144000, () -> locationTracker.put(location, locationTracker.getOrDefault(location, 0) - 1)); // subtract an hour later

            if (killAmount > setLimit)
                return;
        }

        if (!OldConfig.get(OldConfig.BOOLEAN.DROP_WITH_ANY_DEATH))
        {
            AttributeInstance maxHealth = ((Attributable) m).getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = OldConfig.get(OldConfig.DOUBLE.PERCENTAGE_PLAYER_HIT);

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(m.getUniqueId())/maxHealth.getValue() < hitSetting)
                return;
        }

        Player killer = getKiller(e.getEntity());
        if (killer != null)
        {
            if ((m instanceof Monster || m instanceof Slime || m instanceof Ghast || m instanceof EnderDragon || m instanceof Shulker || m instanceof Phantom)
                    || ((m instanceof Animals || m instanceof Squid || m instanceof Snowman || m instanceof IronGolem
                    || m instanceof Villager || m instanceof Ambient) && OldConfig.get(OldConfig.BOOLEAN.PASSIVE_DROP))
                    || (m instanceof Player && OldConfig.get(OldConfig.BOOLEAN.PLAYER_DROP) && instance.getEconomy().getBalance((Player) m) >= 0))
            {
                dropMobCoin(m, killer);
            }
        }
        else if (OldConfig.get(OldConfig.BOOLEAN.DROP_WITH_ANY_DEATH))
        {
            dropMobCoin(m, null);
        }

        if (m instanceof Player && OldConfig.get(OldConfig.BOOLEAN.LOSE_ON_DEATH))
        {
            Player p = (Player) e.getEntity();

            if (instance.getEconomy().getBalance(p) < OldConfig.get(OldConfig.DOUBLE.DONT_LOSE_BELOW))
                return;

            double second = OldConfig.get(OldConfig.DOUBLE.MONEY_TAKEN__FROM);
            double first = OldConfig.get(OldConfig.DOUBLE.MONEY_TAKEN__TO) - second;

            double random = localRandom.nextDouble() * first + second;
            double take = OldConfig.get(OldConfig.BOOLEAN.TAKE_PERCENTAGE)? (random / 100) * instance.getEconomy().getBalance(p) : random;

            if (take > 0 && instance.getEconomy().withdrawPlayer(p, (long) take).transactionSuccess())
            {
                p.sendTitle("", color(OldConfig.get(OldConfig.STRING.WITHDRAW_MESSAGE).replace("{display}", instance.getEconomy().format(take))
                        .replace("{$}", OldConfig.get(OldConfig.STRING.CURRENCY_SYMBOL))), 20, 100, 20);

                if (OldConfig.get(OldConfig.BOOLEAN.DROP_ON_DEATH) && p.getLocation().getWorld() != null)
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
        if (p != null && m instanceof Player && OldConfig.get(OldConfig.BOOLEAN.PREVENT_ALTS))
        {
            Player player = (Player) m;
            if (p.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (UnfairMobs.fromSlimeSplit(m))
            return;

        if (!instance.getCancelSpawners().fromSpawner(m)
                || (p == null && OldConfig.get(OldConfig.BOOLEAN.SPAWNER_DROP))
                || (p != null && p.hasPermission("coins.spawner")) )
        {
            if (localRandom.nextDouble() <= OldConfig.get(OldConfig.DOUBLE.DROP_CHANCE))
            {
                int amount = 1;
                if (Settings.getMultiplier().containsKey(m.getType()))
                    amount = Settings.getMultiplier().get(m.getType());

                dropCoin(amount, p, m.getLocation());
            }
        }
        else
        {
            //Spawners.removeFromList(m);
        }
    }

    @EventHandler (ignoreCancelled = true,
                   priority = EventPriority.MONITOR)
    public void onMine (BlockBreakEvent e)
    {
        if (!OldConfig.get(OldConfig.BOOLEAN.ONLY_EXPERIENCE_BLOCKS))
        {
            dropBlockCoin(e.getBlock(), e.getPlayer());
            return;
        }

        if (e.getExpToDrop() > 0)
            dropBlockCoin(e.getBlock(), e.getPlayer());
    }

    private void dropBlockCoin (Block block, Player p)
    {
        if (localRandom.nextDouble() <= OldConfig.get(OldConfig.DOUBLE.MINE_PERCENTAGE))
            instance.delayed(1, () -> dropCoin(1, p, block.getLocation().clone().add(0.5, 0.5, 0.5)));
    }

    private void dropCoin (int amount, Player p, Location location)
    {
        if (OldConfig.get(OldConfig.BOOLEAN.DROP_EACH_COIN))
        {
            int second = OldConfig.get(OldConfig.DOUBLE.MONEY_AMOUNT__FROM).intValue();
            int first = OldConfig.get(OldConfig.DOUBLE.MONEY_AMOUNT__TO).intValue() + 1 - second;

            amount *= (localRandom.nextDouble() * first + second);
        }

        if (p != null)
            amount *= instance.getMultiplier().getMultiplier(p);

        boolean stack = !OldConfig.get(OldConfig.BOOLEAN.DROP_EACH_COIN) && OldConfig.get(OldConfig.BOOLEAN.STACK_COINS);
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
