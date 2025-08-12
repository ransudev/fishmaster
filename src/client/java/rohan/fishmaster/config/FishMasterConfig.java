package rohan.fishmaster.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FishMasterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fishmaster").resolve("config.json");

    // Static fields for current values
    private static boolean fishingTrackerEnabled = false;
    private static boolean fishingTrackerSpookyEnabled = false;
    private static boolean fishingTrackerMarinaEnabled = false;
    private static boolean fishingTrackerWinterEnabled = false;
    private static boolean fishingTrackerTimeSinceEnabled = true;
    private static boolean autoDetectFishingTypeEnabled = true;
    private static int fishingTrackerType = 0; // 0 = water, 1 = lava, 2 = none
    private static boolean antiAfkEnabled = true;
    private static long maxSessionTime = 3600000; // Default 1 hour
    private static long maxIdleTime = 300000; // Default 5 minutes
    private static int maxConsecutiveFailures = 10;
    private static boolean enableHealthChecks = true;
    private static float minHealthThreshold = 6.0f; // 3 hearts
    private static boolean pauseOnPlayerMovement = true;
    private static String webhookUrl = "";
    private static boolean webhookEnabled = false;
    private static long healthCheckInterval = 300000; // 5 minutes in milliseconds
    private static String customMageWeapon = ""; // Custom mage weapon name for SCK
    private static boolean seaCreatureKillerEnabled = false; // New field for SCK toggle from GUI
    private static String seaCreatureKillerMode = "RCM"; // Default mode
    private static int autoFishingKeybind = GLFW.GLFW_KEY_F; // Default to F key

    // Default constructor for JSON serialization
    public FishMasterConfig() {
        // Constructor will be used by GSON for serialization/deserialization
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }

            String json = Files.readString(CONFIG_PATH);

            // Handle empty or corrupted config files
            if (json == null || json.trim().isEmpty() || json.trim().equals("{}")) {
                save();
                return;
            }

            ConfigData config = GSON.fromJson(json, ConfigData.class);

            if (config != null) {
                // Load values from config object to static fields
                fishingTrackerEnabled = config.fishingTrackerEnabled;
                fishingTrackerSpookyEnabled = config.fishingTrackerSpookyEnabled;
                fishingTrackerMarinaEnabled = config.fishingTrackerMarinaEnabled;
                fishingTrackerWinterEnabled = config.fishingTrackerWinterEnabled;
                fishingTrackerTimeSinceEnabled = config.fishingTrackerTimeSinceEnabled;
                autoDetectFishingTypeEnabled = config.autoDetectFishingTypeEnabled;
                fishingTrackerType = config.fishingTrackerType;
                antiAfkEnabled = config.antiAfkEnabled;
                maxSessionTime = config.maxSessionTime;
                maxIdleTime = config.maxIdleTime;
                maxConsecutiveFailures = config.maxConsecutiveFailures;
                enableHealthChecks = config.enableHealthChecks;
                minHealthThreshold = config.minHealthThreshold;
                pauseOnPlayerMovement = config.pauseOnPlayerMovement;
                webhookUrl = config.webhookUrl != null ? config.webhookUrl : "";
                webhookEnabled = config.webhookEnabled;
                healthCheckInterval = config.healthCheckInterval > 0 ? config.healthCheckInterval : 300000;
                customMageWeapon = config.customMageWeapon != null ? config.customMageWeapon : "";
                seaCreatureKillerEnabled = config.seaCreatureKillerEnabled;
                seaCreatureKillerMode = config.seaCreatureKillerMode != null ? config.seaCreatureKillerMode : "RCM";
                autoFishingKeybind = config.autoFishingKeybind;
            }
            System.out.println("[FishMaster] Config loaded successfully. Webhook URL: " + (webhookUrl.isEmpty() ? "Not set" : "Set"));
        } catch (Exception e) {
            System.err.println("Failed to load config, creating new one: " + e.getMessage());
            save(); // Create a new config file
        }
    }

    public static void save() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            // Create a config object with current values for serialization
            ConfigData configData = new ConfigData();
            configData.fishingTrackerEnabled = fishingTrackerEnabled;
            configData.fishingTrackerSpookyEnabled = fishingTrackerSpookyEnabled;
            configData.fishingTrackerMarinaEnabled = fishingTrackerMarinaEnabled;
            configData.fishingTrackerWinterEnabled = fishingTrackerWinterEnabled;
            configData.fishingTrackerTimeSinceEnabled = fishingTrackerTimeSinceEnabled;
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

            String json = GSON.toJson(configData);
            Files.writeString(CONFIG_PATH, json);
            System.out.println("[FishMaster] Config saved to: " + CONFIG_PATH);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    // Inner class for JSON serialization
    private static class ConfigData {
        public boolean fishingTrackerEnabled;
        public boolean fishingTrackerSpookyEnabled;
        public boolean fishingTrackerMarinaEnabled;
        public boolean fishingTrackerWinterEnabled;
        public boolean fishingTrackerTimeSinceEnabled;
        public boolean autoDetectFishingTypeEnabled;
        public int fishingTrackerType;
        public boolean antiAfkEnabled;
        public long maxSessionTime;
        public long maxIdleTime;
        public int maxConsecutiveFailures;
        public boolean enableHealthChecks;
        public float minHealthThreshold;
        public boolean pauseOnPlayerMovement;
        public String webhookUrl;
        public boolean webhookEnabled;
        public long healthCheckInterval;
        public String customMageWeapon;
        public boolean seaCreatureKillerEnabled;
        public String seaCreatureKillerMode;
        public int autoFishingKeybind;
    }

    // Getters
    public static boolean isFishingTrackerEnabled() {
        return fishingTrackerEnabled;
    }

    public static boolean isFishingTrackerSpookyEnabled() {
        return fishingTrackerSpookyEnabled;
    }

    public static boolean isFishingTrackerMarinaEnabled() {
        return fishingTrackerMarinaEnabled;
    }

    public static boolean isFishingTrackerWinterEnabled() {
        return fishingTrackerWinterEnabled;
    }

    public static boolean isFishingTrackerTimeSinceEnabled() {
        return fishingTrackerTimeSinceEnabled;
    }

    public static boolean isAutoDetectFishingTypeEnabled() {
        return autoDetectFishingTypeEnabled;
    }

    public static int getFishingTrackerType() {
        return fishingTrackerType;
    }

    public static boolean isAntiAfkEnabled() {
        return antiAfkEnabled;
    }

    public static long getMaxSessionTime() {
        return maxSessionTime;
    }

    public static long getMaxIdleTime() {
        return maxIdleTime;
    }

    public static int getMaxConsecutiveFailures() {
        return maxConsecutiveFailures;
    }

    public static boolean isHealthChecksEnabled() {
        return enableHealthChecks;
    }

    public static float getMinHealthThreshold() {
        return minHealthThreshold;
    }

    public static boolean isPauseOnPlayerMovementEnabled() {
        return pauseOnPlayerMovement;
    }

    public static String getWebhookUrl() {
        return webhookUrl;
    }

    public static boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    public static long getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public static String getCustomMageWeapon() {
        return customMageWeapon;
    }

    public static boolean isSeaCreatureKillerEnabled() {
        return seaCreatureKillerEnabled;
    }

    public static String getSeaCreatureKillerMode() {
        return seaCreatureKillerMode;
    }

    public static int getAutoFishingKeybind() {
        return autoFishingKeybind;
    }

    // Setters
    public static void setFishingTrackerEnabled(boolean enabled) {
        fishingTrackerEnabled = enabled;
        save();
    }

    public static void setFishingTrackerSpookyEnabled(boolean enabled) {
        fishingTrackerSpookyEnabled = enabled;
        save();
    }

    public static void setFishingTrackerMarinaEnabled(boolean enabled) {
        fishingTrackerMarinaEnabled = enabled;
        save();
    }

    public static void setFishingTrackerWinterEnabled(boolean enabled) {
        fishingTrackerWinterEnabled = enabled;
        save();
    }

    public static void setFishingTrackerTimeSinceEnabled(boolean enabled) {
        fishingTrackerTimeSinceEnabled = enabled;
        save();
    }

    public static void setAutoDetectFishingTypeEnabled(boolean enabled) {
        autoDetectFishingTypeEnabled = enabled;
        save();
    }

    public static void setFishingTrackerType(int type) {
        fishingTrackerType = type;
        save();
    }

    public static void setAntiAfkEnabled(boolean enabled) {
        antiAfkEnabled = enabled;
        save();
    }

    public static void setMaxSessionTime(long time) {
        maxSessionTime = time;
        save();
    }

    public static void setMaxIdleTime(long time) {
        maxIdleTime = time;
        save();
    }

    public static void setMaxConsecutiveFailures(int failures) {
        maxConsecutiveFailures = failures;
        save();
    }

    public static void setHealthChecksEnabled(boolean enabled) {
        enableHealthChecks = enabled;
        save();
    }

    public static void setMinHealthThreshold(float threshold) {
        minHealthThreshold = threshold;
        save();
    }

    public static void setPauseOnPlayerMovementEnabled(boolean enabled) {
        pauseOnPlayerMovement = enabled;
        save();
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
    }

    public static void setCustomMageWeapon(String weapon) {
        customMageWeapon = weapon != null ? weapon : "";
        save();
        System.out.println("[FishMaster] Custom mage weapon " + (customMageWeapon.isEmpty() ? "cleared" : "set to " + customMageWeapon));
    }

    public static void setSeaCreatureKillerEnabled(boolean enabled) {
        seaCreatureKillerEnabled = enabled;
        save();
    }

    public static void setSeaCreatureKillerMode(String mode) {
        seaCreatureKillerMode = mode;
        save();
    }

    public static void setAutoFishingKeybind(int keyCode) {
        autoFishingKeybind = keyCode;
        save();
    }

    public static void toggleFishingTracker() {
        fishingTrackerEnabled = !fishingTrackerEnabled;
        save();
    }
}
