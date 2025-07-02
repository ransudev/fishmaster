package rohan.fishmaster.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import rohan.fishmaster.feature.AutoFishingFeature;

import java.util.Random;

public class AutoFishingRenderer {
    private static final Random random = new Random();
    private static float targetOffsetX = 0.0f;
    private static float targetOffsetY = 0.0f;
    private static long lastMovementUpdate = 0;
    private static boolean returningToCenter = false;
    private static boolean movementActive = false;
    private static final int MOVEMENT_UPDATE_INTERVAL = 3000;
    private static final float MAX_OFFSET = 4.0f;
    private static final int RETURN_TO_CENTER_TIME = 1000;

    public static void renderAutoFishingIndicator(DrawContext context, float tickDelta) {
        if (!AutoFishingFeature.isEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        updateActualCrosshairMovement();
    }

    private static void updateActualCrosshairMovement() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse == null) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMovementUpdate >= MOVEMENT_UPDATE_INTERVAL && !movementActive) {
            targetOffsetX = (random.nextFloat() - 0.5f) * 2.0f * MAX_OFFSET;
            targetOffsetY = (random.nextFloat() - 0.5f) * 2.0f * MAX_OFFSET;

            simulateMouseMovement(targetOffsetX, targetOffsetY);

            movementActive = true;
            returningToCenter = false;
            lastMovementUpdate = currentTime;
        }

        if (currentTime - lastMovementUpdate >= RETURN_TO_CENTER_TIME && movementActive && !returningToCenter) {
            returningToCenter = true;

            simulateMouseMovement(-targetOffsetX, -targetOffsetY);

            movementActive = false;
            targetOffsetX = 0.0f;
            targetOffsetY = 0.0f;
            lastMovementUpdate = currentTime;
        }
    }

    private static void simulateMouseMovement(float deltaX, float deltaY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse == null || client.player == null) return;

        double mouseSensitivity = client.options.getMouseSensitivity().getValue();

        double mouseX = deltaX * 0.15 / mouseSensitivity;
        double mouseY = deltaY * 0.15 / mouseSensitivity;

        if (client.player != null) {
            client.player.changeLookDirection(mouseX, mouseY);
        }
    }

    public static void reset() {
        targetOffsetX = 0.0f;
        targetOffsetY = 0.0f;
        returningToCenter = false;
        movementActive = false;
        lastMovementUpdate = 0;
    }
}
