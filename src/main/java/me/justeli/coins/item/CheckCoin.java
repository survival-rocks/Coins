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
public class CheckCoin
{
    private final ItemStack stack;
    private final ItemMeta meta;

    public CheckCoin (ItemStack stack)
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

    public boolean isFor (Player p)
    {
        String value = getTagString("player");
        return value == null || p.getName().equals(value);
    }

    public boolean withdrawed ()
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
        NamespacedKey namespacedKey = new NamespacedKey(Coins.getInstance(), key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(namespacedKey, PersistentDataType.STRING))
            return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        return null;
    }

    private Double getTagDouble (String key)
    {
        NamespacedKey namespacedKey = new NamespacedKey(Coins.getInstance(), key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(namespacedKey, PersistentDataType.DOUBLE))
            return container.get(namespacedKey, PersistentDataType.DOUBLE);
        return -1d;
    }
}
