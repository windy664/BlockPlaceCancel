package org.windy.blockplacecancel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockPlaceCancel extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // 获取放置的方块
        org.bukkit.block.Block placedBlock = event.getBlockPlaced();
        Material placedType = placedBlock.getType();

        // 检查放置的方块是否是限制方块
        if (placedType.name().startsWith("TCONSTRUCT") || placedType.name().startsWith("CREATE")) {
            // 检查周围是否有另一个限制方块
            if (hasAdjacentBlock(placedBlock, "TCONSTRUCT") && hasAdjacentBlock(placedBlock, "CREATE")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("不能摆放两种限制方块相邻放置！");
            }
        }
    }

    // 检查方块周围是否有特定类型的方块
    private boolean hasAdjacentBlock(org.bukkit.block.Block block, String typeName) {
        for (org.bukkit.block.BlockFace face : org.bukkit.block.BlockFace.values()) {
            org.bukkit.block.Block adjacentBlock = block.getRelative(face);
            if (adjacentBlock.getType().name().startsWith(typeName)) {
                return true;
            }
        }
        return false;
    }
}
