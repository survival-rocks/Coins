package me.justeli.coins.libraries;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Eli on 6 jan. 2020.
 * spigotPlugins: me.justeli.coins.libraries
 */
public class HeadTexture
{
    private static final HashMap<String, ItemStack> coin = new HashMap<>();

    public static ItemStack get (String texture, UUID uniqueId)
    {
        if (coin.containsKey(texture))
            return coin.get(texture);

        if (texture.isEmpty())
            return null;

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta == null)
            return skull;

        GameProfile profile = new GameProfile(uniqueId == null? UUID.randomUUID() : uniqueId, "Coin");
        profile.getProperties().put("textures", new Property("textures", texture));

        try
        {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);

            skull.setItemMeta(skullMeta);
            coin.put(texture, skull);

            return skull;
        }
        catch (NoSuchFieldException | SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
            return skull;
        }
    }
}
