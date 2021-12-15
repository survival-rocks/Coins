package me.justeli.coins.spigot.config;

import me.justeli.coins.spigot.config.api.ConfigEntry;
import me.justeli.coins.spigot.config.type.CommandType;
import me.justeli.coins.spigot.config.type.CurrencyType;
import me.justeli.coins.spigot.config.type.DropType;
import me.justeli.coins.spigot.config.type.JobType;
import me.justeli.coins.spigot.config.type.Range;
import me.justeli.coins.spigot.item.CoinType;

import java.awt.Color;

/** by Eli at July 30, 2021 **/
public class Config
{
    @ConfigEntry("language") public static String LANGUAGE;

    @ConfigEntry("storage.method") public static String STORAGE_METHOD;
    @ConfigEntry("storage.connection.address") public static String STORAGE_ADDRESS;
    @ConfigEntry("storage.connection.port") public static String STORAGE_PORT;
    @ConfigEntry("storage.connection.database") public static String STORAGE_DATABASE;
    @ConfigEntry("storage.connection.username") public static String STORAGE_USERNAME;
    @ConfigEntry("storage.connection.password") public static String STORAGE_PASSWORD;

    @ConfigEntry("colors.currency") public static Color COLOR_CURRENCY;
    @ConfigEntry("colors.negative") public static Color COLOR_NEGATIVE;
    @ConfigEntry("colors.primary") public static Color COLOR_PRIMARY;
    @ConfigEntry("colors.values") public static Color COLOR_VALUES;

    @ConfigEntry("display.currency") public static String DISPLAY_CURRENCY;
    @ConfigEntry("display.deposit") public static String DISPLAY_DEPOSIT;
    @ConfigEntry("display.withdraw") public static String DISPLAY_WITHDRAW;
    @ConfigEntry("display.coinItem") public static String DISPLAY_COIN_ITEM;

    @ConfigEntry("lootChests.chancePerSlot") public static Integer LOOT_CHEST_CHANCE;
    @ConfigEntry("lootChests.value") public static Range LOOT_CHEST_RANGE;

    @ConfigEntry("currencies") public static CurrencyType[] CURRENCIES;

    @ConfigEntry("commands") public static CommandType[] COMMANDS;

    @ConfigEntry("coins") public static CoinType[] COINS;

    @ConfigEntry("drops") public static DropType[] DROPS;

    @ConfigEntry("jobs") public static JobType[] JOBS;

    @ConfigEntry("settings.coinDecimals") public static Double COIN_DECIMALS;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
    @ConfigEntry("") public static ;
}
