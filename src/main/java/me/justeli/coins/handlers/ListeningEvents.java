package me.justeli.coins.handlers;

import me.justeli.coins.Coins;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Created by Eli on November 15, 2020.
 * Coins: me.justeli.coins.events
 */
public class ListeningEvents
        implements Listener
{
    private final Coins instance;

    public ListeningEvents (Coins instance)
    {
        this.instance = instance;
    }

    @EventHandler
    public void on (EntityDeathEvent e)
    {

    }


    // Players that killed a mob using a projectile.
    @EventHandler
    public void projectiles (EntityDeathEvent event)
    {
        Entity dead = event.getEntity();
        EntityDamageEvent damageCause = dead.getLastDamageCause();

        if (!(damageCause instanceof EntityDamageByEntityEvent))
            return;

        EntityDamageByEntityEvent damageCausedByEntity = (EntityDamageByEntityEvent) damageCause;

        resolvePlayerShooter(damageCausedByEntity).ifPresent(shooter ->
        {
            String tool = resolveProjectile(damageCausedByEntity).map(d -> d.getType().name()).orElse("something");
            EntityType killedType = dead.getType();
        });
    }

    @Nullable
    public Projectile resolveProjectileOrNull(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        return  (damager instanceof Projectile) ? (Projectile) damager : null;
    }

    public Optional<Projectile> resolveProjectile(EntityDamageByEntityEvent event)
    {
        return Optional.ofNullable(resolveProjectileOrNull(event));
    }

    @Nullable
    public Player resolvePlayerShooterOrNull(EntityDamageByEntityEvent event)
    {
        Projectile projectile = resolveProjectileOrNull(event);
        if (projectile == null) { return null; }

        ProjectileSource shooter = projectile.getShooter();
        return (shooter instanceof Player)? (Player) shooter : null;
    }

    public Optional<Player> resolvePlayerShooter(EntityDamageByEntityEvent event)
    {
        return Optional.ofNullable(resolvePlayerShooterOrNull(event));
    }


    // Drop coins for naturally generated blocks.
    @EventHandler
    public void blockBreak (BlockBreakEvent e)
    {

    }

    @EventHandler
    public void on (PlayerFishEvent e)
    {

    }

    @EventHandler
    public void on (PlayerAdvancementDoneEvent e)
    {

    }

    @EventHandler
    public void on (EntityTameEvent e)
    {

    }

    @EventHandler
    public void on (EntityBreedEvent e)
    {

    }

    @EventHandler
    public void on (PrepareAnvilEvent e)
    {

    }

    @EventHandler
    public void on (EnchantItemEvent e)
    {

    }

    @EventHandler
    public void on (BrewEvent e)
    {

    }

    @EventHandler
    public  void on (PlayerHarvestBlockEvent e)
    {

    }
}
