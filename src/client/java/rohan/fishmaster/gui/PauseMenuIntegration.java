package rohan.fishmaster.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PauseMenuIntegration {

    public static void initialize() {
        // Register screen event to add our button to the pause menu
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen pauseMenu) {
                addFishMasterButton(pauseMenu);
            }
        });
    }

    private static void addFishMasterButton(GameMenuScreen pauseMenu) {
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
                try {
                    // Close pause menu first
                    client.setScreen(null);

                    // Open FishMaster settings
                    FishMasterSettings settings = FishMasterSettings.getInstance();
                    settings.openGui();

                } catch (Exception e) {
                    // Send error message if something goes wrong
                    if (client.player != null) {
                        client.player.sendMessage(
                            Text.literal("[FishMaster] ").formatted(Formatting.RED)
                                .append(Text.literal("Failed to open GUI: " + e.getMessage()).formatted(Formatting.WHITE)),
                            false
                        );
                    }
                }
            })
            .dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
            .build();

        // Use reflection to access the protected addDrawableChild method
        try {
            java.lang.reflect.Method addMethod = pauseMenu.getClass()
                .getSuperclass().getDeclaredMethod("addDrawableChild",
                net.minecraft.client.gui.Element.class);
            addMethod.setAccessible(true);
            addMethod.invoke(pauseMenu, fishMasterButton);
        } catch (Exception e) {
            // Fallback: try adding to drawable list directly
            System.err.println("Failed to add FishMaster button to pause menu: " + e.getMessage());
        }
    }
}
