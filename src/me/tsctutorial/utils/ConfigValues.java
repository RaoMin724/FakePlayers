/*     */ package me.tsctutorial.utils;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.util.List;
/*     */ import me.tsctutorial.fakeplayers.FakePlayersMain;
import net.badbird5907.anticombatlog.object.NotifyType;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.plugin.java.JavaPlugin;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ConfigValues
/*     */ {
/*     */   public static int getCombatLogSeconds() {
/*  16 */     return combatLogSeconds;
/*  17 */   } private static int combatLogSeconds = 15; public static int getCombatTagSeconds() {
/*  18 */     return combatTagSeconds;
/*  19 */   } private static int combatTagSeconds = 15; public static int getCombatLoggedMessageRadius() {
/*  20 */     return combatLoggedMessageRadius;
/*  21 */   } private static int combatLoggedMessageRadius = -1; public static int getNpcHitResetSecond() {
/*  22 */     return npcHitResetSecond;
/*  23 */   } private static int npcHitResetSecond = 35; public static boolean isNpcCombatLog() {
/*  24 */     return npcCombatLog;
/*     */   } private static boolean npcCombatLog = true; public static String getCombatTaggedMessage() {
/*  26 */     return combatTaggedMessage;
/*  27 */   } private static String combatTaggedMessage = null; public static String getLogInAfterKillMessage() {
/*  28 */     return logInAfterKillMessage;
/*  29 */   } private static String logInAfterKillMessage = null; public static String getUnCombatTaggedMessage() {
/*  30 */     return unCombatTaggedMessage;
/*  31 */   } private static String unCombatTaggedMessage = null; public static String getCombatLoggedMessage() {
/*  32 */     return combatLoggedMessage;
/*  33 */   } private static String combatLoggedMessage = null; public static String getActionBarMessage() {
/*  34 */     return actionBarMessage;
/*  35 */   } private static String actionBarMessage = null; public static String getCombatExpiredMessage() {
/*  36 */     return combatExpiredMessage;
/*  37 */   } private static String combatExpiredMessage = null; public static String getKillMessage() {
/*  38 */     return killMessage;
/*  39 */   } private static String killMessage = null; public static NotifyType getNotifyType() {
/*  40 */     return notifyType;
/*  41 */   } private static NotifyType notifyType = NotifyType.BOTH;
/*     */   public static List<String> getBlockedCommands() {
/*  43 */     return blockedCommands;
/*  44 */   } private static List<String> blockedCommands = null; public static String getBlockedCommandMessage() {
/*  45 */     return blockedCommandMessage;
/*  46 */   } private static String blockedCommandMessage = null;
/*     */   
/*  48 */   public static boolean isSetDeathMessage() { return setDeathMessage; } public static boolean isEnableHologram() { return enableHologram; } public static boolean isBlockedCommandsRegex() { return blockedCommandsRegex; } public static boolean isDisableFly() { return disableFly; } public static boolean isTagOnPearl() { return tagOnPearl; } public static boolean isEnableBlockedCommands() { return enableBlockedCommands; }
/*     */   
/*     */   private static boolean setDeathMessage = true, enableHologram = true, blockedCommandsRegex = true; private static boolean disableFly = false;
/*     */   public static List<String> getExemptWorlds() {
/*  52 */     return exemptWorlds;
/*     */   }
/*     */   private static boolean tagOnPearl = false; private static boolean enableBlockedCommands = true; private static List<String> exemptWorlds;
/*     */   private static FileConfiguration getConfig() {
/*  56 */     return FakePlayersMain.getInstance().getConfig();
/*     */   }
/*     */   
/*     */   private static void saveReload() {
/*  60 */     FakePlayersMain.getInstance().saveConfig();
/*  61 */     FakePlayersMain.getInstance().reloadConfig();
/*     */   }
/*     */   
/*     */   public static void load() {
/*  65 */     combatLogSeconds = getConfig().getInt("combat-log-seconds");
/*  66 */     npcCombatLog = getConfig().getBoolean("npc-combat-log");
/*  67 */     combatTaggedMessage = StringUtils.format(getConfig().getString("messages.combat-tagged", "&cYou have been combat tagged. If you log out right now, you will not be safe. Your combat tag expires in &e%1"), new String[0]);
/*  68 */     unCombatTaggedMessage = StringUtils.format(getConfig().getString("messages.un-combat-tagged"), new String[0]);
/*  69 */     combatLoggedMessageRadius = getConfig().getInt("combat-logged-message-radius");
/*  70 */     combatLoggedMessage = StringUtils.format(getConfig().getString("messages.logged-out-combat"), new String[0]);
/*  71 */     combatTagSeconds = getConfig().getInt("combat-tag-seconds");
/*  72 */     notifyType = NotifyType.valueOf(getConfig().getString("notify-type").toUpperCase());
/*  73 */     actionBarMessage = StringUtils.format(getConfig().getString("messages.action-bar-message"), new String[0]);
/*  74 */     npcHitResetSecond = getConfig().getInt("npc-hit-reset-seconds");
/*  75 */     killMessage = StringUtils.format(getConfig().getString("messages.kill-message"), new String[0]);
/*  76 */     logInAfterKillMessage = StringUtils.format(getConfig().getString("messages.log-in-after-kill", "&cYou logged out while in combat and was killed"), new String[0]);
/*  77 */     combatExpiredMessage = StringUtils.format(getConfig().getString("messages.combat-expired"), new String[0]);
/*  78 */     blockedCommands = getConfig().getStringList("blocked-commands.blocked");
/*  79 */     enableBlockedCommands = getConfig().getBoolean("blocked-commands.enabled");
/*  80 */     blockedCommandsRegex = getConfig().getBoolean("blocked-commands.regex");
/*  81 */     blockedCommandMessage = StringUtils.format(getConfig().getString("messages.blocked-command", "&cYou cannot use this command while in combat."), new String[0]);
/*  82 */     enableHologram = getConfig().getBoolean("enable-hologram", true);
/*  83 */     setDeathMessage = getConfig().getBoolean("set-death-message", true);
/*  84 */     exemptWorlds = getConfig().getStringList("exempt-worlds");
/*  85 */     disableFly = getConfig().getBoolean("disable.fly", false);
/*  86 */     tagOnPearl = getConfig().getBoolean("tag-on-enderpearl", true);
/*     */   }
/*     */   
/*     */   public static void reload() {
/*  90 */     FakePlayersMain.getInstance().setConfig(null);
/*  91 */     load();
/*     */   }
/*     */   
/*     */   public static boolean scoreboardEnabled() {
/*  95 */     return (notifyType == NotifyType.BOTH || notifyType == NotifyType.BOARD);
/*     */   }
/*     */   
/*     */   public static void enable(JavaPlugin plugin) {
/*  99 */     if (!(new File(plugin.getDataFolder() + "/config.yml")).exists()) {
/* 100 */       plugin.saveDefaultConfig();
/*     */     }
/* 102 */     load();
/*     */   }
/*     */   
/*     */   public static boolean actionBarEnabled() {
/* 106 */     return (notifyType == NotifyType.BOTH || notifyType == NotifyType.ACTIONBAR);
/*     */   }
/*     */ }


/* Location:              D:\Desktop\AntiCombatLog-2.6.0.jar!\net\badbird5907\anticombatlo\\utils\ConfigValues.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */