package org.windy.blockplacecancel; // 包声明

import com.SelfHome.Main; // 导入Main类
import com.SelfHome.Variable; // 导入Variable类
import com.Util.Home; // 导入Home类
import com.Util.HomeAPI; // 导入HomeAPI类
import com.Util.Util; // 导入Util类
import org.bukkit.Bukkit; // 导入Bukkit类
import org.bukkit.Material; // 导入Material类
import org.bukkit.block.Block; // 导入Block类
import org.bukkit.block.BlockState; // 导入BlockState类
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player; // 导入Player类
import org.bukkit.event.EventHandler; // 导入EventHandler类
import org.bukkit.event.Listener; // 导入Listener类
import org.bukkit.event.block.BlockPlaceEvent; // 导入BlockPlaceEvent类
import org.bukkit.event.player.PlayerJoinEvent; // 导入PlayerJoinEvent类
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin; // 导入JavaPlugin类

import javax.print.DocFlavor;
import java.io.IOException; // 导入IOException类
import java.util.List; // 导入List类



public class BlockPlaceCancel extends JavaPlugin implements Listener { // 定义类BlockPlaceCancel，继承自JavaPlugin并实现Listener接口
    @Override
    public void onEnable() { // 覆盖父类的onEnable方法
        this.saveDefaultConfig(); // 保存默认配置
        Bukkit.getPluginManager().registerEvents(this, this); // 注册事件监听器
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws IOException { // 注解方法，处理玩家加入事件
        HomeAPI homeapi = new HomeAPI(); // 创建HomeAPI对象
        Home home = homeapi.getHome(event.getPlayer().getName()); // 获取玩家的家园信息

        for(Home temp:homeapi.getHomes()) { // 遍历家园列表
            System.out.println(temp); // 打印家园信息
        }
        home.setLevel(9); // 设置家园等级为9
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //假玩家检测
        if (event.getPlayer().getName().toUpperCase().contains("AS-FAKEPLAYER") ||
                event.getPlayer().getName().toUpperCase().contains("[MINECRAFT]") ||
                event.getPlayer().getName().toUpperCase().contains("[MEKANISM]") ||
                event.getPlayer().getName().toUpperCase().contains("[CREATE]") ||
                event.getPlayer().getName().toUpperCase().contains("[DEPLOYER]") ||
                event.getPlayer().getName().toUpperCase().contains("[IF]")) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        boolean sh = this.getConfig().getBoolean("SelfHomeMain.enable");
        if (sh) {
            Configuration config = getConfig();
            SelfHome.executeIfSh(event, config);
        }
        // 获取放置的方块
        org.bukkit.block.Block placedBlock = event.getBlockPlaced();
        Material placedType = placedBlock.getType();

        // 检查放置的方块是否是限制方块
        if (placedType.name().startsWith("TCONSTRUCT") || placedType.name().startsWith("CREATE")) {
            // 检查周围是否有另一个限制方块
            if (hasAdjacentBlock(placedBlock, "TCONSTRUCT") && hasAdjacentBlock(placedBlock, "CREATE")) {
                event.setCancelled(true); // 取消事件
                event.getPlayer().sendMessage("不能摆放两种限制方块相邻放置！"); // 发送消息
            }
        }
    }

    // 检查方块周围是否有特定类型的方块
    private boolean hasAdjacentBlock(org.bukkit.block.Block block, String typeName) {
        for (org.bukkit.block.BlockFace face : org.bukkit.block.BlockFace.values()) { // 遍历方块周围的所有方向
            org.bukkit.block.Block adjacentBlock = block.getRelative(face); // 获取相邻的方块
            if (adjacentBlock.getType().name().startsWith(typeName)) { // 如果相邻方块的类型以typeName开头
                return true; // 返回true
            }
        }
        return false; // 返回false
    }
}
