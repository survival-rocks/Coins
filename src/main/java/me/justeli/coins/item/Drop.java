package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.handlers.EventType;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Eli on January 11, 2021.
 * Coins: me.justeli.coins.item
 */
public class Drop
{
    private final Coins instance;
    private final String name;

    private final Set<EventType> eventTypes = new HashSet<>();

    public Drop (Coins instance, String name)
    {
        this.instance = instance;
        this.name = name;
    }

    // materials
    // spawners
    // instances
    // events
    // entities
}
