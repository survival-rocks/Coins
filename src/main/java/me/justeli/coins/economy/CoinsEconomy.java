package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import me.justeli.coins.settings.CoinsConfig;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eli on May 17, 2020.
 * Coins: me.justeli.coins.economy
 */
public class CoinsEconomy
        implements Economy
{
    //todo track server stats
    //todo track withdrawn coins

    private final Coins instance;

    public CoinsEconomy (Coins instance)
    {
        this.instance = instance;
    }

    @Override
    public boolean isEnabled ()
    {
        return instance.getEconomy().isEnabled();
    }

    @Override
    public String getName ()
    {
        return instance.getName();
    }

    @Override
    public boolean hasBankSupport ()
    {
        return false;
    }

    @Override
    public int fractionalDigits ()
    {
        return 0;
    }

    @Override
    public String format (double amount)
    {
        return CoinsConfig.currencyDisplay
                .replace("${naming.symbol}", CoinsConfig.currencySymbol)
                .replace("${_amount}", String.format("%,." + CoinsConfig.coinDecimals + "f", amount));
    }

    @Override
    public String currencyNamePlural ()
    {
        return CoinsConfig.pluralCoinName;
    }

    @Override
    public String currencyNameSingular ()
    {
        return CoinsConfig.singleCoinName;
    }

    @Override
    @Deprecated
    public boolean hasAccount (String playerName)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount (OfflinePlayer player)
    {
        return instance.getCoinStorage().hasData(player.getUniqueId());
    }

    @Override
    @Deprecated
    public boolean hasAccount (String playerName, String worldName)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount (OfflinePlayer player, String worldName)
    {
        return hasAccount(player);
    }

    @Override
    @Deprecated
    public double getBalance (String playerName)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance (OfflinePlayer player)
    {
        return instance.getCoinStorage().getStorage(player.getUniqueId()).getDouble("balance");
    }

    @Override
    @Deprecated
    public double getBalance (String playerName, String world)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance (OfflinePlayer player, String world)
    {
        return getBalance(player);
    }

    @Override
    @Deprecated
    public boolean has (String playerName, double amount)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return has(player, amount);
    }

    @Override
    public boolean has (OfflinePlayer player, double amount)
    {
        return amount >= 0 && getBalance(player) >= amount;
    }

    @Override
    @Deprecated
    public boolean has (String playerName, String worldName, double amount)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return has(player, amount);
    }

    @Override
    public boolean has (OfflinePlayer player, String worldName, double amount)
    {
        return has(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer (String playerName, double amount)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer (OfflinePlayer player, double amount)
    {
        if (amount == 0)
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, null);

        setBalance(player, getBalance(player) - amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer (String playerName, String worldName, double amount)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer (OfflinePlayer player, String worldName, double amount)
    {
        return withdrawPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer (String playerName, double amount)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer (OfflinePlayer player, double amount)
    {
        if (amount == 0)
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, null);

        setBalance(player, getBalance(player) + amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer (String playerName, String worldName, double amount)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer (OfflinePlayer player, String worldName, double amount)
    {
        return depositPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank (String name, String player)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse createBank (String name, OfflinePlayer player)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse deleteBank (String name)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankBalance (String name)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankHas (String name, double amount)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankWithdraw (String name, double amount)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankDeposit (String name, double amount)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner (String name, String playerName)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse isBankOwner (String name, OfflinePlayer player)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember (String name, String playerName)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse isBankMember (String name, OfflinePlayer player)
    {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public List<String> getBanks ()
    {
        return new ArrayList<>();
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount (String playerName)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount (OfflinePlayer player)
    {
        instance.getCoinStorage().createFile(player.getUniqueId());
        setBalance(player, CoinsConfig.startingBalance);
        return true;
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount (String playerName, String worldName)
    {
        OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount (OfflinePlayer player, String worldName)
    {
        return createPlayerAccount(player);
    }

    public void setBalance (OfflinePlayer player, double amount)
    {
        double balance = getBalance(player);
        if (amount == balance)
            return;

        double transaction = amount - balance;
        BalanceChangeEvent event = new BalanceChangeEvent(player, amount, balance, transaction);
        instance.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        instance.getCoinStorage().setStorage(player.getUniqueId(), "balance", amount);
        if (!player.isOnline())
        {
            double offline = instance.getCoinStorage().getStorage(player.getUniqueId()).getDouble("offlineBalance");
            instance.getCoinStorage().setStorage(player.getUniqueId(), "offlineBalance", offline + transaction);
        }
    }
}
