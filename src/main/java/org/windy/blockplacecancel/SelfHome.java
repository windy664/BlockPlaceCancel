package org.windy.blockplacecancel;

import com.SelfHome.Variable;
import com.Util.Home;
import com.Util.HomeAPI;
import com.Util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SelfHome {

    public static void executeIfSh(BlockPlaceEvent event, Configuration config) {
        Player p = event.getPlayer(); // 获取触发事件的玩家
        // 使用传入的配置对象获取配置信息
        List<String> TileListA = config.getStringList("SelfHomeMain.TileListA");
        List<String> TileListR = config.getStringList("SelfHomeMain.TileListR");
        boolean CheckA = config.getBoolean("SelfHomeMain.CheckA");
        boolean CheckR = config.getBoolean("SelfHomeMain.CheckR");
        boolean EnableClearExtraBlocks = config.getBoolean("SelfHomeMain.EnableClearExtraBlocks");
        if (!Util.CheckIsHome(event.getPlayer().getWorld().getName().replace(Variable.world_prefix, ""))) {
            return; // 如果不是家园世界，则返回
        }
        Block block = event.getBlock(); // 获取放置的方块
        String nbt = Util.getNBTString(block.getState()); // 获取方块的NBT数据字符串
        boolean check_contain = false; // 初始化标记
        String contain_nbt = ""; // 初始化NBT字符串,用于精准检测
        String contain_nbr = ""; //用于模糊检测
        int MaxThisTile = 0; // 初始化最大方块数

        if(CheckA) {
            for (String s : TileListA) { // 遍历TileList列表
                String[] temp = ((String) s).split("\\|"); // 分割字符串
                if (temp[0].equalsIgnoreCase("chunk")) // 如果第一个参数是"chunk"
                {
                    if (nbt.toUpperCase().contains(temp[1].toUpperCase())) { // 如果NBT数据包含指定字符串
                        check_contain = true; // 标记为true
                        contain_nbt = temp[1]; // 获取NBT字符串
                        MaxThisTile = Integer.valueOf(temp[2]).intValue(); // 获取最大方块数
                        break; // 结束循环
                    }
                }
            }
        }else if(CheckR)
        {
            for (String s : TileListR) { // 遍历TileList列表
                String[] temp = ((String) s).split("\\|"); // 分割字符串
                if (temp[0].equalsIgnoreCase("chunk")) // 如果第一个参数是"chunk"
                {
                    if (nbt.toUpperCase().contains(temp[1].toUpperCase())) { // 如果NBT数据包含指定字符串
                        check_contain = true; // 标记为true
                        contain_nbr = temp[1]; // 获取NBT字符串
                        MaxThisTile = Integer.valueOf(temp[2]).intValue(); // 获取最大方块数
                        break; // 结束循环
                    }
                }
            }
        }
        if (!check_contain) { // 如果不包含指定NBT字符串，则返回
            return;
        }
        int NowAmount = 0; // 初始化当前方块数


        boolean extra_perm = false; // 初始化额外权限
        int extra_amount = MaxThisTile; // 初始化额外方块数
        for (int i = 100; i > 0; i--) { // 循环检查权限
            if (p.hasPermission("SelfHome.ChunkPlace." + contain_nbt + "." + i)) { // 如果有权限
                extra_perm = true; // 标记为true
                if (extra_amount < i) {
                    extra_amount = i;
                    break;
                }
            }
        }


        Home home = HomeAPI.getHome(event.getBlock().getWorld().getName().replace(Variable.world_prefix, "")); // 获取家园对象
        for (String str : home.getLimitBlock()) { // 遍历限制方块列表
            String[] args = str.split("\\|"); // 分割字符串
            if (args[0].equalsIgnoreCase("chunk") && nbt.toUpperCase().contains(args[1].toUpperCase())) {
                int amount = Integer.valueOf(args[2]); // 获取方块数
                if (extra_amount < amount) { // 如果额外方块数小于方块数
                    extra_perm = true; // 标记为true
                    extra_amount = amount; // 更新额外方块数
                }
            }
        }


        int b;
        int j;
        BlockState[] arrayOfBlockState;
        for (j = (arrayOfBlockState = event.getBlock().getChunk().getTileEntities()).length, b = 0; b < j; ) {
            BlockState state = arrayOfBlockState[b];
            if (Util.getNBTString(block.getState()).toUpperCase().contains(contain_nbt.toUpperCase())) {//精准检测
                NowAmount++;
            } else if (Util.getNBTString(state).toUpperCase().contains(contain_nbr.toUpperCase())) {//模糊检测
                NowAmount++;
            }

            if (EnableClearExtraBlocks && NowAmount > MaxThisTile) {
                event.setCancelled(true); // 取消事件
                ItemStack itemStack = new ItemStack(block.getType());
                Item item = block.getWorld().dropItemNaturally(block.getLocation(), itemStack); //变成掉落物
                state.getBlock().getLocation().getWorld().getBlockAt(state.getBlock().getLocation()).setType(Material.AIR); // 设置方块为空气
                p.sendMessage(Variable.Lang_YML.getString("ClearExtraBlocks")); // 发送消息
            }
            b++;
        }

        NowAmount--; // 当前方块数减一


        if (extra_perm) { // 如果有额外权限
            MaxThisTile = extra_amount; // 更新最大方块数
        }

        if (NowAmount + 1 <= MaxThisTile) { // 如果当前方块数加一小于等于最大方块数
            String temp = Variable.Lang_YML.getString("PlaceMaxTile"); // 获取消息模板
            if (temp.contains("<Now>")) { // 替换模板中的当前方块数
                temp = temp.replace("<Now>", String.valueOf(NowAmount + 1));
            }
            if (temp.contains("<Max>")) { // 替换模板中的最大方块数
                temp = temp.replace("<Max>", String.valueOf(MaxThisTile));
            }
            if (temp.contains("<NBT>")) { // 替换模板中的NBT字符串
                temp = temp.replace("<NBT>", String.valueOf(contain_nbt));
            }
            p.sendMessage(temp); // 发送消息
        } else {
            String temp = Variable.Lang_YML.getString("PlaceReachMaxTile"); // 获取消息模板
            if (temp.contains("<Now>")) { // 替换模板中的当前方块数
                temp = temp.replace("<Now>", String.valueOf(NowAmount));
            }
            if (temp.contains("<Max>")) { // 替换模板中的最大方块数
                temp = temp.replace("<Max>", String.valueOf(MaxThisTile));
            }
            if (temp.contains("<NBT>")) { // 替换模板中的NBT字符串
                temp = temp.replace("<NBT>", String.valueOf(contain_nbt));
            }
            event.setCancelled(true); // 取消事件
            p.sendMessage(temp); // 发送消息
        }

    }
}
