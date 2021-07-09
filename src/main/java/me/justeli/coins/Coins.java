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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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

    private static String update;

    public static String getUpdate ()
    {
        return update;
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

    @Override
    public void onEnable ()
    {
        instance = this;
        Locale.setDefault(Locale.US);

        usingPaper.set(getServer().getVersion().contains("Paper"));

        settings = new Settings(this);
        settings.initConfig();

        if (Bukkit.getVersion().contains("Bukkit"))
        {
            getLogger().severe(ChatColor.RED.toString() + ChatColor.UNDERLINE + Messages.USING_BUKKIT);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else if (!isUsingPaper())
        {
            getLogger().warning(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + Messages.USE_PAPER);
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
            Bukkit.getServicesManager().register(Economy.class, new CoinsEconomy(this), this, ServicePriority.Highest);
            coinStorage.initPlayerData();

            provider = getServer().getServicesManager().getRegistration(Economy.class);
            settings.setCoinsEconomy(true);
        }

        economy = provider.getProvider();

        setupCommandManager();
        annotationParser.parse(new CoinCommands(this));
        if (Config.get(Config.BOOLEAN.ENABLE_WITHDRAW))
            annotationParser.parse(new WithdrawCommand(this));

        registerEvents(new PreventSpawner(), coinsPickup, dropCoin, coinPlace, cancelInventories, coinsEffect);

        checkVersion();
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
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(Config.get(s))));

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            metrics.addCustomChart(new Metrics.SimplePie(s.getKey(), () -> String.valueOf(Config.get(s))));
    }


    private void checkVersion ()
    {
        async(() ->
        {
            String version;
            try
            {
                URL url = new URL("https://api.github.com/repos/JustEli/Coins/releases/latest");
                URLConnection request = url.openConnection();
                request.connect();

                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
                JsonObject json = root.getAsJsonObject();
                version = json.get("tag_name").getAsString();

            }
            catch (IOException ex)
            {
                version = getDescription().getVersion();
            }

            Coins.update = version;

            if (!getDescription().getVersion().equals(version))
            {
                getLogger().warning(Messages.CONSIDER_UPDATING.format(version));
                getLogger().warning("https://www.spigotmc.org/resources/coins.33382/");
            }
        });
    }


    private void registerEvents (Listener... listeners)
    {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(isUsingPaper()? new PaperEvents() : new BukkitEvents(), this);

        for (Listener listener : listeners)
            manager.registerEvents(listener, this);
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
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskLater(this, ticks);
    }

    public void async (Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskAsynchronously(this);
    }

    public void sync (Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTask(this);
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
