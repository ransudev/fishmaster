package rohan.fishmaster.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import rohan.fishmaster.feature.AutoFishingFeature;

public class WindowFocusEvents {
    private static boolean wasWindowFocused = true;
    private static boolean wasAutoFishingEnabled = false;

    public static void register() {
        // Register tick event to check window focus state
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() != null) {
                boolean isWindowFocused = client.isWindowFocused();
                
                // Check if window focus state changed
                if (wasWindowFocused != isWindowFocused) {
                    onWindowFocusChanged(client, isWindowFocused);
                    wasWindowFocused = isWindowFocused;
                }
            }
        });
    }

    private static void onWindowFocusChanged(MinecraftClient client, boolean isFocused) {
        if (!isFocused) {
            // Window lost focus - save auto fishing state if it was enabled
            if (AutoFishingFeature.isEnabled()) {
                wasAutoFishingEnabled = true;
                // Notify auto fishing feature about focus change
                AutoFishingFeature.onWindowFocusChanged(false);
            }
        } else {
            // Window gained focus - restore auto fishing if it was enabled
            if (wasAutoFishingEnabled) {
                // Notify auto fishing feature about focus change
                AutoFishingFeature.onWindowFocusChanged(true);
            }
        }
    }

    /**
     * Check if the window is currently focused
     */
    public static boolean isWindowFocused() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.getWindow() != null && client.isWindowFocused();
    }
}