package rohan.fishmaster.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;

public class FishingEvents {
    public static void register() {
        // Register tick event for fishing type detection and safety checks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeyBindings.onKey(); // Handle keybind presses
        });

        // Register disconnect event for failsafe
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            AutoFishingFeature.onDisconnect();
        });

        // Register join event to reset failsafe state
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Only trigger world change detection if auto fishing is currently enabled
            if (AutoFishingFeature.isEnabled()) {
                AutoFishingFeature.onServerSwitch();
            }
        });
    }

    public static void onGameExit() {
        // Cleanup when game exits
        AutoFishingFeature.onDisconnect();
    }
}
