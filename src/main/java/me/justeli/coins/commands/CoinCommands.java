package me.justeli.coins.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.economy.CoinsAPI;
import me.justeli.coins.item.ParseCoin;
import me.justeli.coins.settings.CoinsConfig;
import me.justeli.coins.settings.Messages;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class CoinCommands
{
    private final Coins instance;
    private final ThreadLocalRandom localRandom;

    public CoinCommands (Coins instance)
    {
        this.instance = instance;
        this.localRandom = ThreadLocalRandom.current();
    }

    @CommandMethod("coins|coin reload")
    @CommandPermission("coins.admin")
    public void reload (CommandSender sender)
    {
        long start = System.currentTimeMillis();

        instance.getSettings().remove();
        instance.getMultiplier().resetMultiplier();

        boolean success = instance.getSettings().reloadConfig();
        sender.sendMessage(Messages.RELOAD_SUCCESS.toString().replace("{0}", Long.toString(System.currentTimeMillis() - start)));

        if (!success) sender.sendMessage(Messages.MINOR_ISSUES.toString());
        else sender.sendMessage(Messages.CHECK_SETTINGS.toString());
    }

    @CommandMethod("coins|coin settings")
    @CommandPermission("coins.admin")
    public void settings (CommandSender sender)
    {
        String settings = instance.getSettings().getSettings();
        sender.sendMessage(settings);
    }

    @CommandMethod("coins|coin drop player <username> <amount> [radius]")
    @CommandPermission("coins.drop")
    public void dropPlayer (CommandSender sender,
            @Argument("username") Player player,
            @Argument("amount") @Range(min = "1", max = "1000") int amount,
            @Argument("radius") @Range(min = "1", max = "80") double radius)
    {
        drop (sender, player.getLocation(), amount, radius);
    }

    @CommandMethod("coins|coin drop location <coords> <amount> [radius]")
    @CommandPermission("coins.drop")
    public void dropLocation (CommandSender sender,
            @Argument("coords") Location location,
            @Argument("amount") @Range(min = "1", max = "1000") int amount,
            @Argument("radius") @Range(min = "1", max = "80") double radius)
    {
        drop (sender, location, amount, radius);
    }

    private void drop (CommandSender sender, Location location, double amount, double r)
    {
        if (CoinsConfig.disabledInWorld(location.getWorld()))
        {
            sender.sendMessage(Messages.COINS_DISABLED.toString());
            return;
        }

        double radius = r < 1? amount / 20 : r;

        if (radius < 1 || radius > 80)
        {
            sender.sendMessage(Messages.INVALID_RADIUS.toString());
            return;
        }

        if (amount < 1 || amount > 1000)
        {
            sender.sendMessage(Messages.INVALID_AMOUNT.toString());
            return;
        }

        instance.sync (() ->
        {
            CoinsAPI.dropCoins(location, radius, (int) amount);
            sender.sendMessage(Messages.SPAWNED_COINS.toString()
                    .replace("{0}", Integer.toString((int) amount))
                    .replace("{1}", Double.toString(radius))
                    .replace("{2}", location.getX() + ", " + location.getZ()));
        });
    }

    @CommandMethod("coins|coin give <player> <worth> [amount]")
    @CommandPermission("coins.give")
    public void give (CommandSender sender,
            @Argument("player") Player player,
            @Argument("worth") @Range(min = "1", max = "10000") int worth,
            @Argument("amount") @Range(min = "1", max = "64") int amount)
    {

    }

    @CommandMethod("coins|coin remove [radius]")
    @CommandPermission("coins.remove")
    public void removeRadius (CommandSender sender,
            @Argument("radius") @Range(min = "1", max = "80") Double radius)
    {
        remove (sender, radius);
    }

    private void remove (CommandSender sender, Double radius)
    {
        instance.sync(() ->
        {
            World world = sender instanceof Player? ((Player) sender).getWorld() : instance.getServer().getWorlds().get(0);

            Collection<Item> items = new ArrayList<>();
            if (radius == null || !(sender instanceof Player))
            {
                items = world.getEntitiesByClass(Item.class);
            }
            else if (PaperLib.isPaper())
            {
                items = world.getNearbyEntitiesByType(Item.class, ((Player) sender).getLocation(), radius);
            }
            else
            {
                Collection<Entity> entities = world.getNearbyEntities(((Player) sender).getLocation(), radius, radius, radius);
                for (Entity entity : entities)
                {
                    if (entity instanceof Item)
                        items.add((Item) entity);
                }
            }

            Collection<Item> coins = new ArrayList<>();
            for (Item item : items)
            {
                ParseCoin coin = new ParseCoin(item.getItemStack());
                if (!coin.is() || coin.withdrawn())
                    continue;

                coins.add(item);
            }

            for (Item coin : coins)
            {
                double random = localRandom.nextDouble() * 3;
                coin.setVelocity(new Vector(0, random, 0));
                instance.delayed((int) random * 5, coin::remove);
            }

            sender.sendMessage(Messages.REMOVED_COINS.toString().replace("{0}", Integer.toString(coins.size())));
        });
    }

    @CommandMethod("coins|coin language")
    @CommandPermission("coins.language")
    public void language (CommandSender sender)
    {
        for (Messages m : Messages.values())
            sender.sendMessage(m.toString());
    }

    @CommandMethod("coins|coin version")
    @CommandPermission("coins.version")
    public void version (CommandSender sender)
    {
        String version = instance.getPluginVersion();
        String current = instance.getDescription().getVersion();
        sender.sendMessage(Messages.VERSION_CURRENTLY.format(current));
        sender.sendMessage(Messages.LATEST_VERSION.format(version));
        if (version.equals(current))
        {
            sender.sendMessage(Messages.UP_TO_DATE.format(current));
        }
        else
        {
            sender.sendMessage(Messages.CONSIDER_UPDATING.format(version));
            sender.sendMessage("https://www.spigotmc.org/resources/coins.33382/");
        }
    }

    @CommandMethod("coins|coin")
    public void help (CommandSender sender)
    {
        sendHelp(sender);
    }

    private void sendHelp (CommandSender sender)
    {
        String version = instance.getDescription().getVersion();
        String update = instance.getPluginVersion();
        StringBuilder notice = new StringBuilder(Messages.COINS_HELP.toString()).append(" ").append(version);

        if (!update.equals(version) && sender.hasPermission("coins.version"))
            notice.append(" (outdated → /coins update)");

        sender.sendMessage(notice.toString());

        if (sender.hasPermission("coins.drop"))
            sender.sendMessage(Messages.DROP_USAGE.toString());

        if (sender.hasPermission("coins.remove"))
            sender.sendMessage(Messages.REMOVE_USAGE.toString());

        if (sender.hasPermission("coins.give"))
        {
            //sender.sendMessage(Messages.GIVE_USAGE.toString());
        }

        if (sender.hasPermission("coins.admin"))
        {
            sender.sendMessage(Messages.SETTINGS_USAGE.toString());
            sender.sendMessage(Messages.RELOAD_USAGE.toString());
            sender.sendMessage(Messages.VERSION_CHECK.toString());
        }

        if (CoinsConfig.withdrawEnabled && sender.hasPermission("coins.withdraw"))
            sender.sendMessage(Messages.WITHDRAW_USAGE.toString());
    }
}
