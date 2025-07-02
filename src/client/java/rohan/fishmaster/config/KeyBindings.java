package rohan.fishmaster.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyBinding TOGGLE_FISHING_TRACKER = new KeyBinding(
        "key.fishmaster.toggle_tracker",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        "category.fishmaster.general"
    );

    public static final KeyBinding TOGGLE_AUTO_FISHING = new KeyBinding(
        "key.fishmaster.toggle_auto_fishing",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        "category.fishmaster.general"
    );

    public static final KeyBinding EMERGENCY_STOP = new KeyBinding(
        "key.fishmaster.emergency_stop",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_PERIOD,
        "category.fishmaster.safety"
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_FISHING_TRACKER);
        KeyBindingHelper.registerKeyBinding(TOGGLE_AUTO_FISHING);
        KeyBindingHelper.registerKeyBinding(EMERGENCY_STOP);
    }

    public static void onKey() {
        while (TOGGLE_FISHING_TRACKER.wasPressed()) {
            FishMasterConfig.toggleFishingTracker();
        }

        while (TOGGLE_AUTO_FISHING.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.toggle();
        }

        while (EMERGENCY_STOP.wasPressed()) {
            rohan.fishmaster.feature.AutoFishingFeature.emergencyStop();
        }
    }
}
