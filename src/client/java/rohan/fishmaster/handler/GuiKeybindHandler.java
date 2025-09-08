package rohan.fishmaster.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.feature.AutoFishingFeature;

public class GuiKeybindHandler {

    private static boolean initialized = false;
    private static boolean wasKeyPressed = false;

    public static void initialize() {
        if (initialized) return;

        ClientTickEvents.END_CLIENT_TICK.register(GuiKeybindHandler::onClientTick);
        initialized = true;
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.getWindow() == null) return;

        // Don't trigger keybind if any screen is open (especially our GUI)
        if (client.currentScreen != null) return;

        int autoFishKey = FishMasterConfig.getAutoFishingKeybind();
        if (autoFishKey == GLFW.GLFW_KEY_UNKNOWN) return;

        // Check if the auto-fishing key is currently pressed
        boolean isKeyPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), autoFishKey) == GLFW.GLFW_PRESS;

        // Only trigger on key press (not while held)
        if (isKeyPressed && !wasKeyPressed) {
            AutoFishingFeature.toggle();
        }

        wasKeyPressed = isKeyPressed;
    }
}
