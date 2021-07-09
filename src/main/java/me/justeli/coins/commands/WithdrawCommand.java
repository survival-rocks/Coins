package me.justeli.coins.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import me.justeli.coins.Coins;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Eli on August 05, 2020.
 * Coins: me.justeli.coins.commands
 */
public class WithdrawCommand
{
    public final Coins instance;

    public WithdrawCommand (Coins instance)
    {
        this.instance = instance;
    }

    @CommandMethod("withdraw <worth> [amount]")
    @CommandPermission("coins.withdraw")
    public void withdraw (Player player,
            @Argument("worth") @Range(min = "1", max = "10000") double worth,
            @Argument("amount") @Range(min = "1", max = "64") Integer inputAmount)
    {
        if (Config.get(Config.ARRAY.DISABLED_WORLDS).contains(player.getWorld().getName()))
        {
            player.sendMessage(Messages.COINS_DISABLED.toString());
            return;
        }

        if (player.getInventory().firstEmpty() == -1)
        {
            player.sendMessage(Messages.INVENTORY_FULL.toString());
            return;
        }

        final int amount = inputAmount == null? 1 : inputAmount;
        double cost = worth * amount;

        if (worth < 1 || amount < 1 || worth > Config.get(Config.DOUBLE.MAX_WITHDRAW_AMOUNT) || !instance.getEconomy().has(player, cost))
        {
            player.sendMessage(Messages.NOT_THAT_MUCH.toString());
            return;
        }

        instance.sync(() ->
        {
            ItemStack coin = new Coin(worth).withdraw().create();
            coin.setAmount(amount);

            player.getInventory().addItem(coin);
            instance.getEconomy().withdrawPlayer(player, cost);

            // todo update message
            player.sendMessage(Messages.WITHDRAW_COINS.toString().replace("{0}", Double.toString(worth)));
        });
    }
}
