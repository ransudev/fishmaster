package rohan.fishmaster.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;

public class ClientTickHandler {

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        // Handle auto fishing toggle - use the correct keybinding from config package
        if (KeyBindings.TOGGLE_AUTO_FISHING.wasPressed()) {
            AutoFishingFeature.toggle();
        }

        // Handle emergency stop
        if (KeyBindings.EMERGENCY_STOP.wasPressed()) {
            AutoFishingFeature.emergencyStop();
        }
    }
}
