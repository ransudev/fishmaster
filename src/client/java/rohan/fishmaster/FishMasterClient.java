package rohan.fishmaster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.handler.ClientTickHandler;
import rohan.fishmaster.handler.DisconnectHandler;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.command.FishMasterCommand;
import rohan.fishmaster.gui.PauseMenuIntegration;
import rohan.fishmaster.gui.FishMasterSettings;

public class FishMasterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
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

        // Initialize pause menu integration
        PauseMenuIntegration.initialize();

        // Initialize fishing events after keybindings are registered
        rohan.fishmaster.event.FishingEvents.register();

        // Initialize other handlers
        ClientTickHandler.initialize();
        DisconnectHandler.initialize();

        // Register the main tick event for auto fishing
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                AutoFishingFeature.tick();
                rohan.fishmaster.feature.SeaCreatureKiller.tick();
            }
        });

        // Ensure fishing tracker is always enabled
        FishMasterSettings settings = FishMasterSettings.getInstance();
        settings.fishingTrackerEnabled = true;
        settings.saveSettings();
    }
}