package rohan.fishmaster.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding toggleAutoFishing;

    public static void initialize() {
        // Create the auto fishing keybind with default key (F)
        // Minecraft will handle saving/loading the keybind configuration
        toggleAutoFishing = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fishmaster.auto_fish_toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F, // Default to F key
            "category.fishmaster.general"
        ));
    }
}
