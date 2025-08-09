package rohan.fishmaster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.handler.ClientTickHandler;
import rohan.fishmaster.handler.DisconnectHandler;
import rohan.fishmaster.handler.WebhookHandler;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.command.FishMasterCommand;

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

            // Enable fishing tracker by default - it's now ALWAYS active
            FishMasterConfig.setFishingTrackerEnabled(true);

            // Initialize keybindings FIRST - this is critical for preventing the crash
            KeyBindings.register();

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

            // Ensure fishing tracker is always enabled
            FishMasterConfig.setFishingTrackerEnabled(true);

            initialized = true;
            System.out.println("[FishMaster] Core mod initialized successfully with responsive scheduler!");

        } catch (Exception e) {
            System.err.println("[FishMaster] Failed to initialize core mod: " + e.getMessage());
            e.printStackTrace();
        }
    }
}