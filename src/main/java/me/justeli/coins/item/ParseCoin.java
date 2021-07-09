package me.justeli.coins.item;

import me.justeli.coins.Coins;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Created by Eli on June 16, 2020.
 * Coins: me.justeli.coins.item
 */
public class ParseCoin
{
    private final ItemStack stack;
    private final ItemMeta meta;

    public ParseCoin (ItemStack stack)
    {
        this.stack = stack;
        this.meta = stack.getItemMeta();
    }

    public boolean is ()
    {
        if (stack == null || meta == null)
            return false;

        return getTagDouble("coin") != -1d;
    }

    public boolean isFor (Player player)
    {
        String value = getTagString("player");
        return value == null || player.getName().equals(value);
    }

    public boolean withdrawn ()
    {
        return getTagString("withdraw") != null;
    }

    public boolean isUnique ()
    {
        return meta.hasLore();
    }

    public Double worth ()
    {
        return getTagDouble("coin");
    }

    private String getTagString (String key)
    {
        NamespacedKey namespacedKey = new NamespacedKey(Coins.get(), key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(namespacedKey, PersistentDataType.STRING))
            return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        return null;
    }

    private Double getTagDouble (String key)
    {
        NamespacedKey namespacedKey = new NamespacedKey(Coins.get(), key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(namespacedKey, PersistentDataType.DOUBLE))
            return container.get(namespacedKey, PersistentDataType.DOUBLE);
        return -1d;
    }
}
