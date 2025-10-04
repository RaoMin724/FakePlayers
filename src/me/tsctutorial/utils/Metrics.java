/*     */ package net.badbird5907.anticombatlog.utils;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.URL;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.Map;
/*     */ import java.util.Objects;
/*     */ import java.util.Set;
/*     */ import java.util.UUID;
/*     */ import java.util.concurrent.Callable;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.concurrent.ScheduledThreadPoolExecutor;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.function.BiConsumer;
/*     */ import java.util.function.Consumer;
/*     */ import java.util.function.Supplier;
/*     */ import java.util.logging.Level;
/*     */ import java.util.stream.Collectors;
/*     */ import java.util.zip.GZIPOutputStream;
/*     */ import javax.net.ssl.HttpsURLConnection;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.configuration.file.YamlConfiguration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.bukkit.plugin.java.JavaPlugin;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Metrics
/*     */ {
/*     */   private final Plugin plugin;
/*     */   private final MetricsBase metricsBase;
/*     */   
/*     */   public Metrics(JavaPlugin plugin, int serviceId) {
/*  54 */     this.plugin = (Plugin)plugin;
/*     */     
/*  56 */     File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
/*  57 */     File configFile = new File(bStatsFolder, "config.yml");
/*  58 */     YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
/*  59 */     if (!config.isSet("serverUuid")) {
/*  60 */       config.addDefault("enabled", Boolean.valueOf(true));
/*  61 */       config.addDefault("serverUuid", UUID.randomUUID().toString());
/*  62 */       config.addDefault("logFailedRequests", Boolean.valueOf(false));
/*  63 */       config.addDefault("logSentData", Boolean.valueOf(false));
/*  64 */       config.addDefault("logResponseStatusText", Boolean.valueOf(false));
/*     */       
/*  66 */       config
/*  67 */         .options()
/*  68 */         .header("bStats (https://bStats.org) collects some basic information for plugin authors, like how\nmany people use their plugin and their total player count. It's recommended to keep bStats\nenabled, but if you're not comfortable with this, you can turn this setting off. There is no\nperformance penalty associated with having metrics enabled, and data sent to bStats is fully\nanonymous.")
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  74 */         .copyDefaults(true);
/*     */       try {
/*  76 */         config.save(configFile);
/*  77 */       } catch (IOException iOException) {}
/*     */     } 
/*     */ 
/*     */     
/*  81 */     boolean enabled = config.getBoolean("enabled", true);
/*  82 */     String serverUUID = config.getString("serverUuid");
/*  83 */     boolean logErrors = config.getBoolean("logFailedRequests", false);
/*  84 */     boolean logSentData = config.getBoolean("logSentData", false);
/*  85 */     boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  95 */     Objects.requireNonNull(plugin); this.metricsBase = new MetricsBase("bukkit", serverUUID, serviceId, enabled, this::appendPlatformData, this::appendServiceData, submitDataTask -> Bukkit.getScheduler().runTask((Plugin)plugin, submitDataTask), plugin::isEnabled, (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error), message -> this.plugin.getLogger().log(Level.INFO, message), logErrors, logSentData, logResponseStatusText);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void shutdown() {
/* 105 */     this.metricsBase.shutdown();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void addCustomChart(CustomChart chart) {
/* 114 */     this.metricsBase.addCustomChart(chart);
/*     */   }
/*     */   
/*     */   private void appendPlatformData(JsonObjectBuilder builder) {
/* 118 */     builder.appendField("playerAmount", getPlayerAmount());
/* 119 */     builder.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
/* 120 */     builder.appendField("bukkitVersion", Bukkit.getVersion());
/* 121 */     builder.appendField("bukkitName", Bukkit.getName());
/* 122 */     builder.appendField("javaVersion", System.getProperty("java.version"));
/* 123 */     builder.appendField("osName", System.getProperty("os.name"));
/* 124 */     builder.appendField("osArch", System.getProperty("os.arch"));
/* 125 */     builder.appendField("osVersion", System.getProperty("os.version"));
/* 126 */     builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
/*     */   }
/*     */   
/*     */   private void appendServiceData(JsonObjectBuilder builder) {
/* 130 */     builder.appendField("pluginVersion", this.plugin.getDescription().getVersion());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int getPlayerAmount() {
/*     */     try {
/* 138 */       Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers", new Class[0]);
/* 139 */       return onlinePlayersMethod.getReturnType().equals(Collection.class) ? (
/* 140 */         (Collection)onlinePlayersMethod.invoke(Bukkit.getServer(), new Object[0])).size() : (
/* 141 */         (Player[])onlinePlayersMethod.invoke(Bukkit.getServer(), new Object[0])).length;
/* 142 */     } catch (Exception e) {
/*     */       
/* 144 */       return Bukkit.getOnlinePlayers().size();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static class MetricsBase
/*     */   {
/*     */     public static final String METRICS_VERSION = "3.0.2";
/*     */     
/*     */     private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";
/*     */     
/*     */     private final ScheduledExecutorService scheduler;
/*     */     
/*     */     private final String platform;
/*     */     
/*     */     private final String serverUuid;
/*     */     
/*     */     private final int serviceId;
/*     */     
/*     */     private final Consumer<Metrics.JsonObjectBuilder> appendPlatformDataConsumer;
/*     */     
/*     */     private final Consumer<Metrics.JsonObjectBuilder> appendServiceDataConsumer;
/*     */     
/*     */     private final Consumer<Runnable> submitTaskConsumer;
/*     */     
/*     */     private final Supplier<Boolean> checkServiceEnabledSupplier;
/*     */     
/*     */     private final BiConsumer<String, Throwable> errorLogger;
/*     */     
/*     */     private final Consumer<String> infoLogger;
/*     */     
/*     */     private final boolean logErrors;
/*     */     
/*     */     private final boolean logSentData;
/*     */     
/*     */     private final boolean logResponseStatusText;
/*     */     
/* 181 */     private final Set<Metrics.CustomChart> customCharts = new HashSet<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final boolean enabled;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public MetricsBase(String platform, String serverUuid, int serviceId, boolean enabled, Consumer<Metrics.JsonObjectBuilder> appendPlatformDataConsumer, Consumer<Metrics.JsonObjectBuilder> appendServiceDataConsumer, Consumer<Runnable> submitTaskConsumer, Supplier<Boolean> checkServiceEnabledSupplier, BiConsumer<String, Throwable> errorLogger, Consumer<String> infoLogger, boolean logErrors, boolean logSentData, boolean logResponseStatusText) {
/* 220 */       ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, task -> new Thread(task, "bStats-Metrics"));
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 226 */       scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
/* 227 */       this.scheduler = scheduler;
/* 228 */       this.platform = platform;
/* 229 */       this.serverUuid = serverUuid;
/* 230 */       this.serviceId = serviceId;
/* 231 */       this.enabled = enabled;
/* 232 */       this.appendPlatformDataConsumer = appendPlatformDataConsumer;
/* 233 */       this.appendServiceDataConsumer = appendServiceDataConsumer;
/* 234 */       this.submitTaskConsumer = submitTaskConsumer;
/* 235 */       this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
/* 236 */       this.errorLogger = errorLogger;
/* 237 */       this.infoLogger = infoLogger;
/* 238 */       this.logErrors = logErrors;
/* 239 */       this.logSentData = logSentData;
/* 240 */       this.logResponseStatusText = logResponseStatusText;
/* 241 */       checkRelocation();
/* 242 */       if (enabled)
/*     */       {
/*     */         
/* 245 */         startSubmitting();
/*     */       }
/*     */     }
/*     */     
/*     */     public void addCustomChart(Metrics.CustomChart chart) {
/* 250 */       this.customCharts.add(chart);
/*     */     }
/*     */     
/*     */     public void shutdown() {
/* 254 */       this.scheduler.shutdown();
/*     */     }
/*     */     
/*     */     private void startSubmitting() {
/* 258 */       Runnable submitTask = () -> {
/*     */           if (!this.enabled || !((Boolean)this.checkServiceEnabledSupplier.get()).booleanValue()) {
/*     */             this.scheduler.shutdown();
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             return;
/*     */           } 
/*     */ 
/*     */ 
/*     */           
/*     */           if (this.submitTaskConsumer != null) {
/*     */             this.submitTaskConsumer.accept(this::submitData);
/*     */           } else {
/*     */             submitData();
/*     */           } 
/*     */         };
/*     */ 
/*     */ 
/*     */       
/* 279 */       long initialDelay = (long)(60000.0D * (3.0D + Math.random() * 3.0D));
/* 280 */       long secondDelay = (long)(60000.0D * Math.random() * 30.0D);
/* 281 */       this.scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
/* 282 */       this.scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1800000L, TimeUnit.MILLISECONDS);
/*     */     }
/*     */ 
/*     */     
/*     */     private void submitData() {
/* 287 */       Metrics.JsonObjectBuilder baseJsonBuilder = new Metrics.JsonObjectBuilder();
/* 288 */       this.appendPlatformDataConsumer.accept(baseJsonBuilder);
/* 289 */       Metrics.JsonObjectBuilder serviceJsonBuilder = new Metrics.JsonObjectBuilder();
/* 290 */       this.appendServiceDataConsumer.accept(serviceJsonBuilder);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 295 */       Metrics.JsonObjectBuilder.JsonObject[] chartData = (Metrics.JsonObjectBuilder.JsonObject[])this.customCharts.stream().map(customChart -> customChart.getRequestJsonObject(this.errorLogger, this.logErrors)).filter(Objects::nonNull).toArray(x$0 -> new Metrics.JsonObjectBuilder.JsonObject[x$0]);
/* 296 */       serviceJsonBuilder.appendField("id", this.serviceId);
/* 297 */       serviceJsonBuilder.appendField("customCharts", chartData);
/* 298 */       baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
/* 299 */       baseJsonBuilder.appendField("serverUUID", this.serverUuid);
/* 300 */       baseJsonBuilder.appendField("metricsVersion", "3.0.2");
/* 301 */       Metrics.JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
/* 302 */       this.scheduler.execute(() -> {
/*     */ 
/*     */             
/*     */             try {
/*     */               sendData(data);
/* 307 */             } catch (Exception e) {
/*     */               if (this.logErrors) {
/*     */                 this.errorLogger.accept("Could not submit bStats metrics data", e);
/*     */               }
/*     */             } 
/*     */           });
/*     */     }
/*     */ 
/*     */     
/*     */     private void sendData(Metrics.JsonObjectBuilder.JsonObject data) throws Exception {
/* 317 */       if (this.logSentData) {
/* 318 */         this.infoLogger.accept("Sent bStats metrics data: " + data.toString());
/*     */       }
/* 320 */       String url = String.format("https://bStats.org/api/v2/data/%s", new Object[] { this.platform });
/* 321 */       HttpsURLConnection connection = (HttpsURLConnection)(new URL(url)).openConnection();
/*     */       
/* 323 */       byte[] compressedData = compress(data.toString());
/* 324 */       connection.setRequestMethod("POST");
/* 325 */       connection.addRequestProperty("Accept", "application/json");
/* 326 */       connection.addRequestProperty("Connection", "close");
/* 327 */       connection.addRequestProperty("Content-Encoding", "gzip");
/* 328 */       connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
/* 329 */       connection.setRequestProperty("Content-Type", "application/json");
/* 330 */       connection.setRequestProperty("User-Agent", "Metrics-Service/1");
/* 331 */       connection.setDoOutput(true);
/* 332 */       DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream()); 
/* 333 */       try { outputStream.write(compressedData);
/* 334 */         outputStream.close(); } catch (Throwable throwable) { try { outputStream.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }
/* 335 */        StringBuilder builder = new StringBuilder();
/*     */       
/* 337 */       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream())); 
/*     */       try { String line;
/* 339 */         while ((line = bufferedReader.readLine()) != null) {
/* 340 */           builder.append(line);
/*     */         }
/* 342 */         bufferedReader.close(); } catch (Throwable throwable) { try { bufferedReader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }
/* 343 */        if (this.logResponseStatusText) {
/* 344 */         this.infoLogger.accept("Sent data to bStats and received response: " + builder);
/*     */       }
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     private void checkRelocation() {
/* 351 */       if (System.getProperty("bstats.relocatecheck") == null || 
/* 352 */         !System.getProperty("bstats.relocatecheck").equals("false")) {
/*     */ 
/*     */         
/* 355 */         String defaultPackage = new String(new byte[] { 111, 114, 103, 46, 98, 115, 116, 97, 116, 115 });
/*     */         
/* 357 */         String examplePackage = new String(new byte[] { 121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101 });
/*     */ 
/*     */ 
/*     */         
/* 361 */         if (MetricsBase.class.getPackage().getName().startsWith(defaultPackage) || MetricsBase.class
/* 362 */           .getPackage().getName().startsWith(examplePackage)) {
/* 363 */           throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
/*     */         }
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private static byte[] compress(String str) throws IOException {
/* 375 */       if (str == null) {
/* 376 */         return null;
/*     */       }
/* 378 */       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
/* 379 */       GZIPOutputStream gzip = new GZIPOutputStream(outputStream); 
/* 380 */       try { gzip.write(str.getBytes(StandardCharsets.UTF_8));
/* 381 */         gzip.close(); } catch (Throwable throwable) { try { gzip.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }
/* 382 */        return outputStream.toByteArray();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class SimplePie
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<String> callable;
/*     */ 
/*     */ 
/*     */     
/*     */     public SimplePie(String chartId, Callable<String> callable) {
/* 397 */       super(chartId);
/* 398 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     protected Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 403 */       String value = this.callable.call();
/* 404 */       if (value == null || value.isEmpty())
/*     */       {
/* 406 */         return null;
/*     */       }
/* 408 */       return (new Metrics.JsonObjectBuilder()).appendField("value", value).build();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class MultiLineChart
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<Map<String, Integer>> callable;
/*     */ 
/*     */ 
/*     */     
/*     */     public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
/* 423 */       super(chartId);
/* 424 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     protected Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 429 */       Metrics.JsonObjectBuilder valuesBuilder = new Metrics.JsonObjectBuilder();
/* 430 */       Map<String, Integer> map = this.callable.call();
/* 431 */       if (map == null || map.isEmpty())
/*     */       {
/* 433 */         return null;
/*     */       }
/* 435 */       boolean allSkipped = true;
/* 436 */       for (Map.Entry<String, Integer> entry : map.entrySet()) {
/* 437 */         if (((Integer)entry.getValue()).intValue() == 0) {
/*     */           continue;
/*     */         }
/*     */         
/* 441 */         allSkipped = false;
/* 442 */         valuesBuilder.appendField(entry.getKey(), ((Integer)entry.getValue()).intValue());
/*     */       } 
/* 444 */       if (allSkipped)
/*     */       {
/* 446 */         return null;
/*     */       }
/* 448 */       return (new Metrics.JsonObjectBuilder()).appendField("values", valuesBuilder.build()).build();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class AdvancedPie
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<Map<String, Integer>> callable;
/*     */ 
/*     */ 
/*     */     
/*     */     public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
/* 463 */       super(chartId);
/* 464 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     protected Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 469 */       Metrics.JsonObjectBuilder valuesBuilder = new Metrics.JsonObjectBuilder();
/* 470 */       Map<String, Integer> map = this.callable.call();
/* 471 */       if (map == null || map.isEmpty())
/*     */       {
/* 473 */         return null;
/*     */       }
/* 475 */       boolean allSkipped = true;
/* 476 */       for (Map.Entry<String, Integer> entry : map.entrySet()) {
/* 477 */         if (((Integer)entry.getValue()).intValue() == 0) {
/*     */           continue;
/*     */         }
/*     */         
/* 481 */         allSkipped = false;
/* 482 */         valuesBuilder.appendField(entry.getKey(), ((Integer)entry.getValue()).intValue());
/*     */       } 
/* 484 */       if (allSkipped)
/*     */       {
/* 486 */         return null;
/*     */       }
/* 488 */       return (new Metrics.JsonObjectBuilder()).appendField("values", valuesBuilder.build()).build();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class SimpleBarChart
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<Map<String, Integer>> callable;
/*     */ 
/*     */ 
/*     */     
/*     */     public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
/* 503 */       super(chartId);
/* 504 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     protected Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 509 */       Metrics.JsonObjectBuilder valuesBuilder = new Metrics.JsonObjectBuilder();
/* 510 */       Map<String, Integer> map = this.callable.call();
/* 511 */       if (map == null || map.isEmpty())
/*     */       {
/* 513 */         return null;
/*     */       }
/* 515 */       for (Map.Entry<String, Integer> entry : map.entrySet()) {
/* 516 */         valuesBuilder.appendField(entry.getKey(), new int[] { ((Integer)entry.getValue()).intValue() });
/*     */       } 
/* 518 */       return (new Metrics.JsonObjectBuilder()).appendField("values", valuesBuilder.build()).build();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class AdvancedBarChart
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<Map<String, int[]>> callable;
/*     */ 
/*     */ 
/*     */     
/*     */     public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
/* 533 */       super(chartId);
/* 534 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     protected Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 539 */       Metrics.JsonObjectBuilder valuesBuilder = new Metrics.JsonObjectBuilder();
/* 540 */       Map<String, int[]> map = this.callable.call();
/* 541 */       if (map == null || map.isEmpty())
/*     */       {
/* 543 */         return null;
/*     */       }
/* 545 */       boolean allSkipped = true;
/* 546 */       for (Map.Entry<String, int[]> entry : map.entrySet()) {
/* 547 */         if (((int[])entry.getValue()).length == 0) {
/*     */           continue;
/*     */         }
/*     */         
/* 551 */         allSkipped = false;
/* 552 */         valuesBuilder.appendField(entry.getKey(), entry.getValue());
/*     */       } 
/* 554 */       if (allSkipped)
/*     */       {
/* 556 */         return null;
/*     */       }
/* 558 */       return (new Metrics.JsonObjectBuilder()).appendField("values", valuesBuilder.build()).build();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class DrilldownPie
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<Map<String, Map<String, Integer>>> callable;
/*     */ 
/*     */ 
/*     */     
/*     */     public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
/* 573 */       super(chartId);
/* 574 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     public Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 579 */       Metrics.JsonObjectBuilder valuesBuilder = new Metrics.JsonObjectBuilder();
/* 580 */       Map<String, Map<String, Integer>> map = this.callable.call();
/* 581 */       if (map == null || map.isEmpty())
/*     */       {
/* 583 */         return null;
/*     */       }
/* 585 */       boolean reallyAllSkipped = true;
/* 586 */       for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
/* 587 */         Metrics.JsonObjectBuilder valueBuilder = new Metrics.JsonObjectBuilder();
/* 588 */         boolean allSkipped = true;
/* 589 */         for (Map.Entry<String, Integer> valueEntry : (Iterable<Map.Entry<String, Integer>>)((Map)map.get(entryValues.getKey())).entrySet()) {
/* 590 */           valueBuilder.appendField(valueEntry.getKey(), ((Integer)valueEntry.getValue()).intValue());
/* 591 */           allSkipped = false;
/*     */         } 
/* 593 */         if (!allSkipped) {
/* 594 */           reallyAllSkipped = false;
/* 595 */           valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
/*     */         } 
/*     */       } 
/* 598 */       if (reallyAllSkipped)
/*     */       {
/* 600 */         return null;
/*     */       }
/* 602 */       return (new Metrics.JsonObjectBuilder()).appendField("values", valuesBuilder.build()).build();
/*     */     }
/*     */   }
/*     */   
/*     */   public static abstract class CustomChart
/*     */   {
/*     */     private final String chartId;
/*     */     
/*     */     protected CustomChart(String chartId) {
/* 611 */       if (chartId == null) {
/* 612 */         throw new IllegalArgumentException("chartId must not be null");
/*     */       }
/* 614 */       this.chartId = chartId;
/*     */     }
/*     */ 
/*     */     
/*     */     public Metrics.JsonObjectBuilder.JsonObject getRequestJsonObject(BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
/* 619 */       Metrics.JsonObjectBuilder builder = new Metrics.JsonObjectBuilder();
/* 620 */       builder.appendField("chartId", this.chartId);
/*     */       try {
/* 622 */         Metrics.JsonObjectBuilder.JsonObject data = getChartData();
/* 623 */         if (data == null)
/*     */         {
/* 625 */           return null;
/*     */         }
/* 627 */         builder.appendField("data", data);
/* 628 */       } catch (Throwable t) {
/* 629 */         if (logErrors) {
/* 630 */           errorLogger.accept("Failed to get data for custom chart with id " + this.chartId, t);
/*     */         }
/* 632 */         return null;
/*     */       } 
/* 634 */       return builder.build();
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     protected abstract Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static class SingleLineChart
/*     */     extends CustomChart
/*     */   {
/*     */     private final Callable<Integer> callable;
/*     */ 
/*     */     
/*     */     public SingleLineChart(String chartId, Callable<Integer> callable) {
/* 651 */       super(chartId);
/* 652 */       this.callable = callable;
/*     */     }
/*     */ 
/*     */     
/*     */     protected Metrics.JsonObjectBuilder.JsonObject getChartData() throws Exception {
/* 657 */       int value = ((Integer)this.callable.call()).intValue();
/* 658 */       if (value == 0)
/*     */       {
/* 660 */         return null;
/*     */       }
/* 662 */       return (new Metrics.JsonObjectBuilder()).appendField("value", value).build();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class JsonObjectBuilder
/*     */   {
/* 674 */     private StringBuilder builder = new StringBuilder();
/*     */     
/*     */     private boolean hasAtLeastOneField = false;
/*     */     
/*     */     public JsonObjectBuilder() {
/* 679 */       this.builder.append("{");
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendNull(String key) {
/* 689 */       appendFieldUnescaped(key, "null");
/* 690 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendField(String key, String value) {
/* 701 */       if (value == null) {
/* 702 */         throw new IllegalArgumentException("JSON value must not be null");
/*     */       }
/* 704 */       appendFieldUnescaped(key, "\"" + escape(value) + "\"");
/* 705 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendField(String key, int value) {
/* 716 */       appendFieldUnescaped(key, String.valueOf(value));
/* 717 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendField(String key, JsonObject object) {
/* 728 */       if (object == null) {
/* 729 */         throw new IllegalArgumentException("JSON object must not be null");
/*     */       }
/* 731 */       appendFieldUnescaped(key, object.toString());
/* 732 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendField(String key, String[] values) {
/* 743 */       if (values == null) {
/* 744 */         throw new IllegalArgumentException("JSON values must not be null");
/*     */       }
/*     */ 
/*     */ 
/*     */       
/* 749 */       String escapedValues = Arrays.<String>stream(values).map(value -> "\"" + escape(value) + "\"").collect(Collectors.joining(","));
/* 750 */       appendFieldUnescaped(key, "[" + escapedValues + "]");
/* 751 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendField(String key, int[] values) {
/* 762 */       if (values == null) {
/* 763 */         throw new IllegalArgumentException("JSON values must not be null");
/*     */       }
/*     */       
/* 766 */       String escapedValues = Arrays.stream(values).<CharSequence>mapToObj(String::valueOf).collect(Collectors.joining(","));
/* 767 */       appendFieldUnescaped(key, "[" + escapedValues + "]");
/* 768 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObjectBuilder appendField(String key, JsonObject[] values) {
/* 779 */       if (values == null) {
/* 780 */         throw new IllegalArgumentException("JSON values must not be null");
/*     */       }
/*     */       
/* 783 */       String escapedValues = Arrays.<JsonObject>stream(values).map(JsonObject::toString).collect(Collectors.joining(","));
/* 784 */       appendFieldUnescaped(key, "[" + escapedValues + "]");
/* 785 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private void appendFieldUnescaped(String key, String escapedValue) {
/* 795 */       if (this.builder == null) {
/* 796 */         throw new IllegalStateException("JSON has already been built");
/*     */       }
/* 798 */       if (key == null) {
/* 799 */         throw new IllegalArgumentException("JSON key must not be null");
/*     */       }
/* 801 */       if (this.hasAtLeastOneField) {
/* 802 */         this.builder.append(",");
/*     */       }
/* 804 */       this.builder.append("\"").append(escape(key)).append("\":").append(escapedValue);
/* 805 */       this.hasAtLeastOneField = true;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public JsonObject build() {
/* 814 */       if (this.builder == null) {
/* 815 */         throw new IllegalStateException("JSON has already been built");
/*     */       }
/* 817 */       JsonObject object = new JsonObject(this.builder.append("}").toString());
/* 818 */       this.builder = null;
/* 819 */       return object;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private static String escape(String value) {
/* 832 */       StringBuilder builder = new StringBuilder();
/* 833 */       for (int i = 0; i < value.length(); i++) {
/* 834 */         char c = value.charAt(i);
/* 835 */         if (c == '"') {
/* 836 */           builder.append("\\\"");
/* 837 */         } else if (c == '\\') {
/* 838 */           builder.append("\\\\");
/* 839 */         } else if (c <= '\017') {
/* 840 */           builder.append("\\u000").append(Integer.toHexString(c));
/* 841 */         } else if (c <= '\037') {
/* 842 */           builder.append("\\u00").append(Integer.toHexString(c));
/*     */         } else {
/* 844 */           builder.append(c);
/*     */         } 
/*     */       } 
/* 847 */       return builder.toString();
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public static class JsonObject
/*     */     {
/*     */       private final String value;
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       private JsonObject(String value) {
/* 862 */         this.value = value;
/*     */       }
/*     */ 
/*     */       
/*     */       public String toString() {
/* 867 */         return this.value;
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Desktop\AntiCombatLog-2.6.0.jar!\net\badbird5907\anticombatlo\\utils\Metrics.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */