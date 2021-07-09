package me.justeli.coins.events;

import org.bukkit.event.Listener;

/**
 * Created by Eli on June 02, 2020.
 * Coins: me.justeli.coins.events
 */
public class LootChests
        implements Listener
{/*
    @EventHandler
    public void onintercat (PlayerInteractEvent e)
    {/*
        LootTable table = e.getInventory().getLootTable();
        table.populateLoot()
        e.getInventory().setLootTable(table);

        e.getInventory().getLootTable().


        if (e.getClickedBlock() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST)
            return;

        World nmsWorld = ((CraftWorld) e.getClickedBlock().getWorld()).getHandle();
        TileEntityLootable te = (TileEntityLootable) nmsWorld
                .getTileEntity(new BlockPosition(e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()));

        // LootChest
        if (te != null && te.b() != null)
        {
            for (int i = 0; i < 27; i++)
            {
                if (te.getItem(i).isEmpty() && localRandom.nextDouble() < 0.06)
                {
                    int second = Config.get(Config.DOUBLE.MONEY_AMOUNT__FROM).intValue()*2;
                    int first = Config.get(Config.DOUBLE.MONEY_AMOUNT__TO).intValue()*2 + 1 - second;

                    int amount = (int)(localRandom.nextDouble() * first + second);
                    ItemStack coin = new OldCoin().stack(true).item();
                    coin.setAmount(amount);

                    te.setItem(i, CraftItemStack.asNMSCopy(coin));
                }
            }
        }

    }*/
}
