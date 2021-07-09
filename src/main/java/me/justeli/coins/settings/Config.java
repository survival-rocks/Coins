package me.justeli.coins.settings;

import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Eli on 12/14/2016.
 */

public class Config
{
    private final static HashMap<BOOLEAN, Boolean> configBooleans = new HashMap<>();
    private final static HashMap<STRING, String> configStrings = new HashMap<>();
    private final static HashMap<DOUBLE, Double> configDoubles = new HashMap<>();
    private final static HashMap<ARRAY, Set<String>> configArrays = new HashMap<>();

    public static Set<String> get (ARRAY key)
    {
        return configArrays.get(key);
    }

    public static Boolean get (BOOLEAN key)
    {
        return configBooleans.get(key);
    }

    public static Double get (DOUBLE key)
    {
        return configDoubles.get(key);
    }

    public static String get (STRING key)
    {
        return configStrings.get(key);
    }

    public static void set (ARRAY key, Set<String> value)
    {
        configArrays.put(key, value);
    }

    public static void set (BOOLEAN key, Boolean value)
    {
        configBooleans.put(key, value);
    }

    public static void set (DOUBLE key, Double value)
    {
        configDoubles.put(key, value);
    }

    public static void set (STRING key, String value)
    {
        configStrings.put(key, value);
    }

    public static void clear ()
    {
        configArrays.clear();
        configBooleans.clear();
        configDoubles.clear();
        configStrings.clear();
    }

    public enum BOOLEAN
    {
        STACK_COINS(false),
        SPAWNER_DROP(false),
        PASSIVE_DROP(false),
        PICKUP_SOUND(true),
        LOSE_ON_DEATH(true),
        PLAYER_DROP(true),
        PREVENT_ALTS(true),
        ENABLE_WITHDRAW(true),
        DROP_EACH_COIN(true),
        PREVENT_SPLITS(true),
        TAKE_PERCENTAGE(false),
        DROP_ON_DEATH(false),
        ONLY_EXPERIENCE_BLOCKS(true),
        DISABLE_HOPPERS(true),
        DROP_WITH_ANY_DEATH(false),
        COINS_ECONOMY(false),
        COINS_EFFECT(true),
        TRACK_ECONOMY(false),
        ENCHANTED_COIN(false),
        PICKUP_EFFECT(false),
        ;

        private final String key;
        private final Boolean value;

        BOOLEAN (Boolean value)
        {
            this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
            this.value = value;
        }

        public String getKey ()
        {
            return this.key;
        }

        public Boolean getDefault ()
        {
            return this.value;
        }
    }

    public enum STRING
    {
        NAME_OF_COIN("&6Coin"),
        COIN_ITEM("sunflower"),
        DEPOSIT_MESSAGE("&2+ &a{display}"),
        WITHDRAW_MESSAGE("&4- &c{display}"),
        SOUND_NAME("ITEM_ARMOR_EQUIP_GOLD"),
        CURRENCY_SYMBOL("$"),
        MULTI_SUFFIX("s"),
        SKULL_TEXTURE(""),
        DISPLAY_CURRENCY("{$}{amount}"),
        ;

        private final String key;
        private final String value;

        STRING (String value)
        {
            this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
            this.value = value;
        }

        public String getKey ()
        {
            return this.key;
        }

        public String getDefault ()
        {
            return value;
        }
    }

    public enum DOUBLE
    {
        DROP_CHANCE(0.9d),
        MAX_WITHDRAW_AMOUNT(10000d),
        MONEY_AMOUNT__FROM(3d),
        MONEY_AMOUNT__TO(7d),
        MONEY_TAKEN__FROM(10d),
        MONEY_TAKEN__TO(30d),
        MONEY_DECIMALS(2d),
        MINE_PERCENTAGE(0.2d),
        SOUND_PITCH(0.3d),
        SOUND_VOLUME(0.5d),
        LIMIT_FOR_LOCATION(1d),
        DONT_LOSE_BELOW(0d),
        PERCENTAGE_PLAYER_HIT(0.9d),
        STARTING_BALANCE(100d),
        MAXIMUM_ALLOWED(1000000000000d),
        CUSTOM_MODEL_DATA(0d),
        ;

        private final String key;
        private final Double value;

        DOUBLE (Double value)
        {
            this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name().replace("__", "."));
            this.value = value;
        }

        public String getKey ()
        {
            return this.key;
        }

        public Double getDefault ()
        {
            return value;
        }
    }

    public enum ARRAY
    {
        DISABLED_WORLDS(new HashSet<>()),
        ;

        private final String key;
        private final Set<String> value;

        ARRAY (Set<String> value)
        {
            this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
            this.value = value;
        }

        public String getKey ()
        {
            return this.key;
        }

        public Set<String> getDefault ()
        {
            return value;
        }
    }

    public enum CUSTOM
    {
        MOB_MULTIPLIER,
        ;

        private final String key;

        CUSTOM ()
        {
            this.key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
        }

        public String getKey ()
        {
            return this.key;
        }
    }
}
