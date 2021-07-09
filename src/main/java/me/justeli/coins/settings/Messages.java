package me.justeli.coins.settings;

import me.justeli.coins.api.Format;

import java.util.HashMap;

/**
 * Created by Eli on 4/24/2017.
 * Spigot Plugins: me.justeli.coins.settings
 */

public enum Messages
{
    LOADED_SETTINGS ("&3&oCurrently loaded settings of the Coins configuration."),
    NO_PERMISSION ("&4You do not have access to that command."),
    RELOAD_SUCCESS ("&eConfig of &6Coins &ehas been reloaded in &a{0}ms&e."),
    MINOR_ISSUES ("&c&oThere were some minor errors while reloading, check console."),
    CHECK_SETTINGS ("&e&oYou can check the loaded settings with &f&o/coins settings&e&o."),
    INVALID_AMOUNT ("&cThat is an invalid amount."),
    WITHDRAW_COINS ("&eYou withdrawn &f{$}{0} &eand received &f{0} coins&e for it."),
    NOT_THAT_MUCH ("&cYou are not allowed to withdraw that much."),
    WITHDRAW_USAGE ("&c/withdraw <coins> [amount] &7- withdraw some money into coins"),
    INVALID_NUMBER ("&cThat is an invalid number."),
    PLAYER_NOT_FOUND ("&4That player could not be found."),
    COORDS_NOT_FOUND ("&4Those coords or the world couldn't be found."),
    COINS_DISABLED ("&cCoins are disabled in this world."),
    INVALID_RADIUS ("&cThat is an invalid radius."),
    SPAWNED_COINS ("&9Spawned {0} coins in radius {1} around {2}."),
    DROP_USAGE ("&c/coins drop <player|x,y,z[,world]> <amount> [radius]"),
    COINS_HELP ("&4:: Help for Coins"),
    REMOVE_USAGE ("&c/coins remove [radius|all] &7- remove coins in a radius"),
    SETTINGS_USAGE ("&c/coins settings &7- list the currently loaded settings"),
    RELOAD_USAGE ("&c/coins reload &7- reload the settings from config.yml"),
    REMOVED_COINS ("&9Removed a total of {0} coins."),
    INVENTORY_FULL ("&cYou cannot withdraw when your inventory is full!"),
    VERSION_CHECK ("&c/coins version &7- check if there's a new release"),

    VERSION_CURRENTLY ("&eVersion currently installed:&f %s"),
    LATEST_VERSION ("&eLatest released version:&f %s"),
    UP_TO_DATE ("&aYou're up to date with version %s."),
    CONSIDER_UPDATING ("&cConsider updating the plugin Coins to version %s."),
    USING_BUKKIT ("You seem to be using Bukkit for your server, but the plugin " +
            "Coins requires Spigot! This prevents the plugin from doing all kind of things. The plugin will be disabled. Please use Spigot. " +
            "Moving from Bukkit to Spigot will NOT cause any problems with other plugins, since Spigot only adds more features to Bukkit."),
    USE_PAPER ("It is recommended to use Paper server software for the plugin Coins, as it will come with better effects!"),
    INSTALL_VAULT ("Your server needs to have Vault installed before Coins can " +
            "work! Coins will be disabled now... Download Vault here: https://www.spigotmc.org/resources/vault.34315/"),


    ;

    private final String value;

    Messages (String value)
    {
        this.value = value;
    }

    @Override
    public String toString ()
    {
        HashMap<Messages, String> language = Settings.getLanguage();
        String notFormat = language.getOrDefault(this, value);

        return Format.color(notFormat.replace("{$}", Config.get(Config.STRING.CURRENCY_SYMBOL)));
    }

    public String format (String... input)
    {
        return String.format(toString(), input);
    }
}
