package me.justeli.coins.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Eli on 5/20/2017.
 * Coins: me.justeli.coins.economy
 */
public class BalanceChangeEvent
        extends Event
        implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;
    private final double newBalance;
    private boolean cancelled;
    private final double previousBalance;
    private final double transactionAmount;

    public BalanceChangeEvent (OfflinePlayer eventPlayer, double newBalance, double previousBalance, double transactionAmount)
    {
        this.player = eventPlayer;
        this.newBalance = newBalance;
        this.previousBalance = previousBalance;
        this.transactionAmount = transactionAmount;
    }

    public OfflinePlayer getPlayer ()
    {
        return player;
    }

    public double getNewBalance ()
    {
        return newBalance;
    }

    public double getPreviousBalance ()
    {
        return previousBalance;
    }

    public double getTransactionAmount ()
    {
        return transactionAmount;
    }


    public HandlerList getHandlers ()
    {
        return handlers;
    }

    public static HandlerList getHandlerList ()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled ()
    {
        return cancelled;
    }

    @Override
    public void setCancelled (boolean cancel)
    {
        cancelled = cancel;
    }
}