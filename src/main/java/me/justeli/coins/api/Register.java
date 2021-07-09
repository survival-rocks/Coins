package me.justeli.coins.api;

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
import me.justeli.coins.Coins;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Eli on May 2, 2021.
 * Coins: me.justeli.coins.api
 */
public class Register
{
    private final Coins instance;

    public Register (Coins instance)
    {
        this.instance = instance;
        setupCommandManager();
    }

    private BukkitCommandManager<CommandSender> commandManager;

    public BukkitCommandManager<CommandSender> getCommandManager ()
    {
        return commandManager;
    }

    private AnnotationParser<CommandSender> annotationParser;

    public void events (Listener... listeners)
    {
        PluginManager manager = instance.getServer().getPluginManager();
        for (Listener listener : listeners)
        {
            manager.registerEvents(listener, instance);
        }
    }

    public void commands (Object... classes)
    {
        for (Object instance : classes)
        {
            annotationParser.parse(instance);
        }
    }

    public void metrics (final int pluginId, final Consumer<Metric> consumer)
    {
        instance.async().delay(1, TimeUnit.MINUTES).run(() ->
        {
            Metrics metrics = new Metrics(instance, pluginId);
            consumer.accept(new Metric(metrics));
        });
    }

    public static class Metric
    {
        private final Metrics metrics;

        public Metric (Metrics metrics)
        {
            this.metrics = metrics;
        }

        public void add (String key, Object value)
        {
            metrics.addCustomChart(new SimplePie(key, value::toString));
        }
    }

    private void setupCommandManager ()
    {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try
        {
            this.commandManager = new PaperCommandManager<>(instance, executionCoordinatorFunction, mapperFunction, mapperFunction);
        }
        catch (final Exception e)
        {
            instance.getLogger().severe("Failed to initialize the command manager.");
            instance.getServer().getPluginManager().disablePlugin(instance);
            return;
        }

        if (commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER))
            commandManager.registerBrigadier();

        if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
            ((PaperCommandManager<CommandSender>) this.commandManager).registerAsynchronousCompletions();

        final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> BukkitCommandMetaBuilder.builder()
                .withDescription(p.get(StandardParameters.DESCRIPTION, "No description")).build();

        this.annotationParser = new AnnotationParser<>(this.commandManager, CommandSender.class, commandMetaFunction);
    }
}