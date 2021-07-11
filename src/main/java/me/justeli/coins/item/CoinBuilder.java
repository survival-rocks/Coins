package me.justeli.coins.item;

/**
 * Created by Eli on July 10, 2021.
 * Coins: me.justeli.coins.item
 */
public class CoinBuilder
{
    private final String configuredCoin;

    private CoinBuilder (String configuredCoin)
    {
        this.configuredCoin = configuredCoin;
    }

    public static CoinBuilder of (String configuredCoin)
    {
        return new CoinBuilder(configuredCoin);
    }
}
