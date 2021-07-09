package me.justeli.coins.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import me.justeli.coins.Coins;
import me.justeli.coins.settings.CoinsConfig;
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
            @Argument("worth") @Range(min = "1") double worth,
            final @Argument(value = "amount", defaultValue = "1") @Range(min = "1", max = "64") Integer amount)
    {
        if (CoinsConfig.disabledInWorld(player.getWorld()))
        {
            player.sendMessage(Messages.COINS_DISABLED.toString());
            return;
        }

        if (player.getInventory().firstEmpty() == -1)
        {
            player.sendMessage(Messages.INVENTORY_FULL.toString());
            return;
        }

        double cost = worth * amount;

        if (worth < 1 || amount < 1 || worth > OldConfig.get(OldConfig.DOUBLE.MAX_WITHDRAW_AMOUNT) || !instance.getEconomy().has(player, cost))
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
