package rohan.fishmaster.handler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import rohan.fishmaster.feature.AutoFishingFeature;

public class DisconnectHandler {

    public static void initialize() {
        ClientPlayConnectionEvents.DISCONNECT.register(DisconnectHandler::onDisconnect);
    }

    private static void onDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        AutoFishingFeature.onDisconnect();
    }
}
