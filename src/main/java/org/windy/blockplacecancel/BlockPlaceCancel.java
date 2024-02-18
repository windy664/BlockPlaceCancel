package org.windy.blockplacecancel; // 包声明

import com.SelfHome.Main;
import com.Util.Home;
import com.Util.HomeAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import java.io.IOException;



public class BlockPlaceCancel extends JavaPlugin implements Listener {
    private boolean enablesh; // 定义类BlockPlaceCancel，继承自JavaPlugin并实现Listener接口

    @Override
    public void onEnable() { // 覆盖父类的onEnable方法
        this.saveDefaultConfig(); // 保存默认配置
        Bukkit.getPluginManager().registerEvents(this, this);
        String version = this.getDescription().getVersion();
        String serverName = this.getServer().getName();
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
        this.getServer().getConsoleSender().sendMessage("§a" + version + " §e " + serverName + "\n");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getServer().getConsoleSender().sendMessage("检测到PlaceholderAPI，已兼容！");
        }
        if (Bukkit.getPluginManager().getPlugin("SelfHomeMain") != null /*&& !Main.JavaPlugin.getConfig().getBoolean("CustomTileMax")*/) {
            this.getServer().getConsoleSender().sendMessage("检测到SelfHomeMain，已兼容！");
            if(this.getConfig().getBoolean("SelfHomeMain.enable")){
                this.getServer().getConsoleSender().sendMessage("sh联动已启动，");
            }
            if(this.getConfig().getBoolean("SelfHomeMain.CheckA")) {
                this.getServer().getConsoleSender().sendMessage("精准检测已开启，");
                this.getServer().getConsoleSender().sendMessage(this.getConfig().getString("SelfHomeMain.TileListA"));
            }
        }else{
            enablesh = false;
        }
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
        Player player = event.getPlayer();
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
        if (sh && !enablesh) {
            Configuration config = getConfig();
            SelfHome.executeIfSh(event, config);
        }
        // 获取放置的方块
        org.bukkit.block.Block placedBlock = event.getBlockPlaced();
        Material placedType = placedBlock.getType();
        String A = this.getConfig().getString("Overall-Situation.A");
        String B = this.getConfig().getString("Overall-Situation.B");
        // 检查放置的方块是否是限制方块
        if (B != null && A != null && (placedType.name().startsWith(A) || placedType.name().startsWith(B))) {
            // 检查周围是否有另一个限制方块
            if (hasAdjacentBlock(placedBlock, "TCONSTRUCT") && hasAdjacentBlock(placedBlock, "CREATE")) {
                event.setCancelled(true); // 取消事件
                String tip = this.getConfig().getString("Overall-Situation.tip");
                String passtip = PlaceholderAPI.setPlaceholders(player, tip);
                event.getPlayer().sendMessage(passtip); // 发送消息
            }
        }
    }

    // 检查方块周围是否有特定类型的方块
    private boolean hasAdjacentBlock(org.bukkit.block.Block block, String typeName) {
        for (org.bukkit.block.BlockFace face : org.bukkit.block.BlockFace.values()) { // 遍历方块周围的所有方向
            org.bukkit.block.Block adjacentBlock = block.getRelative(face); // 获取相邻的方块
            if (adjacentBlock.getType().name().startsWith(typeName)) { // 如果相邻方块的类型以typeName开头
                return true;
            }
        }
        return false;
    }
}
