package me.justeli.coins;


import me.justeli.coins.api.Module;
import me.justeli.coins.command.CoinsCommand;
import me.justeli.coins.command.DepositCommand;
import me.justeli.coins.command.JobsCommand;
import me.justeli.coins.command.WithdrawCommand;

/**
 * Created by Eli on June 19, 2021.
 * Coins: me.justeli.coins
 */
public class Coins
        extends Module
{
    @Override
    public void enable ()
    {
        saveDefaultConfig();

        new CoinsCommand(this);
        //new DepositCommand(this),
        //new JobsCommand(this),
        //new WithdrawCommand(this)
    }

    @Override
    public void disable ()
    {

    }
}
