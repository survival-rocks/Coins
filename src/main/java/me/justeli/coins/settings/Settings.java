package me.justeli.coins.settings;

import me.justeli.coins.Coins;
import me.justeli.coins.economy.CoinStorage;
import me.justeli.coins.item.GeneratedCoin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Created by Eli on 12/14/2016.
 */

public class Settings
{
    private final Coins instance;

    public Settings (Coins instance)
    {
        this.instance = instance;
    }

    private final static HashMap<Messages, String> language = new HashMap<>();
    private final static HashMap<EntityType, Integer> multiplier = new HashMap<>();
    private final static AtomicReference<Sound> sound = new AtomicReference<>();
    private final static AtomicReference<String> formatter = new AtomicReference<>();

    public static HashMap<Messages, String> getLanguage ()
    {
        return language;
    }

    public static HashMap<EntityType, Integer> getMultiplier ()
    {
        return multiplier;
    }

    public static Sound getSound ()
    {
        return sound.get();
    }

    public static String getFormatter ()
    {
        return formatter.get();
    }

    private GeneratedCoin generatedCoin;
    public GeneratedCoin getGeneratedCoin ()
    {
        return generatedCoin;
    }

    public void setCoinsEconomy (boolean value)
    {
        Config.set(Config.BOOLEAN.COINS_ECONOMY, value);
        FileConfiguration config = getFile();
        config.set("coinsEconomy", value);

        instance.getCoinStorage().saveFile(config, new File(instance.getDataFolder() + File.separator + "config.yml"));
        //todo make it not reset the config owo
    }

    private FileConfiguration getFile ()
    {
        File config = new File(instance.getDataFolder() + File.separator + "config.yml");

        if (!config.exists())
            instance.saveDefaultConfig();

        return YamlConfiguration.loadConfiguration(config);
    }

    public boolean initConfig ()
    {
        FileConfiguration file = getFile();
        try
        {
            for (Config.BOOLEAN s : Config.BOOLEAN.values())
            {
                Config.set(s, file.getBoolean(s.getKey()));
            }

            for (Config.STRING s : Config.STRING.values())
            {
                String value = file.getString(s.getKey());
                Config.set(s, value == null? s.getDefault() : value);
            }

            for (Config.DOUBLE s : Config.DOUBLE.values())
            {
                Config.set(s, file.getDouble(s.getKey()));
            }

            for (Config.ARRAY s : Config.ARRAY.values())
            {
                Config.set(s, new HashSet<>(file.getStringList(s.getKey())));
            }

            ConfigurationSection mobMultiplier = file.getConfigurationSection(Config.CUSTOM.MOB_MULTIPLIER.getKey());
            if (mobMultiplier != null)
            {
                for (String key : mobMultiplier.getKeys(false))
                {
                    try
                    {
                        EntityType type = EntityType.valueOf(key.toUpperCase());
                        multiplier.put(type, file.getInt(Config.CUSTOM.MOB_MULTIPLIER.getKey() + "." + key));
                    }
                    catch (IllegalArgumentException e)
                    {
                        errorMessage(Msg.NO_SUCH_ENTITY, key.toUpperCase());
                    }
                }
            }
        }
        catch (NullPointerException e)
        {
            errorMessage(Msg.OUTDATED_CONFIG);
            return false;
        }

        try
        {
            sound.set(Sound.valueOf(Config.get(Config.STRING.SOUND_NAME).toUpperCase()));
        }
        catch (IllegalArgumentException e)
        {
            errorMessage(Msg.NO_SUCH_SOUND);
            return false;
        }

        formatter.set("%." + Config.get(Config.DOUBLE.MONEY_DECIMALS).intValue() + "f");

        generatedCoin = new GeneratedCoin();
        generatedCoin.init();

        return setLanguage();
    }

    public void remove ()
    {
        multiplier.clear();
        Config.clear();
    }

    public String getSettings ()
    {
        StringBuilder message = new StringBuilder(Messages.LOADED_SETTINGS.toString() + "\n&r");

        for (Config.STRING s : Config.STRING.values())
            message.append(s.getKey()).append("&7: &8").append(Config.get(s)).append("\n&r");

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            message.append(s.getKey()).append("&7: ").append(Config.get(s).toString().replace("true", "&atrue").replace("false", "&cfalse")).append("\n&r");

        for (Config.DOUBLE s : Config.DOUBLE.values())
            message.append(s.getKey()).append("&7: &e").append(Config.get(s)).append("\n&r");

        for (Config.ARRAY s : Config.ARRAY.values())
            message.append(s.getKey()).append("&7: &b").append(Config.get(s)).append("\n&r");

        return message.toString();
    }

    private boolean setLanguage ()
    {
        for (String lang : new String[]{"english", "dutch", "spanish", "german", "french", "swedish", "chinese", "hungarian"})
        {
            if (!new File(instance.getDataFolder() + File.separator + "language" + File.separator + lang + ".json").exists())
            {
                instance.saveResource("language/" + lang + ".json", false);
            }
        }

        FileConfiguration file = getFile();
        String lang = file.getString("language");
        boolean failure = false;

        if (lang == null)
        {
            failure = true;
            lang = "english";
        }

        lang = lang.toLowerCase();

        try
        {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(new InputStreamReader(new FileInputStream(instance
                    .getDataFolder() + File.separator + "language" + File.separator + lang + ".json"), StandardCharsets.UTF_8));
            JSONObject json = (JSONObject) object;

            if (json != null) json.forEach((key, value) -> language.put(Messages.valueOf(key.toString()), value.toString()));
        }
        catch (FileNotFoundException e)
        {
            errorMessage(Msg.LANGUAGE_NOT_FOUND, file.getString("language"));
            return false;
        }
        catch (ParseException | IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return !failure;
    }

    public enum Msg
    {
        OUTDATED_CONFIG,
        LANGUAGE_NOT_FOUND,
        NO_SUCH_ENTITY,
        NO_SUCH_SOUND,
        NO_SUCH_MATERIAL,
        NO_TRANSLATION,
    }

    public static void errorMessage (Msg msg, String... input)
    {
        Logger logger = Bukkit.getLogger();
        switch (msg)
        {
            case OUTDATED_CONFIG:
                logger.severe("Your config of Coins is outdated, update the Coins config.yml.");
                logger.severe("You can copy it from here: https://github.com/JustEli/Coins/blob/master/src/config.yml");
                logger.severe("Use /coins reload afterwards. You could also remove the config if you haven't configured it.");
                if (input.length != 0)
                    logger.severe("This option is probably missing (add it): " + Arrays.toString(input));
                break;
            case LANGUAGE_NOT_FOUND:
                logger.severe("The language '" + input[0] + "' that you set in your config does not exist.");
                logger.severe("Check all available languages in the folder 'Coins/language'.");
                break;
            case NO_SUCH_ENTITY:
                logger.severe("There is no entity with the name '" + input[0] + "', please change the Coins config.");
                logger.severe("Get types from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");
                break;
            case NO_SUCH_MATERIAL:
                logger.severe("There is no material with the name '" + input[0] + "', please change the Coins config.");
                logger.severe("Get materials from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
                break;
            case NO_SUCH_SOUND:
                logger.severe("The sound '" + input[0] + "' does not exist. Change it in the Coins config.");
                logger.severe("Please use a sound from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");
                break;
            case NO_TRANSLATION:
                logger.severe("The translation for '" + input[0] + "' was not found.");
                logger.severe("Please add it to the {language}.json file.");
                logger.severe("Or delete your /language/ folder in /Coins/. RECOMMENDED");
                break;
        }
    }
}
