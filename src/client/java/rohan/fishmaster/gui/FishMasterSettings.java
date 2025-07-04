package rohan.fishmaster.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.SeaCreatureKiller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * FishMaster Settings - Compatible with Essential GUI
 * This version provides a foundation for Essential integration while working standalone
 */
public class FishMasterSettings {

    private static FishMasterSettings instance;
    private final Properties properties;
    private final File configFile;

    // === MAIN FEATURES ===
    public boolean autoFishingEnabled = false;
    public boolean seaCreatureKillerEnabled = false;
    public boolean fishingTrackerEnabled = true;

    // === KEYBINDS ===
    public int autoFishingKeybind = 0; // F key
    public int seaCreatureKillerKeybind = 1; // G key
    public int emergencyStopKeybind = 2; // H key
    public int settingsGuiKeybind = 12; // M key

    // === SAFETY & FAILSAFES ===
    public boolean healthChecksEnabled = true;
    public int minHealthThreshold = 6;
    public boolean antiAfkEnabled = true;
    public boolean pauseOnPlayerMovementEnabled = true;
    public int movementSensitivity = 5;

    // === SESSION LIMITS ===
    public int maxSessionTimeMinutes = 60;
    public int maxIdleTimeMinutes = 10;
    public int maxConsecutiveFailures = 10;

    // === SEA CREATURE KILLER SETTINGS ===
    public boolean combatModeEnabled = true;
    public int detectionRange = 7;
    public int attackSpeed = 4;
    public boolean targetPriority = true;
    public boolean combatMessages = true;

    // === FISHING SETTINGS ===
    public int castDelayMs = 1500;
    public int reactionTimeMs = 300;
    public boolean randomizedTiming = true;
    public int timingVariationPercent = 25;

    // === VISUAL & FEEDBACK ===
    public boolean chatNotifications = true;
    public boolean screenNotifications = false;
    public boolean statisticsDisplay = true;
    public int statisticsPosition = 1; // Top Right

    // === DEBUG & ADVANCED ===
    public boolean debugMode = false;
    public boolean verboseLogging = false;
    public boolean entityDebug = false;

    private FishMasterSettings() {
        // Create config directory if it doesn't exist
        File configDir = new File("./config/fishmaster/");
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create config directory");
            }
        }

        configFile = new File(configDir, "settings.properties");
        properties = new Properties();

        loadSettings();
    }

    public static FishMasterSettings getInstance() {
        if (instance == null) {
            instance = new FishMasterSettings();
        }
        return instance;
    }

    private void loadSettings() {
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);

                // Load all settings from properties
                autoFishingEnabled = getBooleanProperty("autoFishingEnabled", false);
                seaCreatureKillerEnabled = getBooleanProperty("seaCreatureKillerEnabled", false);
                fishingTrackerEnabled = getBooleanProperty("fishingTrackerEnabled", true);

                autoFishingKeybind = getIntProperty("autoFishingKeybind", 0);
                seaCreatureKillerKeybind = getIntProperty("seaCreatureKillerKeybind", 1);
                emergencyStopKeybind = getIntProperty("emergencyStopKeybind", 2);
                settingsGuiKeybind = getIntProperty("settingsGuiKeybind", 12);

                healthChecksEnabled = getBooleanProperty("healthChecksEnabled", true);
                minHealthThreshold = getIntProperty("minHealthThreshold", 6);
                antiAfkEnabled = getBooleanProperty("antiAfkEnabled", true);
                pauseOnPlayerMovementEnabled = getBooleanProperty("pauseOnPlayerMovementEnabled", true);
                movementSensitivity = getIntProperty("movementSensitivity", 5);

                maxSessionTimeMinutes = getIntProperty("maxSessionTimeMinutes", 60);
                maxIdleTimeMinutes = getIntProperty("maxIdleTimeMinutes", 10);
                maxConsecutiveFailures = getIntProperty("maxConsecutiveFailures", 10);

                combatModeEnabled = getBooleanProperty("combatModeEnabled", true);
                detectionRange = getIntProperty("detectionRange", 7);
                attackSpeed = getIntProperty("attackSpeed", 4);
                targetPriority = getBooleanProperty("targetPriority", true);
                combatMessages = getBooleanProperty("combatMessages", true);

                castDelayMs = getIntProperty("castDelayMs", 1500);
                reactionTimeMs = getIntProperty("reactionTimeMs", 300);
                randomizedTiming = getBooleanProperty("randomizedTiming", true);
                timingVariationPercent = getIntProperty("timingVariationPercent", 25);

                chatNotifications = getBooleanProperty("chatNotifications", true);
                screenNotifications = getBooleanProperty("screenNotifications", false);
                statisticsDisplay = getBooleanProperty("statisticsDisplay", true);
                statisticsPosition = getIntProperty("statisticsPosition", 1);

                debugMode = getBooleanProperty("debugMode", false);
                verboseLogging = getBooleanProperty("verboseLogging", false);
                entityDebug = getBooleanProperty("entityDebug", false);

            } catch (IOException e) {
                System.err.println("Failed to load FishMaster settings: " + e.getMessage());
            }
        }
    }

    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            // Save all settings to properties
            properties.setProperty("autoFishingEnabled", String.valueOf(autoFishingEnabled));
            properties.setProperty("seaCreatureKillerEnabled", String.valueOf(seaCreatureKillerEnabled));
            properties.setProperty("fishingTrackerEnabled", String.valueOf(fishingTrackerEnabled));

            properties.setProperty("autoFishingKeybind", String.valueOf(autoFishingKeybind));
            properties.setProperty("seaCreatureKillerKeybind", String.valueOf(seaCreatureKillerKeybind));
            properties.setProperty("emergencyStopKeybind", String.valueOf(emergencyStopKeybind));
            properties.setProperty("settingsGuiKeybind", String.valueOf(settingsGuiKeybind));

            properties.setProperty("healthChecksEnabled", String.valueOf(healthChecksEnabled));
            properties.setProperty("minHealthThreshold", String.valueOf(minHealthThreshold));
            properties.setProperty("antiAfkEnabled", String.valueOf(antiAfkEnabled));
            properties.setProperty("pauseOnPlayerMovementEnabled", String.valueOf(pauseOnPlayerMovementEnabled));
            properties.setProperty("movementSensitivity", String.valueOf(movementSensitivity));

            properties.setProperty("maxSessionTimeMinutes", String.valueOf(maxSessionTimeMinutes));
            properties.setProperty("maxIdleTimeMinutes", String.valueOf(maxIdleTimeMinutes));
            properties.setProperty("maxConsecutiveFailures", String.valueOf(maxConsecutiveFailures));

            properties.setProperty("combatModeEnabled", String.valueOf(combatModeEnabled));
            properties.setProperty("detectionRange", String.valueOf(detectionRange));
            properties.setProperty("attackSpeed", String.valueOf(attackSpeed));
            properties.setProperty("targetPriority", String.valueOf(targetPriority));
            properties.setProperty("combatMessages", String.valueOf(combatMessages));

            properties.setProperty("castDelayMs", String.valueOf(castDelayMs));
            properties.setProperty("reactionTimeMs", String.valueOf(reactionTimeMs));
            properties.setProperty("randomizedTiming", String.valueOf(randomizedTiming));
            properties.setProperty("timingVariationPercent", String.valueOf(timingVariationPercent));

            properties.setProperty("chatNotifications", String.valueOf(chatNotifications));
            properties.setProperty("screenNotifications", String.valueOf(screenNotifications));
            properties.setProperty("statisticsDisplay", String.valueOf(statisticsDisplay));
            properties.setProperty("statisticsPosition", String.valueOf(statisticsPosition));

            properties.setProperty("debugMode", String.valueOf(debugMode));
            properties.setProperty("verboseLogging", String.valueOf(verboseLogging));
            properties.setProperty("entityDebug", String.valueOf(entityDebug));

            properties.store(fos, "FishMaster Configuration - Compatible with Essential GUI");

            // Apply settings to features when saved
            applySettings();

        } catch (IOException e) {
            System.err.println("Failed to save FishMaster settings: " + e.getMessage());
        }
    }

    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void applySettings() {
        // Apply auto fishing setting
        if (autoFishingEnabled != AutoFishingFeature.isEnabled()) {
            AutoFishingFeature.toggle();
        }

        // Apply sea creature killer setting - can be toggled independently now
        if (seaCreatureKillerEnabled != SeaCreatureKiller.isEnabled()) {
            SeaCreatureKiller.toggle();
        }
    }

    public void refreshFromFeatures() {
        autoFishingEnabled = AutoFishingFeature.isEnabled();
        seaCreatureKillerEnabled = SeaCreatureKiller.isEnabled();
    }

    public void sendChatMessage(String message, Formatting color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && chatNotifications) {
            client.player.sendMessage(Text.literal(message).formatted(color), false);
        }
    }

    public void sendStatusMessage(String feature, boolean enabled) {
        String status = enabled ? "ENABLED" : "DISABLED";
        Formatting color = enabled ? Formatting.GREEN : Formatting.RED;
        sendChatMessage("[FishMaster] " + feature + ": " + status, color);
    }

    // Essential GUI Integration Methods (for future Essential compatibility)
    public static void openGui() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            // Open the FishMaster settings screen
            client.setScreen(new FishMasterScreen(client.currentScreen));
        }
    }

    // Color scheme for Essential theming
    public int getPrimaryColor() {
        return 0x3498db; // Nice blue color
    }

    public int getSecondaryColor() {
        return 0x2c3e50; // Dark gray
    }

    // Keybind management methods
    public void setKeybind(String keybindName, int newKeybindId) {
        switch (keybindName.toLowerCase()) {
            case "auto_fishing":
                autoFishingKeybind = newKeybindId;
                break;
            case "sea_creature_killer":
                seaCreatureKillerKeybind = newKeybindId;
                break;
            case "emergency_stop":
                emergencyStopKeybind = newKeybindId;
                break;
            case "settings_gui":
                settingsGuiKeybind = newKeybindId;
                break;
        }
        saveSettings();
        // Update the actual keybindings
        rohan.fishmaster.config.KeyBindings.updateKeybindings();
    }

    public String getKeybindDisplayName(int keybindId) {
        return rohan.fishmaster.config.KeyBindings.getKeyName(keybindId);
    }

    // Cycle through available keybinds
    public int getNextKeybindId(int currentId) {
        return (currentId + 1) % 21; // 0-20 available keybinds
    }

    public int getPreviousKeybindId(int currentId) {
        return currentId == 0 ? 20 : currentId - 1;
    }

    // Method to toggle features via keybinds
    public void toggleAutoFishing() {
        autoFishingEnabled = !autoFishingEnabled;
        AutoFishingFeature.toggle();
        sendStatusMessage("Auto Fishing", autoFishingEnabled);
        saveSettings();
    }

    public void toggleSeaCreatureKiller() {
        // Allow toggling regardless of autofish state
        seaCreatureKillerEnabled = !seaCreatureKillerEnabled;
        SeaCreatureKiller.toggle();
        sendStatusMessage("Sea Creature Killer", seaCreatureKillerEnabled);
        saveSettings();
    }

    public void emergencyStop() {
        if (autoFishingEnabled) {
            autoFishingEnabled = false;
            AutoFishingFeature.toggle();
        }
        if (seaCreatureKillerEnabled) {
            seaCreatureKillerEnabled = false;
            SeaCreatureKiller.toggle();
        }
        sendChatMessage("[FishMaster] EMERGENCY STOP - All features disabled", Formatting.RED);
        saveSettings();
    }
}
