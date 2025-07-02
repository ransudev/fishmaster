package rohan.fishmaster.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
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

    public static final KeyBinding TOGGLE_DEBUG = new KeyBinding(
        "key.fishmaster.toggle_debug",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_F8,
        "category.fishmaster.general"
    );

    public static final KeyBinding TOGGLE_SEA_CREATURE_KILLER = new KeyBinding(
        "key.fishmaster.toggle_sea_creature_killer",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        "category.fishmaster.combat"
    );

    public static void register() {
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
