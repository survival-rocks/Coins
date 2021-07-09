package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.api.PrepareCommand;

/**
 * Created by Eli on June 19, 2021.
 * Coins: me.justeli.coins.command
 */
public class CoinsCommand
        extends PrepareCommand
{
    public CoinsCommand (Coins plugin)
    {
        super(plugin, "coins");

        manager().command(command().literal("reload").handler(context ->
        {
            manager().taskRecipe().begin(context).synchronous(c ->
            {
                c.getSender().sendMessage("done async");
            }).execute();
        }));
    }
}
