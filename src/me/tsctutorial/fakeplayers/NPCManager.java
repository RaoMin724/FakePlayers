package me.tsctutorial.fakeplayers;



import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.effect.Effect;
import me.tsctutorial.object.Triplet;
import me.tsctutorial.utils.ConfigValues;
import me.tsctutorial.utils.StringUtils;
import net.citizensnpcs.api.CitizensAPI;
import me.tsctutorial.object.CombatNPCTrait;

import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.ai.Navigator;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class NPCManager {

public static Map<UUID, Triplet<Integer, NPC, String>> getNpcs() {
    return npcs;
}

private static final Map<UUID, Triplet<Integer, NPC, String>> npcs = new ConcurrentHashMap<>();

    public static boolean isSpawned(UUID player) {
        return npcs.containsKey(player);
    }

    public void createNPC(Player player) {
        // 检查是否已经创建了具有特定 UUID 的 NPC
        if (npcs.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "您已經建立了一個 NPC！");
            return;
        }

        NPC npc = getNPCRegistry().createNPC(EntityType.PLAYER, "&cDISCONNECTED: &7"+player.getDisplayName());




        Navigator navigator = npc.getNavigator();
        navigator.cancelNavigation(); // 取消 NPC 当前的导航

        // 获取NPC的背包
        Inventory npcInventory = ((Inventory) npc.getOrAddTrait(Inventory.class));

        // 获取玩家的装备
        ItemStack boots = player.getInventory().getBoots();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack helmet = player.getInventory().getHelmet();

        // 获取玩家背包物品
        ItemStack[] playerInventoryContents = player.getInventory().getContents();

        // 创建一个列表来存放所有物品（包括装备和背包物品）
        List<ItemStack> allItems = new ArrayList<>();
        allItems.add(boots);
        allItems.add(leggings);
        allItems.add(chestplate);
        allItems.add(helmet);
        allItems.addAll(Arrays.asList(playerInventoryContents));

        // 将所有物品放入NPC的背包
        npcInventory.setContents(allItems.toArray(new ItemStack[0]));

        // 添加CombatNPCTrait
        npc.addTrait(new CombatNPCTrait(player.getName(), 0, player.getUniqueId(), allItems, player.getHealth()));
        ((CombatNPCTrait) npc.getTraitNullable(CombatNPCTrait.class)).setIndefinite(true);





        int i = 1;
        npcs.put(player.getUniqueId(), new Triplet(Integer.valueOf(i), npc, player.getName()));
        npc.spawn(player.getLocation());
    }


    public static NPCRegistry getNPCRegistry() {
        if (CitizensAPI.getNamedNPCRegistry("AntiCombatLog") == null)
            CitizensAPI.createNamedNPCRegistry("AntiCombatLog", (NPCDataStore) new MemoryNPCDataStore());
        return CitizensAPI.getNamedNPCRegistry("AntiCombatLog");
    }

    public static void despawn(UUID player) {
        if (isSpawned(player)) {
            NPC npc = (NPC) ((Triplet) npcs.get(player)).getValue1();
            npcs.remove(player);
            if (npc.isSpawned()) {
                npc.despawn();
            }
            npc.destroy();
        }
    }   

}


