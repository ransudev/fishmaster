package rohan.fishmaster.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    // Static keybindings that appear in Minecraft's controls menu
    public static KeyBinding TOGGLE_AUTO_FISHING;
    public static KeyBinding EMERGENCY_STOP;
    public static KeyBinding TOGGLE_DEBUG;
    public static KeyBinding TOGGLE_SEA_CREATURE_KILLER;

    public static void register() {
        // Create keybindings with default keys that will appear in Minecraft's controls
        TOGGLE_AUTO_FISHING = new KeyBinding(
            "key.fishmaster.toggle_auto_fishing",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F, // Default to F key
            "category.fishmaster.general"
        );

        EMERGENCY_STOP = new KeyBinding(
            "key.fishmaster.emergency_stop",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H, // Default to H key
            "category.fishmaster.safety"
        );

        TOGGLE_DEBUG = new KeyBinding(
            "key.fishmaster.toggle_debug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8, // Default to F8 key
            "category.fishmaster.general"
        );

        TOGGLE_SEA_CREATURE_KILLER = new KeyBinding(
            "key.fishmaster.toggle_sea_creature_killer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // Default to G key
            "category.fishmaster.combat"
        );

        // Register all keybindings with Minecraft
        KeyBindingHelper.registerKeyBinding(TOGGLE_AUTO_FISHING);
        KeyBindingHelper.registerKeyBinding(EMERGENCY_STOP);
        KeyBindingHelper.registerKeyBinding(TOGGLE_DEBUG);
        KeyBindingHelper.registerKeyBinding(TOGGLE_SEA_CREATURE_KILLER);
    }

    public static void onKey() {
        while (TOGGLE_AUTO_FISHING.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.toggle();
        }

        while (EMERGENCY_STOP.wasPressed()) {
            // Emergency stop functionality
            rohan.fishmaster.feature.AutoFishingFeature.emergencyStop();
        }

        while (TOGGLE_DEBUG.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.toggleDebugMode();
        }

        while (TOGGLE_SEA_CREATURE_KILLER.wasPressed()) {
            rohan.fishmaster.feature.SeaCreatureKiller.toggle();
        }
    }
}
