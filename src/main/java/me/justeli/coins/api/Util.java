package me.justeli.coins.api;

import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eli on May 02, 2021.
 * Coins: me.justeli.coins.api
 */
public class Util
{
    private static final Pattern RGB_COLOR = Pattern.compile("(?<!\\\\)(&#[a-fA-F0-9]{6})");

    public static String color (String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', parseRgb(msg));
    }

    private static String parseRgb (String msg)
    {
        Matcher matcher = RGB_COLOR.matcher(msg);
        while (matcher.find())
        {
            String color = msg.substring(matcher.start(), matcher.end());
            String hex = color.replace("&", "").toUpperCase();
            msg = msg.replace(color, ChatColor.of(hex).toString());
            matcher = RGB_COLOR.matcher(msg);
        }
        return msg;
    }

    public static String getLatestVersion (String githubRepo)
    {
        try
        {
            URL url = new URL("https://api.github.com/repos/" + githubRepo + "/releases/latest");
            URLConnection request = url.openConnection();
            request.connect();

            return new JsonParser().parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject().get("tag_name").getAsString();
        }
        catch (IOException exception)
        {
            return null;
        }
    }
}
