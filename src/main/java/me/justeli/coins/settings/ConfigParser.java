package me.justeli.coins.settings;

import me.justeli.coins.Coins;
import me.justeli.coins.item.Coin;
import me.justeli.coins.item.Drop;
import me.justeli.coins.libraries.ParseUtil;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eli on November 04, 2020.
 * Coins: me.justeli.coins.settings
 */
public class ConfigParser
{
    private final Coins instance;

    public ConfigParser (Coins instance)
    {
        this.instance = instance;
        updateConfig();
    }

    public void updateConfig ()
    {
        int totalErrors = 0;

        // Load all the custom configured coins.
        if (!parseCoins()) totalErrors ++;

        // Load all the custom configured drops.
        if (!parseDrops()) totalErrors ++;

        // Load all simple variables from the config.
        for (Field field : CoinsConfig.class.getDeclaredFields())
        {
            if (!field.isAnnotationPresent(LoadConfig.class))
                continue;

            LoadConfig loadConfig = field.getAnnotation(LoadConfig.class);
            try
            {
                if (field.getType() == ChatColor.class)
                {
                    String data = instance.getConfig().getString(loadConfig.path());
                    field.set(CoinsConfig.class, ParseUtil.parse(data == null? loadConfig.usual() : data, ChatColor.class));
                }
                else
                {
                    Object data = instance.getConfig().getObject(loadConfig.path(), field.getType());
                    field.set(CoinsConfig.class, data == null? ParseUtil.parse(loadConfig.usual(), field.getType()) : data);

                    if (data == null)
                        continue;

                    // Add data to bStats metrics while we're at it.
                    instance.getMetrics().addCustomChart(new Metrics.SimplePie(loadConfig.path(), data::toString));
                }
            }
            catch (IllegalAccessException | ParseException exception)
            {
                throwError(exception.getMessage());
                totalErrors++;
            }
        }

        if (totalErrors != 0)
        {
            instance.getLogger().severe("The config of Coins loaded with " + totalErrors + " errors.");
        }
    }

    private boolean parseCoins ()
    {
        ConfigurationSection coinSection = instance.getConfig().getConfigurationSection("coins");

        if (coinSection == null)
            return throwError("There is no section in your config defining coins.");

        for (String coinName : coinSection.getKeys(false))
        {
            ConfigurationSection options = instance.getConfig().getConfigurationSection("coins." + coinName);

            if (options == null)
                return throwError("Something is not right at the coin called '" + coinName + "'.");

            List<Double> worth = options.getDoubleList("worth");
            if (worth.size() != 2)
                return throwError("The worth range of coin '" + coinName + "' can only be 2 values.");

            Material material = ParseUtil.parseEnum(Material.class, options.getString("item", "GOLD_NUGGET"));
            Sound sound = ParseUtil.parseEnum(Sound.class, options.getString("sound", "ITEM_ARMOR_EQUIP_GOLD"));

            if (material == null)
                return throwError(String.format("There is no Material called '%s'.", options.getString("item")));

            if (sound == null)
                return throwError(String.format("There is no Sound called '%s'.", options.getString("sound")));

            Coin coin = new Coin(
                    instance,
                    coinName,
                    options.getObject("uuid", UUID.class),
                    material,
                    options.getString("skull", null),
                    sound,
                    worth.get(0),
                    worth.get(1),
                    options.getBoolean("stack", true),
                    options.getInt("data", 0),
                    options.getBoolean("glow", false),
                    (float) options.getDouble("pitch", 0.5f)
            );

            if (!coin.isValid())
                return throwError(coin.getError());

            CoinsConfig.addCoin(coinName, coin);
        }

        return true;
    }


    private boolean parseDrops ()
    {
        ConfigurationSection dropSection = instance.getConfig().getConfigurationSection("drops");

        if (dropSection == null)
            return throwError("There is no section in your config defining drops.");

        for (String dropName : dropSection.getKeys(false))
        {
            ConfigurationSection options = instance.getConfig().getConfigurationSection("drops." + dropName);

            if (options == null || !options.getBoolean("enabled"))
                continue;

            Drop drop = new Drop(
                    instance,
                    dropName,
                    options.getObject("uuid", UUID.class),
                    material,
                    options.getString("skull", null),
                    sound,
                    worth.get(0),
                    worth.get(1),
                    options.getBoolean("stack", true),
                    options.getInt("data", 0),
                    options.getBoolean("glow", false),
                    (float) options.getDouble("pitch", 0.5f)
            );

            if (!drop.isValid())
                return throwError(drop.getError());

            CoinsConfig.addCoin(dropName, drop);
        }

        return true;
    }

    private boolean throwError (String message)
    {
        instance.getLogger().severe("Config of 'Coins' has an error: " + message);
        return false;
    }
}
