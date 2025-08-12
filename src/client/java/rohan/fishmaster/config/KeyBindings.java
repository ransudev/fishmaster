package rohan.fishmaster.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    // Static keybindings that appear in Minecraft's controls menu
    // TOGGLE_AUTO_FISHING is now handled by GUI only, not registered with Minecraft
    public static KeyBinding EMERGENCY_STOP;
    public static KeyBinding TOGGLE_DEBUG;

    public static void register() {
        // Create keybindings with default keys that will appear in Minecraft's controls
        // Note: TOGGLE_AUTO_FISHING is no longer registered here - it's GUI only

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

        // Register only emergency stop and debug keybindings with Minecraft
        KeyBindingHelper.registerKeyBinding(EMERGENCY_STOP);
        KeyBindingHelper.registerKeyBinding(TOGGLE_DEBUG);
    }

    public static void onKey() {
        // TOGGLE_AUTO_FISHING is now handled by GUI only

        while (EMERGENCY_STOP.wasPressed()) {
            // Emergency stop functionality
            rohan.fishmaster.feature.AutoFishingFeature.emergencyStop();
        }

        while (TOGGLE_DEBUG.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.toggleDebugMode();
        }
    }
}
