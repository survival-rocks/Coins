package me.justeli.coins.events;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Format;
import me.justeli.coins.api.Is;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Location;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class DropCoin
        implements Listener
{
    private final Coins instance;
    private static final Random RANDOM = new Random();

    public DropCoin (Coins instance)
    {
        this.instance = instance;
    }

    private final HashMap<Location, Integer> locationTracker = new HashMap<>();
    private final HashMap<UUID, Double> damages = new HashMap<>();

    // Drop coins when mob is killed.
    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityDeath (EntityDeathEvent event)
    {
        LivingEntity dead = event.getEntity();
        EntityDamageEvent damageCause = dead.getLastDamageCause();

        if (dead.getKiller() != null)
        {
            entityDeath(event.getEntity(), event.getEntity().getKiller());
        }
        else if (damageCause instanceof EntityDamageByEntityEvent)
        {
            entityDeath(dead, resolvePlayerShooterOrNull((EntityDamageByEntityEvent) damageCause));
        }
        else
        {
            entityDeath(dead, null);
        }

        PreventSpawner.removeFromList(dead);
        damages.remove(dead.getUniqueId());
    }

    public void entityDeath (LivingEntity entity, Player killer)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(entity.getWorld().getName()))
            return;

        int setLimit = Config.get(Config.DOUBLE.LIMIT_FOR_LOCATION).intValue();
        if (setLimit >= 1)
        {
            final Location location = entity.getLocation().getBlock().getLocation().clone();
            int killAmount = locationTracker.getOrDefault(location, 0);
            locationTracker.put(location, killAmount + 1);

            instance.delayed(144000, () -> locationTracker.put(location, locationTracker.getOrDefault(location, 0) - 1)); // subtract an hour later

            if (killAmount > setLimit)
                return;
        }

        if (!Config.get(Config.BOOLEAN.DROP_WITH_ANY_DEATH))
        {
            AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = Config.get(Config.DOUBLE.PERCENTAGE_PLAYER_HIT);

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(entity.getUniqueId())/maxHealth.getValue() < hitSetting)
                return;
        }

        if (killer != null)
        {
            if (
                    (Is.hostile(entity)) ||
                    (Is.passive(entity) && Config.get(Config.BOOLEAN.PASSIVE_DROP)) ||
                    (Is.player(entity) && Config.get(Config.BOOLEAN.PLAYER_DROP) && Coins.getInstance().getEconomy().getBalance((Player) entity) >= 0)
            )
            {
                dropMobCoin(entity, killer);
            }
        }
        else if (Config.get(Config.BOOLEAN.DROP_WITH_ANY_DEATH) && Is.mob(entity))
        {
            dropMobCoin(entity, null);
        }

        if (Is.player(entity) && Config.get(Config.BOOLEAN.LOSE_ON_DEATH))
        {
            Player p = (Player) entity;

            if (instance.getEconomy().getBalance(p) < Config.get(Config.DOUBLE.DONT_LOSE_BELOW))
                return;

            double second = Config.get(Config.DOUBLE.MONEY_TAKEN__FROM);
            double first = Config.get(Config.DOUBLE.MONEY_TAKEN__TO) - second;

            double random = RANDOM.nextDouble() * first + second;
            double take = Config.get(Config.BOOLEAN.TAKE_PERCENTAGE)? (random / 100) * instance.getEconomy().getBalance(p) : random;

            if (take > 0 && instance.getEconomy().withdrawPlayer(p, (long) take).transactionSuccess())
            {
                p.sendTitle("", Format.color(Config.get(Config.STRING.WITHDRAW_MESSAGE).replace("{display}", instance.getEconomy().format(take))
                        .replace("{$}", Config.get(Config.STRING.CURRENCY_SYMBOL))), 20, 100, 20);

                if (Config.get(Config.BOOLEAN.DROP_ON_DEATH) && p.getLocation().getWorld() != null)
                {
                    p.getWorld().dropItem(p.getLocation(), new Coin(take).create());
                }
            }
        }
    }

    // Bow & Trident Section

    @Nullable
    public Projectile resolveProjectileOrNull(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        return  (damager instanceof Projectile) ? (Projectile) damager : null;
    }


    @Nullable
    public Player resolvePlayerShooterOrNull(EntityDamageByEntityEvent event)
    {
        Projectile projectile = resolveProjectileOrNull(event);
        if (projectile == null) { return null; }

        ProjectileSource shooter = projectile.getShooter();
        return (shooter instanceof Player) ? (Player) shooter : null;
    }

    // End of Bow & Trident Section

    private void dropMobCoin (Entity victim, Player killer)
    {
        if (killer != null && victim instanceof Player && Config.get(Config.BOOLEAN.PREVENT_ALTS))
        {
            Player player = (Player) victim;
            if (killer.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (PreventSpawner.fromSplit(victim))
            return;

        if (!PreventSpawner.fromSpawner(victim)
                || (killer == null && Config.get(Config.BOOLEAN.SPAWNER_DROP))
                || (killer != null && killer.hasPermission("coins.spawner")) )
        {
            if (RANDOM.nextDouble() <= Config.get(Config.DOUBLE.DROP_CHANCE))
            {
                int amount = 1;
                if (Settings.getMultiplier().containsKey(victim.getType()))
                    amount = Settings.getMultiplier().get(victim.getType());

                dropCoin(amount, killer, victim.getLocation());
            }
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
        if (RANDOM.nextDouble() <= Config.get(Config.DOUBLE.MINE_PERCENTAGE))
            instance.delayed(1, () -> dropCoin(1, p, block.getLocation().clone().add(0.5, 0.5, 0.5)));
    }

    private void dropCoin (int amount, Player p, Location location)
    {
        if (Config.get(Config.BOOLEAN.DROP_EACH_COIN))
        {
            int second = Config.get(Config.DOUBLE.MONEY_AMOUNT__FROM).intValue();
            int first = Config.get(Config.DOUBLE.MONEY_AMOUNT__TO).intValue() + 1 - second;

            amount *= (RANDOM.nextDouble() * first + second);
        }

        boolean stack = !Config.get(Config.BOOLEAN.DROP_EACH_COIN) && Config.get(Config.BOOLEAN.STACK_COINS);
        for (int i = 0; i < amount; i++)
        {
            ItemStack coin = new Coin().stack(stack).create();
            location.getWorld().dropItem(location, coin);
        }
    }

    private double getPlayerDamage (UUID uuid)
    {
        return damages.computeIfAbsent(uuid, empty -> 0D);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void registerHits (EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player) && resolvePlayerShooterOrNull(e) == null)
            return;

        UUID uuid = e.getEntity().getUniqueId();
        double playerDamage = damages.computeIfAbsent(uuid, empty -> 0D);
        damages.put(uuid, playerDamage + e.getFinalDamage());
    }
}
