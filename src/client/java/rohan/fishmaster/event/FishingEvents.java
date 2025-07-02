package rohan.fishmaster.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.data.FishingData;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.FishingTracker;
import rohan.fishmaster.gui.FishingTrackerHud;

public class FishingEvents {
    public static void register() {
        // Register tick event for fishing type detection and safety checks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FishingTracker.tick();
            KeyBindings.onKey(); // Handle keybind presses
        });

        // Register modern HUD render event
        WorldRenderEvents.END.register(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                DrawContext drawContext = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());
                FishingTrackerHud.render(drawContext, 10, 10);
            }
        });

        // Register disconnect event for failsafe
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            AutoFishingFeature.onDisconnect();
        });

        // Register join event to reset failsafe state
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Reset any emergency states when joining a server
            AutoFishingFeature.onServerSwitch();
        });

        // Load fishing data and config
        FishingData.load();
        FishMasterConfig.load();
        KeyBindings.register();
    }

    public static void onGameExit() {
        FishingData.save();
        FishMasterConfig.save();
        // Ensure auto fishing is stopped on game exit
        if (AutoFishingFeature.isEnabled()) {
            AutoFishingFeature.onDisconnect();
        }
    }
}
