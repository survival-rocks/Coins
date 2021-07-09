package me.justeli.coins.libraries;

import net.md_5.bungee.api.ChatColor;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Eli on January 11, 2021.
 * Coins: me.justeli.coins.libraries
 */
public final class ParseUtil
{
    public interface IStringParser<E>
    {
        E parse (String s)
        throws ParseException;
    }

    private ParseUtil ()
    {
        registerStringParser(Integer.class, Integer::parseInt);
        registerStringParser(Long.class, Long::parseLong);
        registerStringParser(Float.class, Float::parseFloat);
        registerStringParser(Double.class, Double::parseDouble);
        registerStringParser(Boolean.class, Boolean::parseBoolean);
        registerStringParser(Character.class, s -> s.charAt(0));
        registerStringParser(ChatColor.class, s ->
        {
            if (s.length() == 1)
            {
                ChatColor color = ChatColor.getByChar(s.charAt(0));
                if (color != null)
                    return color;
            }

            ChatColor color = ChatColor.valueOf(s.toUpperCase());
            if (color != null)
                return color;

            if (!s.contains("#"))
                s = "#" + s;

            return ChatColor.of(s);
        });
    }

    private static final Map<Class<?>, IStringParser<?>> STRING_PARSERS = new ConcurrentHashMap<>();

    public static <E> void registerStringParser (Class<E> c, IStringParser<E> p)
    {
        STRING_PARSERS.put(c, p);
    }

    @SuppressWarnings ("unchecked")
    public static <E> IStringParser<E> getStringParser (Class<E> c)
    {
        return (IStringParser<E>) STRING_PARSERS.get(c);
    }

    public static <E> E parse (String s, Class<E> clazz)
    throws ParseException
    {
        if (s == null || s.length() == 0 || clazz == null)
        {
            throw new IllegalArgumentException();
        }

        IStringParser<E> stringParser = getStringParser(clazz);
        if (stringParser == null)
        {
            throw new ParseException("No valid parser found for " + clazz.getName() + ".", -1);
        }
        return stringParser.parse(s);
    }

    public static <E extends Enum<E>> E parseEnum (Class<E> passed, String name)
    {
        if (name == null)
            return null;

        try
        {
            return Enum.valueOf(passed, name.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException exception)
        {
            return null;
        }
    }
}
