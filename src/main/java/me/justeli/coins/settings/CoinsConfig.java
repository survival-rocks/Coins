package me.justeli.coins.settings;

import me.justeli.coins.handlers.EventType;
import me.justeli.coins.handlers.WithdrawType;
import me.justeli.coins.item.Coin;
import me.justeli.coins.item.Drop;
import me.justeli.coins.item.Job;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Eli on November 04, 2020.
 * Coins: me.justeli.coins.settings
 */
public class CoinsConfig
{
    @LoadConfig(path = "language", usual = "English") public static String language;
    @LoadConfig(path = "naming.symbol", usual = "$") public static String currencySymbol;
    @LoadConfig(path = "naming.single", usual = "Coin") public static String singleCoinName;
    @LoadConfig(path = "naming.plural", usual = "Coins") public static String pluralCoinName;
    @LoadConfig(path = "naming.color", usual = "ffaa00") public static ChatColor coinColor;
    @LoadConfig(path = "display.currency", usual = "${naming.currency}${_amount}") public static String currencyDisplay;
    @LoadConfig(path = "display.deposit", usual = "&2+ &a${display.currency}") public static String depositMessage;
    @LoadConfig(path = "display.withdraw", usual = "&4- &c${display.currency}") public static String withdrawMessage;
    @LoadConfig(path = "jobs.enabled", usual = "false") public static boolean jobsEnabled;
    @LoadConfig(path = "settings.coinDecimals", usual = "2") public static int coinDecimals;
    @LoadConfig(path = "settings.percentagePlayerHit", usual = "0.9") public static double percentagePlayerHit;
    @LoadConfig(path = "settings.disableHoppers", usual = "false") public static boolean disableHoppers;
    @LoadConfig(path = "settings.soundVolume", usual = "0.5") public static float soundVolume;
    @LoadConfig(path = "settings.limitForLocation", usual = "1") public static int limitForLocation;
    @LoadConfig(path = "withdraw.enabled", usual = "true") public static boolean withdrawEnabled;
    @LoadConfig(path = "withdraw.signed", usual = "false") public static boolean withdrawSigned;
    @LoadConfig(path = "withdraw.rename", usual = "false") public static boolean withdrawRename;
    @LoadConfig(path = "death.enabled", usual = "true") public static boolean deathDropEnabled;
    @LoadConfig(path = "death.takePercentage", usual = "false") public static boolean deathTakePercentage;
    @LoadConfig(path = "death.preventNegativeBalance", usual = "true") public static boolean deathPreventNegativeBalance;
    @LoadConfig(path = "death.dropAtDeathLocation", usual = "false") public static boolean dropAtDeathLocation;
    @LoadConfig(path = "economy.integration", usual = "false") public static boolean economyIntegration;
    @LoadConfig(path = "economy.effects", usual = "true") public static boolean displayCoinEffects;
    @LoadConfig(path = "economy.startingBalance", usual = "100.00") public static double startingBalance;
    @LoadConfig(path = "economy.track", usual = "false") public static boolean trackEconomyStats;
    @LoadConfig(path = "economy.maximumBalance", usual = "1000000000000.00") public static double maximumBalance;

    private static final HashMap<String, Coin> coins = new HashMap<>();

    private static final HashMap<String, Drop> drops = new HashMap<>();
    private static final HashMap<EventType, Set<String>> eventDrops = new HashMap<>();

    private static final HashMap<String, Job> jobs = new HashMap<>();
    private static final Set<String> defaultJobs = new HashSet<>();

    private static final HashMap<Set<Double>, String> withdrawRange = new HashMap<>();

    private static final Set<UUID> disabledWorlds = new HashSet<>();
    private static final Set<WithdrawType> withdrawTypes = new HashSet<>();
    private static final Set<Double> deathTakeRange = new HashSet<>();

    // GET

    public static HashMap<String, Coin> getCoins ()
    {
        return coins;
    }

    public static Coin getCoin (String name)
    {
        return coins.computeIfAbsent(name.toLowerCase(), empty -> null);
    }

    public static Drop getDrop (String name)
    {
        return drops.computeIfAbsent(name.toLowerCase(), empty -> null);
    }

    public static Job getJob (String name)
    {
        return jobs.computeIfAbsent(name.toLowerCase(), empty -> null);
    }

    public static boolean disabledInWorld (World world)
    {
        return disabledWorlds.contains(world.getUID());
    }

    public static Coin getCoinForWithdraw (double worth)
    {
        return withdrawRange....;
    }

    public static Set<WithdrawType> getEnabledWithdrawTypes ()
    {
        return withdrawTypes;
    }

    public static Set<Double> getDeathTakeRange ()
    {
        return deathTakeRange;
    }

    // SET & ADD

    static void addCoin (String name, Coin coin)
    {
        CoinsConfig.coins.put(name.toLowerCase(), coin);
    }

    static void addDrop (String name, Drop drop)
    {
        CoinsConfig.drops.put(name.toLowerCase(), drop);
    }
}
