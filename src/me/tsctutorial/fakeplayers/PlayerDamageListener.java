package me.tsctutorial.fakeplayers;

import me.tsctutorial.object.CombatNPCTrait;
import me.tsctutorial.object.Triplet;
import me.tsctutorial.utils.ConfigValues;
import me.tsctutorial.utils.ExcellentEnchantUtil;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static me.tsctutorial.fakeplayers.NPCManager.*;

public class PlayerDamageListener implements Listener {


    private final Map<UUID, Long> damagedPlayers = new HashMap<>();

    private final FakePlayersMain plugin = FakePlayersMain.getInstance();


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
//        System.out.println("onPlayerQuit!!!!");
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (event.getQuitMessage().equals("您已成功安全登出遊戲")){
            return;
        }

//        System.out.println(isSpawned(playerId));
        if (!isSpawned(playerId)) {
            // 检查玩家附近是否有其他玩家
            boolean nearbyPlayerExists = false;
            for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                if (entity instanceof Player && !entity.getUniqueId().equals(playerId)) {
                    nearbyPlayerExists = true;
                    break;
                }
            }

            if (nearbyPlayerExists) {
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                plugin.npcManager.createNPC(player);
            }
            new Thread(() -> {
                try {
                    Thread.sleep(15000); // 等待 15 秒
                    // 檢查玩家是否仍在 HashMap 中
//                    System.out.println("Check NPC 15 second!!!");

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        despawn(playerId);
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }


    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        despawn(playerId);

        if(FakePlayersMain.getToKillOnLogin().containsKey(playerId)){
            player.getInventory().clear(); // 清除玩家背包
            // 清除玩家穿戴的裝備
            player.getInventory().setBoots(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setHelmet(null);

            player.setHealth(0); // 殺死玩家
            FakePlayersMain.getToKillOnLogin().remove(playerId);
        }

    }

    @EventHandler
    public void onNpcDeath(NPCDeathEvent event) {
//        System.out.println("onNpcDeath!!!");
        NPC npc = event.getNPC();
        npc.getName();

        if (npc != null) {

            CombatNPCTrait trait = (CombatNPCTrait) npc.getTraitNullable(CombatNPCTrait.class);
            if (trait != null) {
                String killer;

                event.getDrops().addAll(trait.getItems());

                if (event.getEvent().getEntity().getKiller() == null) {
                    killer = "null";
                } else {
                    killer = event.getEvent().getEntity().getKiller().getName();
                }
                FakePlayersMain.getToKillOnLogin().put(trait.getUuid(), killer);
                FakePlayersMain.saveData();
            }

        } else {
            Bukkit.getLogger().warning("NPC was null in NPCDeathEvent!");
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        FPCommand.onPlayerMove();
    }

}