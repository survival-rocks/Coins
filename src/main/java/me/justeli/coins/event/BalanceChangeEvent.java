package me.justeli.coins.event;

import me.justeli.coins.api.CancellableEvent;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Created by Eli on June 19, 2021.
 * Coins: me.justeli.coins.event
 */
public class BalanceChangeEvent
        extends CancellableEvent
{
    private final OfflinePlayer offlinePlayer;
    private final UUID uniqueId;
    private final double updatedBalance;
    private final double previousBalance;
    private final double transactionAmount;

    public BalanceChangeEvent (OfflinePlayer offlinePlayer, UUID uniqueId, double updatedBalance, double previousBalance, double transactionAmount)
    {
        this.offlinePlayer = offlinePlayer;
        this.uniqueId = uniqueId;
        this.updatedBalance = updatedBalance;
        this.previousBalance = previousBalance;
        this.transactionAmount = transactionAmount;
    }

    public OfflinePlayer offlinePlayer ()
    {
        return offlinePlayer;
    }

    public UUID uniqueId ()
    {
        return uniqueId;
    }

    public double updatedBalance ()
    {
        return updatedBalance;
    }

    public double previousBalance ()
    {
        return previousBalance;
    }

    public double transactionAmount ()
    {
        return transactionAmount;
    }
}
