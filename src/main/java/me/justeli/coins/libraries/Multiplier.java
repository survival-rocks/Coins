package me.justeli.coins.libraries;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eli on 6 jan. 2020.
 * spigotPlugins: me.justeli.coins.libraries
 */
public class Multiplier
{
    private final HashMap<UUID, Double> multiplier = new HashMap<>();

    public void resetMultiplier ()
    {
        multiplier.clear();
    }

    public double getMultiplier (Player player)
    {
        if (!multiplier.containsKey(player.getUniqueId()))
        {
            List<Double> permissions = new ArrayList<>();
            for (PermissionAttachmentInfo permissionInfo : player.getEffectivePermissions())
            {
                String permission = permissionInfo.getPermission();
                if (permission.startsWith("coins.multiplier."))
                {
                    String number = permission.replace("coins.multiplier.", "");
                    permissions.add(Double.parseDouble(number));
                }
            }
            multiplier.put(player.getUniqueId(), permissions.size() == 0? 1d : Collections.max(permissions));
        }
        return multiplier.get(player.getUniqueId());
    }
}
