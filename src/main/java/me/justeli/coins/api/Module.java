package me.justeli.coins.api;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import community.leaf.textchain.bukkit.BukkitTextChainSource;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.function.Function;

/**
 * Created by Eli on June 28, 2021.
 * Coins: me.justeli.coins
 */
public abstract class Module
        extends JavaPlugin
        implements BukkitTaskSource, BukkitTextChainSource
{
    private @NullOr BukkitAudiences audiences;
    protected PluginManager pluginManager;
    protected BukkitCommandManager<CommandSender> commandManager;

    public abstract void enable ();
    public abstract void disable ();

    @Override
    public void onEnable ()
    {
        PaperLib.suggestPaper(this);

        this.audiences = BukkitAudiences.create(this);
        this.pluginManager = getServer().getPluginManager();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();

        try
        {
            this.commandManager = new PaperCommandManager<>(this, executionCoordinatorFunction, mapperFunction, mapperFunction);
        }
        catch (final Exception exception)
        {
            this.getLogger().severe("Failed to initialize the command manager.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try
        {
            if (commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER))
            {
                commandManager.registerBrigadier();
            }
        }
        catch (Exception ignored) {}

        if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
        {
            ((PaperCommandManager<CommandSender>) this.commandManager).registerAsynchronousCompletions();
        }

        enable();
    }

    @Override
    public void onDisable ()
    {
        disable();
        if (this.audiences != null)
        {
            this.audiences.close();
            this.audiences = null;
        }
    }

    public void registerEvents (Listener... listeners)
    {
        for (Listener listener : listeners)
        {
            pluginManager.registerEvents(listener, this);
        }
    }

    @Override
    public BukkitAudiences adventure ()
    {
        if (this.audiences != null) { return this.audiences; }
        throw new IllegalStateException("Audiences not initialized (plugin is disabled).");
    }

    @Override
    public Plugin plugin()
    {
        return this;
    }

    public BukkitCommandManager<CommandSender> commandManager ()
    {
        return commandManager;
    }
}
