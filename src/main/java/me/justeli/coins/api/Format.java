package me.justeli.coins.api;

import me.justeli.coins.settings.Settings;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Eli on June 16, 2020.
 * Coins: me.justeli.coins.api
 */
public class Format
{
    public static String color (String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Double number (Double amount)
    {
        return Double.parseDouble(String.format(Settings.getFormatter(), amount));
    }
}
