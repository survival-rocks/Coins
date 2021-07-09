package me.justeli.coins.api;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import me.justeli.coins.Coins;
import org.bukkit.command.CommandSender;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Eli on June 28, 2021.
 * Coins: me.justeli.coins.api
 */
public class PrepareCommand
{
    private final Command.Builder<CommandSender> builder;
    private final Coins plugin;

    public PrepareCommand (Coins plugin, String configCommand)
    {
        this.plugin = plugin;

        List<String> list = plugin.getConfig().getStringList("commands." + configCommand);
        if (list.size() == 0)
        {
            this.builder = plugin.commandManager().commandBuilder(configCommand.toLowerCase(Locale.ROOT));
            return;
        }

        if (list.size() == 1)
        {
            this.builder = plugin.commandManager().commandBuilder(list.get(0));
            return;
        }

        LinkedList<String> commands = new LinkedList<>(list);

        String primary = commands.getFirst();
        commands.removeFirst();

        String[] aliases = commands.toArray(new String[0]);

        this.builder = plugin.commandManager().commandBuilder(primary, aliases);
    }

    public Command.Builder<CommandSender> command ()
    {
        return builder;
    }

    public BukkitCommandManager<CommandSender> manager ()
    {
        return plugin.commandManager();
    }
}
