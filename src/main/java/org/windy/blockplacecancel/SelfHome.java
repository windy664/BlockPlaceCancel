package org.windy.blockplacecancel;

import com.SelfHome.Variable;
import com.Util.Home;
import com.Util.HomeAPI;
import com.Util.Util;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

import java.util.logging.Logger;

public class SelfHome {
    private static final Logger LOGGER = Logger.getLogger(SelfHome.class.getName());

    public static void executeIfSh(BlockPlaceEvent event, Configuration config) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String worldName = block.getWorld().getName().replace(Variable.world_prefix, "");

        // 获取配置文件中的 TileList.chunk 和 TileList.world 配置项
        List<String> tileListChunk = config.getStringList("SelfHomeMain.TileList.chunk");
        List<String> tileListWorld = config.getStringList("SelfHomeMain.TileList.world");

        // 检查 TileList.chunk 中的配置
        if (isBlocked(block, tileListChunk, "chunk")) {
            event.setCancelled(true);
            player.sendMessage(Variable.Lang_YML.getString("PlaceReachMaxTile"));
            LOGGER.info("Player " + player.getName() + " attempted to place a block, but the maximum limit was reached in the chunk.");
            return;
        }

        // 检查 TileList.world 中的配置
        Home home = HomeAPI.getHome(worldName);
        if (home != null && isBlocked(block, tileListWorld, "world")) {
            event.setCancelled(true);
            player.sendMessage(Variable.Lang_YML.getString("PlaceReachMaxTile"));
            LOGGER.info("Player " + player.getName() + " attempted to place a block, but the maximum limit was reached in the world.");
        }
    }

    /**
     * 检查玩家是否被阻止在特定方块上放置更多方块
     * @param block 放置的方块
     * @param tileList 方块配置列表
     * @param type 方块类型
     * @return 如果放置的方块数量超过了配置的最大值，则返回 true；否则返回 false
     */
    private static boolean isBlocked(Block block, List<String> tileList, String type) {
        // 获取放置方块的 NBT 数据
        String nbt = Util.getNBTString(block.getState());
        // 遍历方块配置列表
        for (String tile : tileList) {
            // 分割配置项
            String[] temp = tile.split("\\|");
            // 检查配置项格式是否正确
            if (temp.length >= 2 && nbt.equalsIgnoreCase(temp[0])) {
                // 获取配置的最大放置数量
                int maxAmount = Integer.parseInt(temp[1]);
                // 获取当前放置的方块数量
                int currentAmount = countPlacedBlocks(block, type, temp[0]);
                // 如果当前数量超过了最大数量，则返回 true
                if (currentAmount >= maxAmount) {
                    return true;
                }
            }
        }
        // 如果未超过最大数量，则返回 false
        return false;
    }

    /**
     * 计算玩家在特定方块周围已经放置的方块数量
     * @param block 放置的方块
     * @param type 方块类型（"chunk" 或 "world"）
     * @param nbt 方块的 NBT 数据
     * @return 放置的方块数量
     */
    private static int countPlacedBlocks(Block block, String type, String nbt) {
        // 初始化放置方块数量为 0
        int count = 0;
        // 如果方块类型为 "chunk"
        if (type.equalsIgnoreCase("chunk")) {
            // 遍历方块所在区块的所有方块状态
            for (BlockState state : block.getChunk().getTileEntities()) {
                // 检查方块状态的 NBT 数据是否包含指定的 NBT 数据
                if (Util.getNBTString(state).toUpperCase().contains(nbt.toUpperCase())) {
                    // 如果包含，则计数加一
                    count++;
                }
            }
        }
        // 如果方块类型为 "world"
        else if (type.equalsIgnoreCase("world")) {
            // 获取方块所在世界的家园信息
            Home home = HomeAPI.getHome(block.getWorld().getName().replace(Variable.world_prefix, ""));
            // 如果家园信息不为空
            if (home != null) {
                // 遍历家园的限制方块列表
                for (String str : home.getLimitBlock()) {
                    // 分割限制方块信息
                    String[] args = str.split("\\|");
                    // 检查限制方块信息是否符合条件，并计数加一
                    if (args.length >= 2 && args[0].equalsIgnoreCase("chunk") && nbt.toUpperCase().contains(args[1].toUpperCase())) {
                        count++;
                    }
                }
            }
        }
        // 返回放置方块数量
        return count;
    }
}
