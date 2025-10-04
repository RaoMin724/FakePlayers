package me.tsctutorial.fakeplayers;


import me.tsctutorial.utils.ConfigValues;
import me.tsctutorial.utils.StringUtils;

import me.tsctutorial.fakeplayers.NPCManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import org.bukkit.configuration.file.FileConfiguration;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;

public class FakePlayersMain extends JavaPlugin {
    private static FakePlayersMain instance;
    private static Map<UUID, String> toKillOnLogin;

    public NPCManager npcManager;

    private static final List<UUID> killed = new ArrayList<>();
    private static File file = null;

    private FileConfiguration config = null;

    public void onEnable() {
        this.npcManager = new NPCManager();
        setInstance(this);



        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);

        // 初始化 file 变量
        file = new File(getInstance().getDataFolder() + "/data.json");

        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PrintStream ps = null;
            try {
                ps = new PrintStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            ps.print("{}");
            ps.close();
        }
        loadData();

        this.getCommand("logout").setExecutor(new FPCommand());


    }



    public static boolean isCitizensEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Citizens");
    }


    public static Map<UUID, String> getToKillOnLogin() {
        return toKillOnLogin;
    }

    public static List<UUID> getKilled() {
        return killed;
    }

    public static void setToKillOnLogin(Map<UUID, String> toKillOnLogin) {
        FakePlayersMain.toKillOnLogin = toKillOnLogin;
    }

    public static FakePlayersMain getInstance() {
        return instance;
    }

    private void setInstance(FakePlayersMain instance) {
        FakePlayersMain.instance = instance;
    }


    public static void loadData() {
        // 在嘗試讀取文件之前檢查文件物件是否已經初始化
        if (file != null) {
            // 檢查文件是否存在
            if (file.exists()) {
                String json = StringUtils.readFile(file);
                toKillOnLogin = new HashMap<>();
                Map<String, String> a = (Map<String, String>) (new Gson()).fromJson(json, HashMap.class);

                a.forEach((a1, b) -> toKillOnLogin.put(UUID.fromString(a1), b));
                System.out.println(toKillOnLogin);
            } else {
                // 如果檔案不存在，可以列印一條日誌來提示
                System.err.println("Data file does not exist!");
            }
        } else {
            // 如果檔案物件為null，也可以列印一條日誌來提示
            System.err.println("File object is null!");
        }
    }


    public void reloadConfig() {
        super.reloadConfig();
        ConfigValues.reload();
    }


    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/config.yml"));
        }
        return this.config;
    }

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }


    public static void saveData() {
        // 在嘗試保存資料之前檢查文件是否存在
        if (file != null) {
            Bukkit.getScheduler().runTaskAsynchronously((Plugin) getInstance(), () -> {
                String json = (new Gson()).toJson(toKillOnLogin);
                try {
                    PrintStream ps = new PrintStream(file);
                    ps.print(json);
                    ps.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void onDisable() {
        getLogger().info("Disabling AntiCombatLog!");

        String json = (new Gson()).toJson(toKillOnLogin);
        try {
            PrintStream ps = new PrintStream(file);
            ps.print(json);
            ps.close();
        } catch (FileNotFoundException e) {
            System.err.println("Failed to save data.json!");
            e.printStackTrace();
        }
        getLogger().info("Despawning current combat logged NPCs.");
        if (isCitizensEnabled()) {
            NPCManager.getNpcs().forEach((uuid, integerNPCStringTriplet) -> NPCManager.despawn(uuid));
        }

    }


}
