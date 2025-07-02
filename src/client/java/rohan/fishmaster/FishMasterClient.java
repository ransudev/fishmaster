package rohan.fishmaster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.handler.ClientTickHandler;
import rohan.fishmaster.handler.DisconnectHandler;
import rohan.fishmaster.config.KeyBindings; // Use the correct KeyBindings import
import rohan.fishmaster.feature.AutoFishingFeature;

public class FishMasterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load config first
        FishMasterConfig.load();

        // Enable fishing tracker by default - it's now always active
        FishMasterConfig.setFishingTrackerEnabled(true);

        // Initialize keybindings FIRST - this is critical for preventing the crash
        KeyBindings.register();

        // Initialize fishing events after keybindings are registered
        rohan.fishmaster.event.FishingEvents.register();

        // Initialize other handlers
        ClientTickHandler.initialize();
        DisconnectHandler.initialize();

        // Register the main tick event for auto fishing
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                AutoFishingFeature.tick();
            }
        });
    }
}