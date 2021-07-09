package me.justeli.coins.economy;

import me.justeli.coins.Coins;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Eli on May 17, 2020.
 * Coins: me.justeli.coins.economy
 */
public class CoinStorage
{
    private final Coins instance;

    public CoinStorage (Coins instance)
    {
        this.instance = instance;
    }

    private final HashMap<UUID, FileConfiguration> playerData = new HashMap<>();
    private final AtomicReference<FileConfiguration> serverConfig = new AtomicReference<>();

    public void initPlayerData ()
    {
        File data = getPluginFile(instance, "data");
        if (!data.exists() && data.mkdir())
            System.out.println("Created data folder for Coins.");

        File folder = getPluginFile(instance, "data/players");
        if (!folder.exists() && folder.mkdir())
            System.out.println("Created player data folder for Coins.");

        initServerData();

        File[] files = folder.listFiles();
        if (files == null)
            return;

        for (File uuidFile : files)
        {
            UUID uuid = UUID.fromString(uuidFile.getName().replace(".yml", ""));
            playerData.put(uuid, getFileConfiguration(instance, String.format("data/players/%s.yml", uuid.toString())));
        }
    }

    public void initServerData ()
    {
        File data = getPluginFile(instance, "data/server.yml");

        try
        {
            if (!data.exists() && data.createNewFile())
                System.out.println("Created server data file for Coins.");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        serverConfig.set(getFileConfiguration(instance, "data/server.yml"));
    }

    public FileConfiguration getCachedServerData ()
    {
        return serverConfig.get();
    }

    public void setServerData (String key, Object value)
    {
        FileConfiguration config = getCachedServerData();
        config.set(key, value);

        serverConfig.set(config);
        saveFile(config, getPluginFile(instance, "data/server.yml"));
    }

    FileConfiguration getStorage (UUID uuid)
    {
        return uuid == null? null : playerData.get(uuid);
    }

    boolean hasData (UUID uuid)
    {
        return playerData.containsKey(uuid);
    }

    boolean createFile (UUID uuid)
    {
        try
        {
            if (getPluginFile(instance, String.format("data/players/%s.yml", uuid)).createNewFile())
            {
                playerData.put(uuid, getFileConfiguration(instance, String.format("data/players/%s.yml", uuid)));
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    void setStorage (UUID uuid, String key, Object value)
    {
        FileConfiguration config = playerData.get(uuid);
        config.set(key, value);

        playerData.put(uuid, config);
        saveFile(config, getPluginFile(instance, String.format("data/players/%s.yml", uuid)));
    }

    private FileConfiguration getFileConfiguration (JavaPlugin instance, String path)
    {
        return YamlConfiguration.loadConfiguration(getPluginFile(instance, path.endsWith(".yml")? path : path + ".yml"));
    }

    private File getPluginFile (JavaPlugin instance, String path)
    {
        return new File(instance.getDataFolder().getAbsolutePath() + File.separator + path.replace("/", File.separator));
    }

    // Saving off of the main thread, in a queue.
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    static
    {
        // Set the name of our single thread.
        EXECUTOR_SERVICE.submit(() -> Thread.currentThread().setName("coins-storage"));
    }

    public void saveFile (FileConfiguration data, File file)
    {
        Path path = file.toPath();
        String stringData = data.saveToString();

        EXECUTOR_SERVICE.submit(() ->
        {
            try
            {
                // Create the file if it doesn't exist...
                if (!Files.isRegularFile(path))
                {
                    Files.createDirectories(path.toAbsolutePath().getParent());
                    Files.createFile(path);
                }

                // Write to the file
                Files.write(path, stringData.getBytes(StandardCharsets.UTF_8));
            }
            catch (Exception e) { e.printStackTrace(); }
        });
    }
}
