package rohan.fishmaster.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import rohan.fishmaster.gui.FishMasterSettings;

public class KeyBindings {
    // Dynamic keybindings that are controlled by the GUI
    public static KeyBinding TOGGLE_AUTO_FISHING;
    public static KeyBinding EMERGENCY_STOP;
    public static KeyBinding TOGGLE_DEBUG;
    public static KeyBinding TOGGLE_SEA_CREATURE_KILLER;
    public static KeyBinding SETTINGS_GUI;

    public static void register() {
        // Get keybind settings from GUI
        FishMasterSettings settings = FishMasterSettings.getInstance();

        // Create keybindings based on GUI settings
        TOGGLE_AUTO_FISHING = new KeyBinding(
            "key.fishmaster.toggle_auto_fishing",
            InputUtil.Type.KEYSYM,
            getKeyFromId(settings.autoFishingKeybind),
            "category.fishmaster.general"
        );

        EMERGENCY_STOP = new KeyBinding(
            "key.fishmaster.emergency_stop",
            InputUtil.Type.KEYSYM,
            getKeyFromId(settings.emergencyStopKeybind),
            "category.fishmaster.safety"
        );

        TOGGLE_DEBUG = new KeyBinding(
            "key.fishmaster.toggle_debug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "category.fishmaster.general"
        );

        TOGGLE_SEA_CREATURE_KILLER = new KeyBinding(
            "key.fishmaster.toggle_sea_creature_killer",
            InputUtil.Type.KEYSYM,
            getKeyFromId(settings.seaCreatureKillerKeybind),
            "category.fishmaster.combat"
        );

        SETTINGS_GUI = new KeyBinding(
            "key.fishmaster.settings_gui",
            InputUtil.Type.KEYSYM,
            getKeyFromId(settings.settingsGuiKeybind),
            "category.fishmaster.general"
        );

        // Register all keybindings
        KeyBindingHelper.registerKeyBinding(TOGGLE_AUTO_FISHING);
        KeyBindingHelper.registerKeyBinding(EMERGENCY_STOP);
        KeyBindingHelper.registerKeyBinding(TOGGLE_DEBUG);
        KeyBindingHelper.registerKeyBinding(TOGGLE_SEA_CREATURE_KILLER);
        KeyBindingHelper.registerKeyBinding(SETTINGS_GUI);
    }

    public static void onKey() {
        while (TOGGLE_AUTO_FISHING.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.toggle();
        }

        while (EMERGENCY_STOP.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.emergencyStop();
        }

        while (TOGGLE_SEA_CREATURE_KILLER.wasPressed()) {
            rohan.fishmaster.feature.SeaCreatureKiller.toggle();
        }

        while (SETTINGS_GUI.wasPressed()) {
            // Open the FishMaster settings GUI
            rohan.fishmaster.gui.FishMasterSettings.openGui();
        }
    }

    // Convert keybind ID to actual key code
    private static int getKeyFromId(int keybindId) {
        switch (keybindId) {
            case 0: return GLFW.GLFW_KEY_F;
            case 1: return GLFW.GLFW_KEY_G;
            case 2: return GLFW.GLFW_KEY_H;
            case 3: return GLFW.GLFW_KEY_I;
            case 4: return GLFW.GLFW_KEY_J;
            case 5: return GLFW.GLFW_KEY_K;
            case 6: return GLFW.GLFW_KEY_L;
            case 7: return GLFW.GLFW_KEY_N;
            case 8: return GLFW.GLFW_KEY_O;
            case 9: return GLFW.GLFW_KEY_P;
            case 10: return GLFW.GLFW_KEY_Q;
            case 11: return GLFW.GLFW_KEY_R;
            case 12: return GLFW.GLFW_KEY_M;
            case 13: return GLFW.GLFW_KEY_SEMICOLON;
            case 14: return GLFW.GLFW_KEY_APOSTROPHE;
            case 15: return GLFW.GLFW_KEY_COMMA;
            case 16: return GLFW.GLFW_KEY_PERIOD;
            case 17: return GLFW.GLFW_KEY_SLASH;
            case 18: return GLFW.GLFW_KEY_LEFT_BRACKET;
            case 19: return GLFW.GLFW_KEY_RIGHT_BRACKET;
            case 20: return GLFW.GLFW_KEY_BACKSLASH;
            default: return GLFW.GLFW_KEY_UNKNOWN;
        }
    }

    // Get key name for display in GUI
    public static String getKeyName(int keybindId) {
        switch (keybindId) {
            case 0: return "F";
            case 1: return "G";
            case 2: return "H";
            case 3: return "I";
            case 4: return "J";
            case 5: return "K";
            case 6: return "L";
            case 7: return "N";
            case 8: return "O";
            case 9: return "P";
            case 10: return "Q";
            case 11: return "R";
            case 12: return "M";
            case 13: return ";";
            case 14: return "'";
            case 15: return ",";
            case 16: return ".";
            case 17: return "/";
            case 18: return "[";
            case 19: return "]";
            case 20: return "\\";
            default: return "UNKNOWN";
        }
    }

    // Re-register keybindings when settings change
    public static void updateKeybindings() {
        // This method is called when GUI settings are updated
        // We need to re-register the keybindings with new keys
        register();
    }
}
