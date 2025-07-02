package rohan.fishmaster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.handler.ClientTickHandler;
import rohan.fishmaster.handler.DisconnectHandler;
import rohan.fishmaster.keybind.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;

public class FishMasterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load config first
        FishMasterConfig.load();

        // Initialize keybindings - now uses Minecraft's native keybinding system
        KeyBindings.initialize();


        ClientTickHandler.initialize();
        DisconnectHandler.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                AutoFishingFeature.tick();
            }
        });
    }
}