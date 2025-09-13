package rohan.fishmaster.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced configuration system with persistent storage and thread-safe operations
 * Automatically saves all GUI changes to maintain settings across sessions
 */
public class FishMasterConfig {
    // Gson instance for JSON serialization/deserialization
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fishmaster").resolve("config.json");
    
    // Thread-safe saving mechanism to batch multiple rapid changes
    private static final ScheduledExecutorService SAVE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "FishMaster-Config-Saver");
        t.setDaemon(true);
        return t;
    });
    
    private static volatile boolean saveScheduled = false;
    private static final Object SAVE_LOCK = new Object();

    // Configuration fields - marked volatile for thread safety
    private static volatile boolean autoDetectFishingTypeEnabled = true;
    private static volatile int fishingTrackerType = 0; // 0 = water, 1 = lava, 2 = none
    private static volatile boolean antiAfkEnabled = true;
    private static volatile long maxSessionTime = 3600000; // Default 1 hour
    private static volatile long maxIdleTime = 300000; // Default 5 minutes
    private static volatile int maxConsecutiveFailures = 10;
    private static volatile boolean enableHealthChecks = true;
    private static volatile float minHealthThreshold = 6.0f; // 3 hearts
    private static volatile boolean pauseOnPlayerMovement = true;
    private static volatile String webhookUrl = "";
    private static volatile boolean webhookEnabled = false;
    private static volatile long healthCheckInterval = 300000; // 5 minutes in milliseconds
    private static volatile String customMageWeapon = ""; // Custom mage weapon name for SCK
    private static volatile boolean seaCreatureKillerEnabled = false;
    private static volatile String seaCreatureKillerMode = "RCM"; // Default to RCM
    private static volatile int autoFishingKeybind = GLFW.GLFW_KEY_B; // Default keybind set to 'B'
    private static volatile float recastDelay = 5.0f; // Default 5 ticks (250ms) - matches GUI cycle button
    private static volatile float reelingDelay = 3.0f; // Default 3 ticks (150ms) - delay after fish bite before reeling
    private static volatile boolean ungrabMouseWhenFishing = true; // Default to ungrab mouse for background usage
    public static volatile int devMKillDist = 100;
    public static volatile int devMKillRot = 100;

    // Config loading state
    private static volatile boolean configLoaded = false;

    /**
     * Loads configuration from the JSON file
     * Creates a default config if file doesn't exist
     */
    public static void load() {
        synchronized (SAVE_LOCK) {
            try {
                if (!Files.exists(CONFIG_PATH)) {
                    System.out.println("[FishMaster] No config file found, creating default config...");
                    saveImmediate();
                    configLoaded = true;
                    return;
                }

                String json = Files.readString(CONFIG_PATH);

                // Handle empty or corrupted config files
                if (json == null || json.trim().isEmpty() || json.trim().equals("{}")) {
                    System.out.println("[FishMaster] Empty or corrupted config file, creating new one...");
                    saveImmediate();
                    configLoaded = true;
                    return;
                }

                ConfigData config = GSON.fromJson(json, ConfigData.class);

                if (config != null) {
                    // Load values from config object to static fields with validation
                    autoDetectFishingTypeEnabled = config.autoDetectFishingTypeEnabled;
                    fishingTrackerType = Math.max(0, Math.min(2, config.fishingTrackerType));
                    antiAfkEnabled = config.antiAfkEnabled;
                    maxSessionTime = Math.max(60000, config.maxSessionTime);
                    maxIdleTime = Math.max(30000, config.maxIdleTime);
                    maxConsecutiveFailures = Math.max(1, config.maxConsecutiveFailures);
                    enableHealthChecks = config.enableHealthChecks;
                    minHealthThreshold = Math.max(0.5f, Math.min(20.0f, config.minHealthThreshold));
                    pauseOnPlayerMovement = config.pauseOnPlayerMovement;
                    webhookUrl = config.webhookUrl != null ? config.webhookUrl : "";
                    webhookEnabled = config.webhookEnabled;
                    healthCheckInterval = Math.max(60000, config.healthCheckInterval);
                    customMageWeapon = config.customMageWeapon != null ? config.customMageWeapon : "";
                    seaCreatureKillerEnabled = config.seaCreatureKillerEnabled;
                    seaCreatureKillerMode = config.seaCreatureKillerMode != null ? config.seaCreatureKillerMode : "RCM";
                    autoFishingKeybind = config.autoFishingKeybind != 0 ? config.autoFishingKeybind : GLFW.GLFW_KEY_B;
                    recastDelay = Math.max(2.0f, Math.min(30.0f, config.recastDelay));
                    reelingDelay = Math.max(3.0f, Math.min(10.0f, config.reelingDelay)); // 150ms to 500ms
                    ungrabMouseWhenFishing = config.ungrabMouseWhenFishing;
                    devMKillDist = config.devMKillDist;
                    devMKillRot = config.devMKillRot;
                    
                    System.out.println("[FishMaster] Config loaded successfully from: " + CONFIG_PATH);
                    System.out.println("[FishMaster] AutoFishing keybind: " + autoFishingKeybind + 
                                     ", SCK enabled: " + seaCreatureKillerEnabled + 
                                     ", Recast delay: " + (recastDelay * 50) + "ms");
                } else {
                    System.out.println("[FishMaster] Config parsing failed, creating new config...");
                    saveImmediate();
                }
                
                configLoaded = true;
            } catch (Exception e) {
                System.err.println("[FishMaster] Failed to load config, creating new one: " + e.getMessage());
                e.printStackTrace();
                configLoaded = true;
                saveImmediate(); // Create a new config file
            }
        }
    }

    /**
     * Schedules a config save operation with a small delay to batch multiple changes
     * This prevents excessive file I/O when multiple settings are changed rapidly
     */
    public static void save() {
        if (!configLoaded) {
            System.out.println("[FishMaster] Config not loaded yet, skipping save");
            return;
        }
        
        synchronized (SAVE_LOCK) {
            if (!saveScheduled) {
                saveScheduled = true;
                SAVE_EXECUTOR.schedule(() -> {
                    synchronized (SAVE_LOCK) {
                        saveScheduled = false;
                        saveImmediate();
                    }
                }, 500, TimeUnit.MILLISECONDS); // 500ms delay to batch changes
            }
        }
    }

    /**
     * Immediately saves the config synchronously
     */
    public static void saveImmediate() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            // Create a config object with current values for serialization
            ConfigData configData = new ConfigData();
            configData.autoDetectFishingTypeEnabled = autoDetectFishingTypeEnabled;
            configData.fishingTrackerType = fishingTrackerType;
            configData.antiAfkEnabled = antiAfkEnabled;
            configData.maxSessionTime = maxSessionTime;
            configData.maxIdleTime = maxIdleTime;
            configData.maxConsecutiveFailures = maxConsecutiveFailures;
            configData.enableHealthChecks = enableHealthChecks;
            configData.minHealthThreshold = minHealthThreshold;
            configData.pauseOnPlayerMovement = pauseOnPlayerMovement;
            configData.webhookUrl = webhookUrl;
            configData.webhookEnabled = webhookEnabled;
            configData.healthCheckInterval = healthCheckInterval;
            configData.customMageWeapon = customMageWeapon;
            configData.seaCreatureKillerEnabled = seaCreatureKillerEnabled;
            configData.seaCreatureKillerMode = seaCreatureKillerMode;
            configData.autoFishingKeybind = autoFishingKeybind;
            configData.recastDelay = recastDelay;
            configData.reelingDelay = reelingDelay;
            configData.ungrabMouseWhenFishing = ungrabMouseWhenFishing;
            configData.devMKillDist = devMKillDist;
            configData.devMKillRot = devMKillRot;

            String json = GSON.toJson(configData);
            Files.writeString(CONFIG_PATH, json);
            System.out.println("[FishMaster] Config saved successfully");
        } catch (IOException e) {
            System.err.println("[FishMaster] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Forces an immediate save and shuts down the save executor - used during shutdown
     */
    public static void saveAndShutdown() {
        synchronized (SAVE_LOCK) {
            saveImmediate();
            SAVE_EXECUTOR.shutdown();
            try {
                if (!SAVE_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    SAVE_EXECUTOR.shutdownNow();
                }
            } catch (InterruptedException e) {
                SAVE_EXECUTOR.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Configuration data structure for JSON serialization
     * Includes default values to ensure proper initialization
     */
    private static class ConfigData {
        public boolean autoDetectFishingTypeEnabled = true;
        public int fishingTrackerType = 0;
        public boolean antiAfkEnabled = true;
        public long maxSessionTime = 3600000;
        public long maxIdleTime = 300000;
        public int maxConsecutiveFailures = 10;
        public boolean enableHealthChecks = true;
        public float minHealthThreshold = 6.0f;
        public boolean pauseOnPlayerMovement = true;
        public String webhookUrl = "";
        public boolean webhookEnabled = false;
        public long healthCheckInterval = 300000;
        public String customMageWeapon = "";
        public boolean seaCreatureKillerEnabled = false;
        public String seaCreatureKillerMode = "RCM";
        public int autoFishingKeybind = GLFW.GLFW_KEY_B;
        public float recastDelay = 5.0f; // 250ms in ticks
        public float reelingDelay = 3.0f; // 150ms in ticks
        public boolean ungrabMouseWhenFishing = true; // Ungrab mouse for background usage
        public int devMKillDist = 100;
        public int devMKillRot = 100;
    }

    // ======================== GETTERS ========================
    public static boolean isAutoDetectFishingTypeEnabled() { return autoDetectFishingTypeEnabled; }
    public static int getFishingTrackerType() { return fishingTrackerType; }
    public static boolean isAntiAfkEnabled() { return antiAfkEnabled; }
    public static long getMaxSessionTime() { return maxSessionTime; }
    public static long getMaxIdleTime() { return maxIdleTime; }
    public static int getMaxConsecutiveFailures() { return maxConsecutiveFailures; }
    public static boolean isHealthChecksEnabled() { return enableHealthChecks; }
    public static float getMinHealthThreshold() { return minHealthThreshold; }
    public static boolean isPauseOnPlayerMovementEnabled() { return pauseOnPlayerMovement; }
    public static String getWebhookUrl() { return webhookUrl; }
    public static boolean isWebhookEnabled() { return webhookEnabled; }
    public static long getHealthCheckInterval() { return healthCheckInterval; }
    public static String getCustomMageWeapon() { return customMageWeapon; }
    public static boolean isSeaCreatureKillerEnabled() { return seaCreatureKillerEnabled; }
    public static String getSeaCreatureKillerMode() { return seaCreatureKillerMode; }
    public static int getAutoFishingKeybind() { return autoFishingKeybind; }
    public static float getRecastDelay() { return recastDelay; }
    public static float getReelingDelay() { return reelingDelay; }
    public static boolean isUngrabMouseWhenFishingEnabled() { return ungrabMouseWhenFishing; }
    public static boolean isConfigLoaded() { return configLoaded; }

    // ======================== ENHANCED SETTERS WITH VALIDATION ========================

    public static void setAutoFishingKeybind(int keyCode) {
        autoFishingKeybind = keyCode;
        save();
        System.out.println("[FishMaster] AutoFishing keybind set to: " + keyCode);
    }

    public static void setAutoDetectFishingTypeEnabled(boolean enabled) {
        autoDetectFishingTypeEnabled = enabled;
        save();
        System.out.println("[FishMaster] Auto-detect fishing type: " + enabled);
    }

    public static void setFishingTrackerType(int type) {
        fishingTrackerType = Math.max(0, Math.min(2, type)); // Clamp 0-2
        save();
        System.out.println("[FishMaster] Fishing tracker type set to: " + fishingTrackerType);
    }

    public static void setAntiAfkEnabled(boolean enabled) {
        antiAfkEnabled = enabled;
        save();
        System.out.println("[FishMaster] Anti-AFK: " + enabled);
    }

    public static void setMaxSessionTime(long time) {
        maxSessionTime = Math.max(60000, time); // Minimum 1 minute
        save();
        System.out.println("[FishMaster] Max session time set to: " + maxSessionTime + "ms");
    }

    public static void setMaxIdleTime(long time) {
        maxIdleTime = Math.max(30000, time); // Minimum 30 seconds
        save();
        System.out.println("[FishMaster] Max idle time set to: " + maxIdleTime + "ms");
    }

    public static void setMaxConsecutiveFailures(int failures) {
        maxConsecutiveFailures = Math.max(1, failures);
        save();
        System.out.println("[FishMaster] Max consecutive failures set to: " + maxConsecutiveFailures);
    }

    public static void setHealthChecksEnabled(boolean enabled) {
        enableHealthChecks = enabled;
        save();
        System.out.println("[FishMaster] Health checks: " + enabled);
    }

    public static void setMinHealthThreshold(float threshold) {
        minHealthThreshold = Math.max(0.5f, Math.min(20.0f, threshold)); // 0.5 to 20 HP
        save();
        System.out.println("[FishMaster] Min health threshold set to: " + minHealthThreshold);
    }

    public static void setPauseOnPlayerMovementEnabled(boolean enabled) {
        pauseOnPlayerMovement = enabled;
        save();
        System.out.println("[FishMaster] Pause on player movement: " + enabled);
    }

    public static void setWebhookUrl(String url) {
        webhookUrl = url != null ? url : "";
        save();
        System.out.println("[FishMaster] Webhook URL " + (webhookUrl.isEmpty() ? "cleared" : "set"));
    }

    public static void setWebhookEnabled(boolean enabled) {
        webhookEnabled = enabled;
        save();
        System.out.println("[FishMaster] Webhook " + (enabled ? "enabled" : "disabled"));
    }

    public static void setHealthCheckInterval(long interval) {
        healthCheckInterval = Math.max(60000, interval); // Minimum 1 minute
        save();
        System.out.println("[FishMaster] Health check interval set to: " + healthCheckInterval + "ms");
    }

    public static void setCustomMageWeapon(String weapon) {
        customMageWeapon = weapon != null ? weapon : "";
        save();
        System.out.println("[FishMaster] Custom mage weapon " + (customMageWeapon.isEmpty() ? "cleared" : "set to " + customMageWeapon));
    }

    public static void setSeaCreatureKillerEnabled(boolean enabled) {
        seaCreatureKillerEnabled = enabled;
        save();
        System.out.println("[FishMaster] Sea Creature Killer: " + enabled);
    }

    public static void setSeaCreatureKillerMode(String mode) {
        seaCreatureKillerMode = mode != null ? mode : "RCM";
        save();
        System.out.println("[FishMaster] Sea Creature Killer mode set to: " + seaCreatureKillerMode);
    }

    /**
     * Sets the recast delay for auto fishing
     * @param delay Delay in ticks (2-30 ticks = 100ms-1500ms)
     */
    public static void setRecastDelay(float delay) {
        recastDelay = Math.max(2.0f, Math.min(30.0f, delay)); // 2-30 ticks (100ms-1500ms)
        save();
        System.out.println("[FishMaster] Recast delay set to: " + (recastDelay * 50) + "ms (" + recastDelay + " ticks)");
    }

    /**
     * Sets the reeling delay for auto fishing (delay after fish bite detection before reeling in)
     * @param delay Delay in ticks (3-10 ticks = 150ms-500ms)
     */
    public static void setReelingDelay(float delay) {
        reelingDelay = Math.max(3.0f, Math.min(10.0f, delay)); // 3-10 ticks (150ms-500ms)
        save();
        System.out.println("[FishMaster] Reeling delay set to: " + (reelingDelay * 50) + "ms (" + reelingDelay + " ticks)");
    }

    /**
     * Sets whether to ungrab mouse when auto-fishing is enabled
     * @param enabled True to ungrab mouse (allows background usage), false to keep mouse grabbed
     */
    public static void setUngrabMouseWhenFishingEnabled(boolean enabled) {
        ungrabMouseWhenFishing = enabled;
        save();
        System.out.println("[FishMaster] Mouse ungrabbing when fishing: " + (enabled ? "enabled" : "disabled"));
    }
}
