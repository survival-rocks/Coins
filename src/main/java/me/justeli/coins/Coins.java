package me.justeli.coins;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.BukkitCommandMetaBuilder;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import io.papermc.lib.PaperLib;
import me.justeli.coins.cancel.CancelInventories;
import me.justeli.coins.cancel.CoinPlace;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.commands.WithdrawCommand;
import me.justeli.coins.economy.CoinStorage;
import me.justeli.coins.economy.CoinsEconomy;
import me.justeli.coins.economy.CoinsEffect;
import me.justeli.coins.events.BukkitEvents;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.events.PaperEvents;
import me.justeli.coins.commands.CoinCommands;
import me.justeli.coins.item.CoinParticles;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Created by Eli on 12/13/2016.
 * Rewritten by Eli on October 26, 2020.
 */

public class Coins
        extends JavaPlugin
{
    private Economy economy;

    public Economy getEconomy ()
    {
        return economy;
    }

    private Settings settings;

    public Settings getSettings ()
    {
        return settings;
    }

    private static String latest;

    public static String latest ()
    {
        return latest;
    }

    private final AtomicBoolean usingPaper = new AtomicBoolean();

    public boolean isUsingPaper ()
    {
        return usingPaper.get();
    }

    public static Coins instance;

    public static Coins getInstance ()
    {
        return instance;
    }

    @Override
    public void onEnable ()
    {
        instance = this;
        Locale.setDefault(Locale.US);

        usingPaper.set(PaperLib.isPaper());

        settings = new Settings(this);
        settings.initConfig();

        if (!PaperLib.isPaper() && !PaperLib.isSpigot())
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

        coinStorage = new CoinStorage(this);
        coinsEffect = new CoinsEffect(this);
        coinParticles = new CoinParticles(this);
        coinsPickup = new CoinsPickup(this);
        dropCoin = new DropCoin(this);
        coinPlace = new CoinPlace(this);
        cancelInventories = new CancelInventories(this);

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (Config.get(Config.BOOLEAN.COINS_ECONOMY) || provider == null)
        {
            getServer().getServicesManager().register(Economy.class, new CoinsEconomy(this), this, ServicePriority.Highest);
            coinStorage.initPlayerData();

            provider = getServer().getServicesManager().getRegistration(Economy.class);
            settings.setCoinsEconomy(true);
        }

        economy = provider.getProvider();

        setupCommandManager();
        annotationParser.parse(new CoinCommands(this));
        if (Config.get(Config.BOOLEAN.ENABLE_WITHDRAW))
        {
            annotationParser.parse(new WithdrawCommand(this));
        }

        registerEvents(new PreventSpawner(), coinsPickup, dropCoin, coinPlace, cancelInventories, coinsEffect);
        addMetrics();
    }

    private void addMetrics ()
    {
        Metrics metrics = new Metrics(this, 831);

        for (Config.STRING s : Config.STRING.values())
        {
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () ->
            {
                if (s.equals(Config.STRING.SKULL_TEXTURE))
                {
                    String texture = Config.get(s);
                    return String.valueOf(texture != null && !texture.isEmpty());
                }
                return Config.get(s);
            }));
        }

        for (Config.DOUBLE s : Config.DOUBLE.values())
        {
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(Config.get(s))));
        }

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
        {
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(Config.get(s))));
        }
    }

    private void registerEvents (Listener... listeners)
    {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(isUsingPaper()? new PaperEvents() : new BukkitEvents(), this);

        for (Listener listener : listeners)
        {
            manager.registerEvents(listener, this);
        }
    }

    private BukkitCommandManager<CommandSender> commandManager;
    private AnnotationParser<CommandSender> annotationParser;

    public BukkitCommandManager<CommandSender> getCommandManager ()
    {
        return commandManager;
    }

    private void setupCommandManager ()
    {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try
        {
            this.commandManager = new PaperCommandManager<>(this, executionCoordinatorFunction, mapperFunction, mapperFunction);
        }
        catch (final Exception e)
        {
            this.getLogger().severe("Failed to initialize the command manager.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
            ((PaperCommandManager<CommandSender>) this.commandManager).registerAsynchronousCompletions();

        final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> BukkitCommandMetaBuilder.builder()
                .withDescription(p.get(StandardParameters.DESCRIPTION, "No description")).build();

        this.annotationParser = new AnnotationParser<>(this.commandManager, CommandSender.class, commandMetaFunction);
    }


    public void delayed (final int ticks, Runnable runnable)
    {
        getServer().getScheduler().runTaskLater(this, runnable, ticks);
    }

    public void sync (Runnable runnable)
    {
        getServer().getScheduler().runTask(this, runnable);
    }

    private CoinParticles coinParticles;
    private CoinStorage coinStorage;
    public CoinsPickup coinsPickup;
    private DropCoin dropCoin;
    private CoinPlace coinPlace;
    private CoinsEffect coinsEffect;
    private CancelInventories cancelInventories;

    public CoinStorage getCoinStorage ()
    {
        return coinStorage;
    }
    public CoinsEffect getCoinsEffect ()
    {
        return coinsEffect;
    }
    public CoinParticles getCoinParticles ()
    {
        return coinParticles;
    }
    public CoinsPickup getCoinsPickup ()
    {
        return coinsPickup;
    }
    public CoinPlace getCoinPlace ()
    {
        return coinPlace;
    }
    public DropCoin getDropCoin ()
    {
        return dropCoin;
    }
    public CancelInventories getCancelInventories ()
    {
        return cancelInventories;
    }
}
