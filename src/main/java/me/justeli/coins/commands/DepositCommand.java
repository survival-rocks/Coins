package me.justeli.coins.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import me.justeli.coins.Coins;
import org.bukkit.entity.Player;

/**
 * Created by Eli on January 11, 2021.
 * Coins: me.justeli.coins.commands
 */
public class DepositCommand
{
    private final Coins instance;

    public DepositCommand (Coins instance)
    {
        this.instance = instance;
    }

    @CommandMethod ("deposit")
    @CommandPermission ("coins.withdraw")
    public void deposit (Player player)
    {

    }
}
