package rohan.fishmaster.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FishMasterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fishmaster").resolve("config.json");

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

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }

            String json = Files.readString(CONFIG_PATH);
            FishMasterConfig config = GSON.fromJson(json, FishMasterConfig.class);

            if (config != null) {
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
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            String json = GSON.toJson(new FishMasterConfig());
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
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
        if (type >= 0 && type <= 2) {
            fishingTrackerType = type;
            save();
        }
    }

    public static void setAntiAfkEnabled(boolean enabled) {
        antiAfkEnabled = enabled;
        save();
    }

    public static void setMaxSessionTime(long time) {
        if (time > 0 && time <= 7200000) { // Max 2 hours
            maxSessionTime = time;
            save();
        }
    }

    public static void setMaxIdleTime(long time) {
        if (time > 0 && time <= 1800000) { // Max 30 minutes
            maxIdleTime = time;
            save();
        }
    }

    public static void setMaxConsecutiveFailures(int failures) {
        if (failures > 0 && failures <= 50) {
            maxConsecutiveFailures = failures;
            save();
        }
    }

    public static void setHealthChecksEnabled(boolean enabled) {
        enableHealthChecks = enabled;
        save();
    }

    public static void setMinHealthThreshold(float threshold) {
        if (threshold >= 2.0f && threshold <= 20.0f) {
            minHealthThreshold = threshold;
            save();
        }
    }

    public static void setPauseOnPlayerMovementEnabled(boolean enabled) {
        pauseOnPlayerMovement = enabled;
        save();
    }

    public static void toggleFishingTracker() {
        fishingTrackerEnabled = !fishingTrackerEnabled;
        save();
    }
}
