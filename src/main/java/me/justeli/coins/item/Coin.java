package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Format;
import me.justeli.coins.settings.Config;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Eli on June 08, 2020.
 * Coins: me.justeli.coins.item
 */
public class Coin
{
    private final ItemStack coin;
    private final ItemMeta meta;
    private final Double worth;

    private static final Random RANDOM = new Random();

    public Coin (double worth)
    {
        this.coin = Coins.getInstance().getSettings().getGeneratedCoin().getCoin();
        this.meta = coin.getItemMeta();
        this.worth = worth;
    }

    public Coin ()
    {
        this.coin = Coins.getInstance().getSettings().getGeneratedCoin().getCoin();
        this.meta = coin.getItemMeta();

        if (Config.get(Config.BOOLEAN.DROP_EACH_COIN))
        {
            this.worth = 1d;
        }
        else
        {
            double second = Config.get(Config.DOUBLE.MONEY_AMOUNT__FROM);
            double first = Config.get(Config.DOUBLE.MONEY_AMOUNT__TO) - second;

            this.worth = RANDOM.nextDouble() * first + second;
        }
    }

    public Coin setFor (Player p)
    {
        setTag("player", p.getName());
        return this;
    }

    public Coin worth (double coins)
    {
        setTag("coin", coins);
        return this;
    }

    public Coin unique ()
    {
        setLore(String.valueOf(ThreadLocalRandom.current().nextDouble()));
        return this;
    }

    public Coin stack (boolean stack)
    {
        if (!stack) unique();
        else setLore();
        return this;
    }

    public Coin withdraw ()
    {
        setName("&e" + worth.longValue() + " " + Config.get(Config.STRING.NAME_OF_COIN) + Config.get(Config.STRING.MULTI_SUFFIX));
        setTag("withdraw", "true");
        return this;
    }

    public ItemStack create ()
    {
        setTag("coin", worth);
        coin.setItemMeta(meta);
        return coin;
    }

    private void setName (String name)
    {
        meta.setDisplayName(Format.color(name));
    }

    private void setLore (String... lore)
    {
        meta.setLore(Arrays.asList(lore));
    }

    private void setTag (String key, String tag)
    {
        NamespacedKey namespacedKey = new NamespacedKey(Coins.getInstance(), key);
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, tag);
    }

    private void setTag (String key, Double tag)
    {
        NamespacedKey namespacedKey = new NamespacedKey(Coins.getInstance(), key);
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.DOUBLE, tag);
    }
}
