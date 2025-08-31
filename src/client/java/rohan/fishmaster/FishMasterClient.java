package rohan.fishmaster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.handler.ClientTickHandler;
import rohan.fishmaster.handler.DisconnectHandler;
import rohan.fishmaster.handler.WebhookHandler;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.command.FishMasterCommand;
import rohan.fishmaster.util.TickScheduler;

public class FishMasterClient implements ClientModInitializer {

    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        if (initialized) {
            return; // Prevent double initialization
        }

        try {
            // Load config first
            FishMasterConfig.load();

            // Initialize keybindings FIRST - this is critical for preventing the crash
            KeyBindings.register();

            // Initialize GUI-only keybind handler for auto-fishing
            rohan.fishmaster.handler.GuiKeybindHandler.initialize();

            // Register the /fm command
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                FishMasterCommand.register(dispatcher);
            });

            // Initialize fishing events after keybindings are registered
            rohan.fishmaster.event.FishingEvents.register();

            // Initialize other handlers
            ClientTickHandler.initialize();
            DisconnectHandler.initialize();
            WebhookHandler.getInstance().initialize();

            // Add shutdown hook for webhook handler
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                WebhookHandler.getInstance().shutdown();
            }));

            // Register the main tick event for auto fishing
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                TickScheduler.tick(); // Process scheduled tasks
                if (client.player != null && client.world != null) {
                    AutoFishingFeature.tick();
                    rohan.fishmaster.feature.SeaCreatureKiller.tick();
                }
            });

            initialized = true;
            System.out.println("[FishMaster] Core mod initialized successfully!");

        } catch (Exception e) {
            System.err.println("[FishMaster] Failed to initialize core mod: " + e.getMessage());
            e.printStackTrace();
        }
    }
}