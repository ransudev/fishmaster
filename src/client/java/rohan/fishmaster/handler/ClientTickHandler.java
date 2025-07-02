package rohan.fishmaster.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import rohan.fishmaster.keybind.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;

public class ClientTickHandler {

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        // Handle auto fishing toggle
        if (KeyBindings.toggleAutoFishing.wasPressed()) {
            AutoFishingFeature.toggle();
        }
    }
}
