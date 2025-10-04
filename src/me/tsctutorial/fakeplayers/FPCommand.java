package me.tsctutorial.fakeplayers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class FPCommand implements CommandExecutor {

    private static BukkitRunnable countdownTask;
    static Player player;

    private final FakePlayersMain plugin = FakePlayersMain.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {



        if (sender instanceof Player) {
            player = (Player) sender;
            UUID playerId = player.getUniqueId();

            if (cmd.getName().equalsIgnoreCase("logout")) {
                // 檢查玩家附近是否有其他玩家
                boolean nearbyPlayerExists = false;
                for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                    if (entity instanceof Player && !entity.getUniqueId().equals(playerId)) {
                        nearbyPlayerExists = true;
                        break;
                    }
                }

                if (nearbyPlayerExists) {
                    int countdownSeconds = 10;
                    countdownTask = new BukkitRunnable() {
                        int timeLeft = countdownSeconds;

                        @Override
                        public void run() {
                            if (timeLeft > 0) {
                                player.sendMessage(ChatColor.GRAY + "將在 " + ChatColor.RED  + timeLeft + ChatColor.GRAY +  " 秒後安全登出遊戲.");
                                timeLeft--;
                            } else {
                                player.kickPlayer(ChatColor.GREEN + "您已成功安全登出遊戲.");
                                cancel(); 
                            }
                        }
                    };
                    countdownTask.runTaskTimer(plugin, 20L, 20L);


                }else {
                    player.kickPlayer(ChatColor.GREEN + "您已成功安全登出遊戲.");
                }
                return true;
            }

        }
        sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行！");
        return true;
    }
    // 监听玩家移动事件
    public static void onPlayerMove() {
        if (countdownTask != null) {
            countdownTask.cancel(); // 取消倒计时任务
            player.sendMessage(ChatColor.RED + "您的移動取消了登出倒數計時。");
        }
    }
}
