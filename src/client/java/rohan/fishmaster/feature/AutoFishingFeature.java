package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Formatting;
import rohan.fishmaster.render.AutoFishingRenderer;
import rohan.fishmaster.config.FishMasterConfig;

public class AutoFishingFeature {
    private static boolean enabled = false;
    private static boolean isFishing = false;
    private static int delayTimer = 0;
    private static int castCooldownTimer = 0;
    private static long lastDetectionTime = 0;
    private static boolean mouseWasGrabbed = false;

    // Anti-AFK variables
    private static boolean antiAfkEnabled = true; // Passive when mod starts
    private static int antiAfkTimer = 0;
    private static float lastYaw = 0.0f;
    private static float lastPitch = 0.0f;
    private static float targetYaw = 0.0f;
    private static float targetPitch = 0.0f;
    private static boolean isMovingToTarget = false;
    private static int smoothSteps = 0;
    private static final int SMOOTH_DURATION = 20; // Smooth over 20 ticks (1 second)
    private static final java.util.Random random = new java.util.Random();

    // Original position tracking for anti-AFK
    private static float originalYaw = 0.0f;
    private static float originalPitch = 0.0f;
    private static int movementCount = 0;
    private static final int RETURN_TO_ORIGIN_INTERVAL = 5; // Return to original position every 5 movements

    // Failsafe variables
    private static long lastSuccessfulFish = System.currentTimeMillis();
    private static int consecutiveFailures = 0;
    private static boolean emergencyStop = false;
    private static long sessionStartTime = 0;
    private static final long HEALTH_CHECK_INTERVAL = 5000; // 5 seconds
    private static long lastHealthCheck = 0;
    private static boolean serverConnectionLost = false;

    // Player movement detection for failsafe
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    private static boolean playerPositionInitialized = false;
    private static final double MOVEMENT_THRESHOLD = 3.0; // Player moved more than 3 blocks

    private enum FishingState {
        IDLE,
        CASTING,
        FISHING
    }

    private static FishingState currentState = FishingState.IDLE;

    private static final long DETECTION_COOLDOWN = 50;
    private static final int BOBBER_SETTLE_TIME = 3;

    // Anti-AFK constants
    private static final int ANTI_AFK_INTERVAL = 120; // Move every 6 seconds (120 ticks)
    private static final float CROSSHAIR_MOVEMENT_RANGE = 2.5f; // Decreased movement range (was 5.0f)

    static {
        // Initialize fishing events
        rohan.fishmaster.event.FishingEvents.register();
        rohan.fishmaster.event.ChatEvents.register();
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            stop();
            AutoFishingRenderer.reset();
            restoreMouseGrab();
            // Disable fishing tracker when mod is disabled
            FishMasterConfig.setFishingTrackerEnabled(false);
            emergencyStop = false;
        } else {
            if (!performPreStartChecks()) {
                enabled = false;
                return;
            }
            switchToFishingRod();
            ungrabMouse();
            // Initialize anti-AFK when mod starts
            initializeAntiAfk();
            // Enable fishing tracker when mod is enabled
            FishMasterConfig.setFishingTrackerEnabled(true);
            sessionStartTime = System.currentTimeMillis();
            lastSuccessfulFish = sessionStartTime;
            consecutiveFailures = 0;
            emergencyStop = false;
            serverConnectionLost = false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (enabled) {
                Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
                Text message = Text.literal("Auto fishing enabled (mouse ungrabbed, anti-AFK active)").formatted(Formatting.GREEN);
                client.player.sendMessage(prefix.copy().append(message), false);
            } else {
                Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
                Text message = Text.literal("Auto fishing disabled (mouse restored, anti-AFK disabled)").formatted(Formatting.RED);
                client.player.sendMessage(prefix.copy().append(message), false);
            }
        }
    }

    private static boolean performPreStartChecks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            sendFailsafeMessage("Cannot start: Player or world is null", true);
            return false;
        }

        if (FishMasterConfig.isHealthChecksEnabled() &&
            client.player.getHealth() <= FishMasterConfig.getMinHealthThreshold()) {
            sendFailsafeMessage("Cannot start: Player health too low (≤" +
                (FishMasterConfig.getMinHealthThreshold() / 2) + " hearts)", true);
            return false;
        }

        if (!hasValidFishingRod()) {
            sendFailsafeMessage("Cannot start: No fishing rod found in inventory", true);
            return false;
        }

        if (client.getNetworkHandler() == null) {
            sendFailsafeMessage("Cannot start: No network connection", true);
            return false;
        }

        // Initialize player position tracking
        lastPlayerX = client.player.getX();
        lastPlayerY = client.player.getY();
        lastPlayerZ = client.player.getZ();
        playerPositionInitialized = true;

        return true;
    }

    private static void initializeAntiAfk() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            lastYaw = client.player.getYaw();
            lastPitch = client.player.getPitch();
            // Store original position
            originalYaw = lastYaw;
            originalPitch = lastPitch;
            antiAfkTimer = 0;
            // Use config setting instead of hardcoded value
            antiAfkEnabled = FishMasterConfig.isAntiAfkEnabled();
            movementCount = 0;
        }
    }

    private static boolean hasValidFishingRod() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        // Check main hand and offhand
        ItemStack mainHand = client.player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = client.player.getStackInHand(Hand.OFF_HAND);

        if ((mainHand.getItem() instanceof FishingRodItem && !mainHand.isEmpty()) ||
            (offHand.getItem() instanceof FishingRodItem && !offHand.isEmpty())) {
            return true;
        }

        // Check inventory for fishing rods
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FishingRodItem && !stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static void ungrabMouse() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse != null) {
            mouseWasGrabbed = client.mouse.isCursorLocked();
            client.mouse.unlockCursor();
        }
    }

    private static void restoreMouseGrab() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse != null) {
            if (mouseWasGrabbed && !enabled) {
                client.mouse.lockCursor();
            }
        }
    }

    public static void ensureMouseUngrabbedIfEnabled() {
        if (enabled) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.mouse != null && client.mouse.isCursorLocked()) {
                client.mouse.unlockCursor();
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isFishing() {
        return isFishing;
    }

    public static void tick() {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            emergencyStopWithReason("Player or world became null");
            return;
        }

        // Check for player movement (possible admin teleport or manual movement)
        if (FishMasterConfig.isPauseOnPlayerMovementEnabled() && playerPositionInitialized) {
            double currentX = client.player.getX();
            double currentY = client.player.getY();
            double currentZ = client.player.getZ();

            double distance = Math.sqrt(
                Math.pow(currentX - lastPlayerX, 2) +
                Math.pow(currentY - lastPlayerY, 2) +
                Math.pow(currentZ - lastPlayerZ, 2)
            );

            if (distance > MOVEMENT_THRESHOLD) {
                emergencyStopWithReason("Player moved significantly - possible manual movement or teleport");
                return;
            }
        }

        // Perform periodic health checks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHealthCheck >= HEALTH_CHECK_INTERVAL) {
            if (!performHealthCheck()) {
                return; // Emergency stop was triggered
            }
            lastHealthCheck = currentTime;
        }

        // Check for emergency stop conditions
        if (emergencyStop) {
            stop();
            return;
        }

        // Anti-AFK crosshair movement
        handleAntiAfk(client);

        ItemStack mainHand = client.player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = client.player.getStackInHand(Hand.OFF_HAND);

        boolean hasFishingRod = mainHand.getItem() instanceof FishingRodItem ||
                               offHand.getItem() instanceof FishingRodItem;

        if (!hasFishingRod) {
            if (!switchToFishingRod()) {
                enabled = false;
                resetFishingState();
                return;
            }
        }

        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        if (castCooldownTimer > 0) {
            castCooldownTimer--;
        }

        switch (currentState) {
            case IDLE:
                if (client.player.fishHook == null) {
                    startCasting(client);
                } else {
                    if (hasBobberInWater(client.player)) {
                        currentState = FishingState.FISHING;
                        isFishing = true;
                    }
                }
                break;

            case CASTING:
                if (client.player.fishHook != null && hasBobberInWater(client.player)) {
                    currentState = FishingState.FISHING;
                    isFishing = true;
                }
                break;

            case FISHING:
                if (client.player.fishHook == null) {
                    resetFishingState();
                    delayTimer = 20;
                } else if (hasBobberInWater(client.player) && detectArmorStandFishBite(client)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastDetectionTime >= DETECTION_COOLDOWN) {
                        lastDetectionTime = currentTimeMillis;
                        performSingleClick(client);
                        resetFishingState();
                        delayTimer = 10;
                    }
                }
                break;
        }

        // Check session time limit
        if (currentTime - sessionStartTime >= FishMasterConfig.getMaxSessionTime()) {
            emergencyStopWithReason("Maximum session time reached (" +
                (FishMasterConfig.getMaxSessionTime() / 60000) + " minutes)");
            return;
        }

        // Check if we haven't caught anything in too long
        if (currentTime - lastSuccessfulFish >= FishMasterConfig.getMaxIdleTime()) {
            emergencyStopWithReason("No fish caught for " +
                (FishMasterConfig.getMaxIdleTime() / 60000) + " minutes - possible detection");
            return;
        }

        // Check consecutive failures
        if (consecutiveFailures >= FishMasterConfig.getMaxConsecutiveFailures()) {
            emergencyStopWithReason("Too many consecutive fishing failures (" +
                consecutiveFailures + "/" + FishMasterConfig.getMaxConsecutiveFailures() + ")");
            return;
        }
    }

    private static boolean performHealthCheck() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Skip health checks if disabled in config
        if (!FishMasterConfig.isHealthChecksEnabled()) {
            return true;
        }

        // Check player health
        if (client.player.getHealth() <= (FishMasterConfig.getMinHealthThreshold() - 2.0f)) {
            emergencyStopWithReason("Player health critically low (≤" +
                ((FishMasterConfig.getMinHealthThreshold() - 2.0f) / 2) + " hearts)");
            return false;
        }

        // Check if player is drowning
        if (client.player.isSubmergedInWater() && client.player.getAir() < 100) {
            emergencyStopWithReason("Player is drowning");
            return false;
        }

        // Check if player is in lava
        if (client.player.isInLava()) {
            emergencyStopWithReason("Player is in lava");
            return false;
        }

        // Check if player is on fire
        if (client.player.isOnFire()) {
            emergencyStopWithReason("Player is on fire");
            return false;
        }

        // Check if player is falling from high height
        if (client.player.getVelocity().y < -0.5 && client.player.getY() > 100) {
            emergencyStopWithReason("Player is falling from dangerous height");
            return false;
        }

        // Check network connection
        if (client.getNetworkHandler() == null || serverConnectionLost) {
            emergencyStopWithReason("Lost connection to server");
            return false;
        }

        // Check if fishing rod is still available and not broken
        if (!hasValidFishingRod()) {
            emergencyStopWithReason("No valid fishing rod available");
            return false;
        }

        // Check if player moved too far (possible teleportation or admin intervention)
        if (sessionStartTime > 0) {
            if (client.player.getY() < -64 || client.player.getY() > 320) {
                emergencyStopWithReason("Player position out of bounds");
                return false;
            }
        }

        // Check for suspicious game state changes
        if (client.isPaused()) {
            emergencyStopWithReason("Game is paused");
            return false;
        }

        return true;
    }

    private static void emergencyStopWithReason(String reason) {
        emergencyStop = true;
        enabled = false;
        stop();
        sendFailsafeMessage("EMERGENCY STOP: " + reason, true);
        restoreMouseGrab();
    }

    private static void sendFailsafeMessage(String message, boolean isError) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
            Text content = Text.literal(message).formatted(isError ? Formatting.RED : Formatting.YELLOW);
            client.player.sendMessage(prefix.copy().append(content), false);
        }
    }

    private static void startCasting(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                   Hand.MAIN_HAND : Hand.OFF_HAND;

        client.interactionManager.interactItem(client.player, hand);

        currentState = FishingState.CASTING;
        delayTimer = BOBBER_SETTLE_TIME;
        isFishing = false;
    }

    private static void resetFishingState() {
        currentState = FishingState.IDLE;
        isFishing = false;
        castCooldownTimer = 0;
    }

    private static void stop() {
        resetFishingState();
        delayTimer = 0;
        lastDetectionTime = 0;
        // Disable anti-AFK when stopping
        antiAfkEnabled = false;
        antiAfkTimer = 0;
        // Reset smooth movement variables
        isMovingToTarget = false;
        smoothSteps = 0;
    }

    public static void onDisconnect() {
        serverConnectionLost = true;
        enabled = false;
        stop();
        restoreMouseGrab();
        sendFailsafeMessage("Disconnected from server - auto fishing stopped", false);
    }

    public static void onServerSwitch() {
        emergencyStopWithReason("Server change detected");
    }

    public static void onDimensionChange() {
        emergencyStopWithReason("Dimension change detected");
    }

    // Method to be called when player takes damage
    public static void onPlayerDamage() {
        if (enabled) {
            sendFailsafeMessage("Player took damage - monitoring health", false);
        }
    }

    // Method to manually trigger emergency stop (for keybind)
    public static void emergencyStop() {
        if (enabled) {
            emergencyStopWithReason("Manual emergency stop triggered");
        }
    }

    private static void performSingleClick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            consecutiveFailures++;
            return;
        }

        try {
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            lastSuccessfulFish = System.currentTimeMillis();
            consecutiveFailures = 0; // Reset failure count on successful interaction
        } catch (Exception e) {
            consecutiveFailures++;
            sendFailsafeMessage("Failed to interact with fishing rod: " + e.getMessage(), false);
        }
    }

    private static boolean detectArmorStandFishBite(MinecraftClient client) {
        if (client.player == null || client.world == null || !hasBobberInWater(client.player)) {
            return false;
        }

        var entities = client.world.getEntities();
        if (entities == null) return false;

        for (Entity entity : entities) {
            if (entity instanceof ArmorStandEntity armorStand) {
                if (armorStand.hasCustomName()) {
                    Text customName = armorStand.getCustomName();
                    if (customName != null) {
                        String nameString = customName.getString();
                        if ("!!!".equals(nameString)) {
                            double distance = armorStand.squaredDistanceTo(client.player);
                            if (distance <= 50 * 50) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void handleAntiAfk(MinecraftClient client) {
        if (!antiAfkEnabled || client.player == null) return;

        // Handle smooth movement to target position
        if (isMovingToTarget) {
            smoothSteps++;
            float progress = (float) smoothSteps / SMOOTH_DURATION;

            if (progress >= 1.0f) {
                // Movement complete
                client.player.setYaw(targetYaw);
                client.player.setPitch(targetPitch);
                lastYaw = targetYaw;
                lastPitch = targetPitch;
                isMovingToTarget = false;
                smoothSteps = 0;

                // Increment movement count after completing a movement
                movementCount++;
            } else {
                // Smooth interpolation using easing function
                float easedProgress = easeInOutSine(progress);
                float currentYaw = lerp(lastYaw, targetYaw, easedProgress);
                float currentPitch = lerp(lastPitch, targetPitch, easedProgress);

                client.player.setYaw(currentYaw);
                client.player.setPitch(currentPitch);
            }
            return;
        }

        antiAfkTimer++;

        if (antiAfkTimer >= ANTI_AFK_INTERVAL) {
            antiAfkTimer = 0;

            // Check if it's time to return to original position
            if (movementCount >= RETURN_TO_ORIGIN_INTERVAL) {
                movementCount = 0;
                targetYaw = originalYaw;
                targetPitch = originalPitch;
            } else {
                // Generate random movement near original position
                float maxDistanceFromOrigin = CROSSHAIR_MOVEMENT_RANGE * 1.5f;

                // Calculate random offset but keep it within range of original position
                float yawOffset = random.nextFloat() * CROSSHAIR_MOVEMENT_RANGE * 2 - CROSSHAIR_MOVEMENT_RANGE;
                float pitchOffset = random.nextFloat() * (CROSSHAIR_MOVEMENT_RANGE * 1.2f) - (CROSSHAIR_MOVEMENT_RANGE * 0.6f);

                // Calculate potential new position
                float potentialYaw = originalYaw + yawOffset;
                float potentialPitch = originalYaw + pitchOffset;

                // Clamp to stay near original position
                float yawDistanceFromOrigin = Math.abs(potentialYaw - originalYaw);
                float pitchDistanceFromOrigin = Math.abs(potentialPitch - originalPitch);

                if (yawDistanceFromOrigin > maxDistanceFromOrigin) {
                    yawOffset = yawOffset > 0 ? maxDistanceFromOrigin : -maxDistanceFromOrigin;
                }
                if (pitchDistanceFromOrigin > maxDistanceFromOrigin) {
                    pitchOffset = pitchOffset > 0 ? maxDistanceFromOrigin : -maxDistanceFromOrigin;
                }

                targetYaw = originalYaw + yawOffset;
                targetPitch = Math.max(-90.0f, Math.min(90.0f, originalPitch + pitchOffset));
            }

            // Start smooth movement
            isMovingToTarget = true;
            smoothSteps = 0;
        }
    }

    // Linear interpolation function
    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    // Smooth easing function for natural movement
    private static float easeInOutSine(float x) {
        return (float) (-(Math.cos(Math.PI * x) - 1) / 2);
    }
    private static boolean switchToFishingRod() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return false;

        ItemStack mainHand = client.player.getStackInHand(Hand.MAIN_HAND);
        if (mainHand.getItem() instanceof FishingRodItem) {
            return true;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FishingRodItem) {
                client.player.getInventory().setSelectedSlot(i);
                return true;
            }
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FishingRodItem) {
                int currentSlot = client.player.getInventory().getSelectedSlot();

                client.interactionManager.clickSlot(
                    client.player.playerScreenHandler.syncId,
                    i,
                    currentSlot,
                    net.minecraft.screen.slot.SlotActionType.SWAP,
                    client.player
                );

                return true;
            }
        }

        return false;
    }

    private static boolean hasBobberInWater(PlayerEntity player) {
        FishingBobberEntity bobber = player.fishHook;
        if (bobber == null) {
            return false;
        }
        return bobber.isTouchingWater();
    }
}
