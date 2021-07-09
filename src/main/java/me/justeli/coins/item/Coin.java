package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.libraries.Format;
import me.justeli.coins.libraries.HeadTexture;
import me.justeli.coins.settings.CoinsConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Eli on November 04, 2020.
 * Coins: me.justeli.coins.settings
 */
public class Coin
{
    private ItemStack coin;
    private ItemMeta clonedMeta;
    private String error;

    private final Coins instance;
    private final String name;
    private final UUID uniqueId;
    private final Sound sound;
    private final double minWorth;
    private final double maxWorth;
    private final boolean stack;
    private final float pitch;

    public Coin (Coins instance, String name, UUID uniqueId, Material material, String skullTexture, Sound sound, double minWorth,
            double maxWorth, boolean stack, int modelData, boolean glow, float pitch)
    {
        this.instance = instance;
        this.name = name;
        this.uniqueId = uniqueId == null? UUID.randomUUID() : uniqueId;
        this.sound = sound;
        this.minWorth = minWorth;
        this.maxWorth = maxWorth;
        this.stack = stack;
        this.pitch = pitch;

        if (material == null && (skullTexture == null || skullTexture.isEmpty()))
        {
            error = "There is no material or skull texture defined for coin '" + name + "'.";
            return;
        }

        coin = skullTexture == null? new ItemStack(material) : HeadTexture.get(skullTexture, uniqueId);
        if (coin == null)
        {
            error = "There is no valid skull texture for coin '" + name + "'.";
            return;
        }

        ItemMeta meta = coin.getItemMeta();

        if (meta == null)
        {
            error = "The material of a coin cannot be Air.";
            return;
        }

        if (modelData > 0) meta.setCustomModelData(modelData);
        if (glow) meta.addEnchant(Enchantment.DURABILITY, 1, true);

        // todo optimize
        meta.setDisplayName(Format.color(OldConfig.get(OldConfig.STRING.NAME_OF_COIN)));
        coin.setItemMeta(meta);
    }

    public Coin value ()
    {
        setTag("coin", ThreadLocalRandom.current().nextDouble() * (maxWorth - minWorth) + minWorth);
        return this;
    }

    public Coin limit (Player player)
    {
        setTag("player", player.getName());
        return this;
    }

    public Coin value (double worth)
    {
        setTag("coin", worth);
        return this;
    }

    public Coin unique ()
    {
        setLore(String.valueOf(ThreadLocalRandom.current().nextDouble()));
        return this;
    }

    public Coin withdraw ()
    {
        setTag("withdraw", "true");
        return this;
    }

    public ItemStack generate ()
    {
        Double worth = clonedMeta.getPersistentDataContainer().get(new NamespacedKey(instance, "worth"), PersistentDataType.DOUBLE);

        if (worth == null)
            return coin.clone();

        setName(String.format("%,." + CoinsConfig.coinDecimals + "f %s", worth, worth == 1? CoinsConfig.singleCoinName : CoinsConfig.pluralCoinName));

        ItemStack generated = coin.clone();

        generated.setItemMeta(clonedMeta);
        clonedMeta = coin.getItemMeta();

        return generated;
    }

    private void setName (String name)
    {
        clonedMeta.setDisplayName(CoinsConfig.coinColor + name);
    }

    private void setLore (String... lore)
    {
        clonedMeta.setLore(Arrays.asList(lore));
    }

    private void setTag (String key, String tag)
    {
        NamespacedKey namespacedKey = new NamespacedKey(instance, key);
        clonedMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, tag);
    }

    private void setTag (String key, Double tag)
    {
        NamespacedKey namespacedKey = new NamespacedKey(instance, key);
        clonedMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.DOUBLE, tag);
    }

    public String getName ()
    {
        return name;
    }

    public Sound getSound ()
    {
        return sound;
    }

    public double getMinWorth ()
    {
        return minWorth;
    }

    public double getMaxWorth ()
    {
        return maxWorth;
    }

    public ItemStack getRawCoin ()
    {
        return coin.clone();
    }

    public boolean isValid ()
    {
        return error == null;
    }

    public String getError ()
    {
        return error;
    }

    public boolean isStackable ()
    {
        return stack;
    }

    public float getPitch ()
    {
        return pitch;
    }

    public UUID getUniqueId ()
    {
        return uniqueId;
    }
}
