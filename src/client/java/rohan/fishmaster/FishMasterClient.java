package rohan.fishmaster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.handler.ClientTickHandler;
import rohan.fishmaster.handler.DisconnectHandler;
import rohan.fishmaster.config.KeyBindings;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.command.FishMasterCommand;
import net.minecraft.client.MinecraftClient;

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

            // Initialize pause menu integration with more robust button addition
            initializePauseMenuIntegration();

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
            FishMasterConfig.setFishingTrackerEnabled(true);

            initialized = true;
            System.out.println("[FishMaster] Core mod initialized successfully!");

        } catch (Exception e) {
            System.err.println("[FishMaster] Failed to initialize core mod: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializePauseMenuIntegration() {
        // More robust GUI button integration
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen pauseMenu) {
                addFishMasterButtonRobust(pauseMenu);
            }
        });
    }

    private void addFishMasterButtonRobust(GameMenuScreen pauseMenu) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            // Calculate button position (right side of the pause menu)
            int buttonWidth = 98;
            int buttonHeight = 20;
            int centerX = pauseMenu.width / 2;
            int centerY = pauseMenu.height / 4;

            // Position the button to the right of existing buttons
            int buttonX = centerX + 105; // Position to the right of vanilla buttons
            int buttonY = centerY + 48;  // Align with "Options" button

            // Create the FishMaster button
            ButtonWidget fishMasterButton = ButtonWidget.builder(
                Text.literal("FishMaster").formatted(Formatting.AQUA),
                button -> {
                    // Show status instead of opening GUI
                    if (client.player != null) {
                        boolean autoFishStatus = rohan.fishmaster.feature.AutoFishingFeature.isEnabled();
                        boolean seaCreatureStatus = rohan.fishmaster.feature.SeaCreatureKiller.isEnabled();

                        client.player.sendMessage(
                            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                                .append(Text.literal("Status:").formatted(Formatting.WHITE)), false);

                        client.player.sendMessage(
                            Text.literal("Auto Fishing: ").formatted(Formatting.GRAY)
                                .append(Text.literal(autoFishStatus ? "ON" : "OFF")
                                    .formatted(autoFishStatus ? Formatting.GREEN : Formatting.RED)), false);

                        client.player.sendMessage(
                            Text.literal("Sea Creature Killer: ").formatted(Formatting.GRAY)
                                .append(Text.literal(seaCreatureStatus ? "ON" : "OFF")
                                    .formatted(seaCreatureStatus ? Formatting.GREEN : Formatting.RED)), false);
                    }
                })
                .dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
                .build();

            // Multiple fallback methods to add the button using reflection
            boolean buttonAdded = false;

            // Method 1: Try reflection on addDrawableChild
            try {
                java.lang.reflect.Method addMethod = pauseMenu.getClass()
                    .getSuperclass().getDeclaredMethod("addDrawableChild",
                    net.minecraft.client.gui.Element.class);
                addMethod.setAccessible(true);
                addMethod.invoke(pauseMenu, fishMasterButton);
                buttonAdded = true;
            } catch (Exception e) {
                // Method 2: Try accessing drawable list directly
                try {
                    java.lang.reflect.Field drawablesField = pauseMenu.getClass()
                        .getSuperclass().getDeclaredField("drawables");
                    drawablesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<net.minecraft.client.gui.Drawable> drawables =
                        (java.util.List<net.minecraft.client.gui.Drawable>) drawablesField.get(pauseMenu);
                    drawables.add(fishMasterButton);

                    java.lang.reflect.Field selectablesField = pauseMenu.getClass()
                        .getSuperclass().getDeclaredField("selectables");
                    selectablesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<net.minecraft.client.gui.Element> selectables =
                        (java.util.List<net.minecraft.client.gui.Element>) selectablesField.get(pauseMenu);
                    selectables.add(fishMasterButton);

                    buttonAdded = true;
                } catch (Exception e2) {
                    System.err.println("[FishMaster] All methods failed to add button: " + e2.getMessage());
                }
            }

            if (buttonAdded) {
                System.out.println("[FishMaster] GUI button added successfully!");
            }

        } catch (Exception e) {
            System.err.println("[FishMaster] Failed to add FishMaster button: " + e.getMessage());
        }
    }

    // Static method to check if mod is initialized
    public static boolean isInitialized() {
        return initialized;
    }
}