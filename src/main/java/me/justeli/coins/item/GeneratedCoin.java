package me.justeli.coins.item;

import me.justeli.coins.api.Format;
import me.justeli.coins.api.SkullValue;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Eli on June 16, 2020.
 * Coins: me.justeli.coins.item
 */
public class GeneratedCoin
{
    private ItemStack coin;
    public void init ()
    {
        String texture = Config.get(Config.STRING.SKULL_TEXTURE);
        String material = Config.get(Config.STRING.COIN_ITEM).replace(" ", "_").toUpperCase();

        coin = texture == null || texture.isEmpty()? new ItemStack(Material.valueOf(material)) : SkullValue.get(texture);

        if (coin == null)
        {
            Settings.errorMessage(Settings.Msg.NO_SUCH_MATERIAL, material);
            return;
        }

        ItemMeta meta = this.coin.getItemMeta();
        if (meta != null)
        {
            meta.setDisplayName(Format.color(Config.get(Config.STRING.NAME_OF_COIN)));

            if (Config.get(Config.BOOLEAN.ENCHANTED_COIN))
            {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
            }

            int data = Config.get(Config.DOUBLE.CUSTOM_MODEL_DATA).intValue();
            if (data > 0)
            {
                meta.setCustomModelData(data);
            }
        }

        this.coin.setItemMeta(meta);
    }

    public ItemStack getCoin ()
    {
        return coin.clone();
    }
}
