package me.justeli.coins.api;

import community.leaf.textchain.adventure.Chain;

/**
 * Created by Eli on June 28, 2021.
 * Coins: me.justeli.coins.api
 */
public interface Text
        //extends Chain<Text>
{
    default Text coins (long amount)
    {
        //then(amount + "c");
        return this;
    }
}
