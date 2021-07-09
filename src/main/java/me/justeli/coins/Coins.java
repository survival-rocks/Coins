package me.justeli.coins;

import community.leaf.tasks.bukkit.BukkitTaskSource;
import io.papermc.lib.PaperLib;
import me.justeli.coins.api.Util;
import me.justeli.coins.commands.CoinCommands;
import me.justeli.coins.commands.WithdrawCommand;
import me.justeli.coins.economy.CoinStorage;
import me.justeli.coins.economy.CoinsEconomy;
import me.justeli.coins.economy.SpendCoinsEffect;
import me.justeli.coins.events.BukkitEvents;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.events.PaperEvents;
import me.justeli.coins.item.CoinParticles;
import me.justeli.coins.libraries.Multiplier;
import me.justeli.coins.prevent.CoinPlacement;
import me.justeli.coins.prevent.Inventories;
import me.justeli.coins.prevent.UnfairMobs;
import me.justeli.coins.settings.CoinsConfig;
import me.justeli.coins.settings.Messages;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * Created by Eli on 12/13/2016.
 * Rewritten by Eli on October 26, 2020.
 */

// todo use integrated bstats metrics from spigot
// todo host on a maven repository
// https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages
// todo proper formatting before saving to disk i.e.  %.2f
// todo add option to not let balance go negative (with dropOnDeath: true)
// todo coin and/or bill textures using NBT data and a resource pack
// todo Can you add config for specific blocks for mining?
// todo set different materials as different worths, ex: you could have bronze, silver and gold coins
// todo generating of coins in dungeons chests
// todo don't drop/take coins when player has balance below 0
// todo Coins Protection : true #when coins drop they can only be picked up for a short moment by the one who make them drop.
// todo fire BalanceChangeEvent from Essentials if installed

// todo Able to set multiple denominations of coin worth to something other than $1ea. (IE: $1 = skull 1 | $5 = skull 2 | $10 = skull 3....etc)
// todo Able to remove auto-deposit, Introduce /deposit option instead of right-click
// todo Give command (IE: /coin give Dingo 5 64) puts a $5 stack of coins in Dingo's inventory.
// todo WorldGuard integration: Do mobs/mob spawners in this area drop coins
// todo Ability to broadcast coin drop with a timer/countdown
// todo Able to set "limit for location timer" yourself
// todo Keep inventory coins with death option
// todo Coin From-To drop setting available for every mob individually
// todo https://mythicmobs.net/javadocs/
// todo option to disallow mobs from picking up coins
// todo from 1.x: add meta.setCustomModelData(configOption);

// https://www.spigotmc.org/resources/pickupmoney.11334/
// chinese site: https://www.mcbbs.net/thread-1051835-1-1.html

public class Coins
        extends JavaPlugin implements BukkitTaskSource
{
    public static Coins staticInstance;
    public static Coins get()
    {
        return staticInstance;
    }

    private Economy economy;
    public Economy getEconomy ()
    {
        return economy;
    }

    private String pluginVersion;
    public String getPluginVersion ()
    {
        return pluginVersion;
    }

    @Override
    public Plugin plugin ()
    {
        return this;
    }

    @Override
    public void onEnable ()
    {
        staticInstance = this;
        Locale.setDefault(Locale.US);
        PaperLib.suggestPaper(this);

        if (getServer().getVersion().contains("Bukkit"))
        {
            getLogger().severe(ChatColor.RED.toString() + ChatColor.UNDERLINE + Messages.USING_BUKKIT);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            getLogger().severe(ChatColor.RED.toString() + ChatColor.UNDERLINE + Messages.INSTALL_VAULT);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        metrics = new Metrics(this, 831);

        settings = new Settings(this);
        multiplier = new Multiplier();
        coinStorage = new CoinStorage(this);
        spendCoinsEffect = new SpendCoinsEffect(this);
        coinParticles = new CoinParticles(this);
        coinsPickup = new CoinsPickup(this);
        dropCoin = new DropCoin(this);
        coinPlacement = new CoinPlacement(this);
        inventories = new Inventories(this);
        unfairMobs = new UnfairMobs(this);

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (CoinsConfig.economyIntegration || provider == null)
        {
            getServer().getServicesManager().register(Economy.class, new CoinsEconomy(this), this, ServicePriority.Highest);
            coinStorage.initPlayerData();

            provider = getServer().getServicesManager().getRegistration(Economy.class);
            settings.setCoinsEconomy(true);
        }

        if (provider == null)
        {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        economy = provider.getProvider();

        setupCommandManager();
        annotationParser.parse(new CoinCommands(this));
        if (CoinsConfig.withdrawEnabled)
        {
            annotationParser.parse(new WithdrawCommand(this));
        }

        registerEvents(unfairMobs, coinsPickup, dropCoin, coinPlacement, inventories, spendCoinsEffect);

        checkVersion();
        addOtherMetrics();
    }

    private void addOtherMetrics ()
    {
        metrics.addCustomChart(new Metrics.SimplePie("other.amountOfCoins", () -> String.valueOf(CoinsConfig.getCoins().size())));

        for (OldConfig.STRING s : OldConfig.STRING.values())
        {
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () ->
            {
                if (s.equals(OldConfig.STRING.SKULL_TEXTURE))
                {
                    String texture = OldConfig.get(s);
                    return String.valueOf(texture != null && !texture.isEmpty());
                }
                return OldConfig.get(s);
            }));
        }

        for (OldConfig.DOUBLE s : OldConfig.DOUBLE.values())
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(OldConfig.get(s))));

        for (OldConfig.BOOLEAN s : OldConfig.BOOLEAN.values())
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(OldConfig.get(s))));
    }


    private void checkVersion ()
    {
        async().run(() ->
        {
            this.pluginVersion = Util.getLatestVersion("JustEli/Coins");

            if (!getDescription().getVersion().equals(this.pluginVersion))
            {
                getLogger().warning(Messages.CONSIDER_UPDATING.format(this.pluginVersion));
                getLogger().warning("https://www.spigotmc.org/resources/coins.33382/");
            }
        });
    }

    private void registerEvents (Listener... listeners)
    {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(PaperLib.isPaper()? new PaperEvents(this) : new BukkitEvents(this), this);

        for (Listener listener : listeners)
            manager.registerEvents(listener, this);
    }

    private Metrics metrics;

    public Metrics getMetrics ()
    {
        return metrics;
    }

    private CoinParticles coinParticles;
    private CoinStorage coinStorage;
    public CoinsPickup coinsPickup;
    private DropCoin dropCoin;
    private CoinPlacement coinPlacement;
    private SpendCoinsEffect spendCoinsEffect;
    private Inventories inventories;
    private Settings settings;
    private Multiplier multiplier;
    private UnfairMobs unfairMobs;

    public CoinStorage getCoinStorage ()
    {
        return coinStorage;
    }
    public SpendCoinsEffect getCoinsEffect ()
    {
        return spendCoinsEffect;
    }
    public CoinParticles getCoinParticles ()
    {
        return coinParticles;
    }
    public CoinsPickup getCoinsPickup ()
    {
        return coinsPickup;
    }
    public CoinPlacement getCoinPlace ()
    {
        return coinPlacement;
    }
    public DropCoin getDropCoin ()
    {
        return dropCoin;
    }
    public Inventories getCancelInventories ()
    {
        return inventories;
    }
    public Settings getSettings ()
    {
        return settings;
    }
    public Multiplier getMultiplier ()
    {
        return multiplier;
    }
    public UnfairMobs getCancelSpawners ()
    {
        return unfairMobs;
    }
}
