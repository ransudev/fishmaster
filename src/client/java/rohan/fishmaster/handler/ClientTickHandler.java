package rohan.fishmaster.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.SeaCreatureKiller;
import rohan.fishmaster.feature.FishingFailsafe;
import rohan.fishmaster.core.ResponsiveScheduler;
import rohan.fishmaster.core.ResponsiveScheduler.Priority;

public class ClientTickHandler {

    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);

        // Initialize responsive scheduler with prioritized tasks
        ResponsiveScheduler scheduler = ResponsiveScheduler.getInstance();

        // CRITICAL: Input handling (every tick)
        scheduler.scheduleRepeating("input_handling", ClientTickHandler::handleInputs, Priority.CRITICAL, 1);

        // HIGH: Core fishing logic (every tick when fishing)
        scheduler.scheduleRepeating("auto_fishing", AutoFishingFeature::tick, Priority.HIGH, 1);
        scheduler.scheduleRepeating("fishing_failsafe", FishingFailsafe::checkFailsafes, Priority.HIGH, 1);

        // MEDIUM: Sea creature killer (every 2 ticks)
        scheduler.scheduleRepeating("sea_creature_killer", SeaCreatureKiller::tick, Priority.MEDIUM, 2);

        // LOW: Statistics and tracking (every 20 ticks / 1 second)
        scheduler.scheduleRepeating("fishing_tracker", () -> {
            // Fishing tracker updates
        }, Priority.LOW, 20);

        // BACKGROUND: Webhook health checks (every 100 ticks / 5 seconds)
        scheduler.scheduleRepeating("webhook_health", () -> {
            rohan.fishmaster.handler.WebhookHandler.getInstance().performHealthCheck();
        }, Priority.BACKGROUND, 100);

        initialized = true;
    }

    private static void onClientTick(MinecraftClient client) {
        // Use responsive scheduler instead of direct execution
        ResponsiveScheduler.getInstance().tick();
    }

    private static void handleInputs() {
        // Auto fishing toggle is now handled by GUI only, not by keybind

        // Handle emergency stop
        if (KeyBindings.EMERGENCY_STOP.wasPressed()) {
            AutoFishingFeature.emergencyStop();
        }

        // Sea Creature Killer toggle is now handled via GUI instead of keybinding
    }
}
