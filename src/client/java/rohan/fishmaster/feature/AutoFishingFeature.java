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

    // Debug mode variables
    private static boolean debugMode = false;

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

    // Recast mechanism variables
    private static int castAttempts = 0;
    private static long lastCastTime = 0;
    private static boolean waitingForBobberSettle = false;
    private static int bobberSettleTimer = 0;
    private static final int MAX_CAST_ATTEMPTS = 5;
    private static final long CAST_TIMEOUT = 3000; // 3 seconds to wait for bobber to settle
    private static final int BOBBER_SETTLE_DELAY = 40; // 2 seconds in ticks
    private static final int RECAST_DELAY = 10; // 0.5 seconds between recast attempts

    // Fishing timing variables
    private static long fishingStartTime = 0;
    private static final long MIN_FISHING_TIME = 2000; // Minimum 2 seconds before checking for fish bites

    // Dynamic recast delay getter
    private static int getRecastDelay() {
        int delay = SeaCreatureKiller.isEnabled() ? RECAST_DELAY : 1;
        sendDebugMessage("Recast delay: " + delay + " ticks (SCK " + (SeaCreatureKiller.isEnabled() ? "enabled" : "disabled") + ")");
        return delay;
    }

    // Dynamic bobber settle delay getter
    private static int getBobberSettleDelay() {
        sendDebugMessage("Bobber settle delay: " + BOBBER_SETTLE_DELAY + " ticks");
        return BOBBER_SETTLE_DELAY;
    }

    // Dynamic after-fishing delay getter - faster casting after catching fish when SCK is off
    private static int getAfterFishingDelay() {
        int delay = SeaCreatureKiller.isEnabled() ? BOBBER_SETTLE_DELAY : 20;
        sendDebugMessage("After-fishing delay: " + delay + " ticks (SCK " + (SeaCreatureKiller.isEnabled() ? "enabled" : "disabled") + ")");
        return delay;
    }

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
        // Initialize fishing events - but don't register keybindings here
        // KeyBindings are now registered in FishMasterClient.onInitializeClient()
    }

    public static void toggle() {
        enabled = !enabled;
        sendDebugMessage("Auto fishing toggled: " + (enabled ? "ENABLED" : "DISABLED"));

        if (!enabled) {
            sendDebugMessage("Stopping auto fishing - cleaning up resources");
            stop();
            AutoFishingRenderer.reset();
            restoreMouseGrab();
            // Keep fishing tracker always enabled - don't disable it
            emergencyStop = false;
            // Deactivate sea creature killer when auto fishing stops
            SeaCreatureKiller.setAutoFishEnabled(false);
            sendDebugMessage("Auto fishing stopped - SCK disabled, tracker remains active");
        } else {
            sendDebugMessage("Starting auto fishing - performing pre-start checks");
            if (!performPreStartChecks()) {
                enabled = false;
                sendDebugMessage("Pre-start checks failed - auto fishing disabled");
                return;
            }
            sendDebugMessage("Pre-start checks passed - initializing systems");
            switchToFishingRod();
            ungrabMouse();
            // Initialize anti-AFK when mod starts
            initializeAntiAfk();
            // Ensure fishing tracker is always enabled
            FishMasterConfig.setFishingTrackerEnabled(true);
            sessionStartTime = System.currentTimeMillis();
            lastSuccessfulFish = sessionStartTime;
            consecutiveFailures = 0;
            emergencyStop = false;
            serverConnectionLost = false;
            // Enable sea creature killer availability when auto fishing starts
            SeaCreatureKiller.setAutoFishEnabled(true);
            sendDebugMessage("Auto fishing started - Session time: " + sessionStartTime + ", SCK enabled, tracker active");
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (enabled) {
                Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
                Text message = Text.literal("Auto fishing enabled (mouse ungrabbed, anti-AFK active, tracker always on)").formatted(Formatting.GREEN);
                client.player.sendMessage(prefix.copy().append(message), false);
            } else {
                Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
                Text message = Text.literal("Auto fishing disabled (mouse restored, anti-AFK disabled, tracker remains on)").formatted(Formatting.RED);
                client.player.sendMessage(prefix.copy().append(message), false);
            }
        }
    }

    private static boolean performPreStartChecks() {
        MinecraftClient client = MinecraftClient.getInstance();
        sendDebugMessage("Starting pre-start checks...");

        if (client.player == null || client.world == null) {
            sendDebugMessage("Pre-start check failed: Player or world is null");
            sendFailsafeMessage("Cannot start: Player or world is null", true);
            return false;
        }

        if (FishMasterConfig.isHealthChecksEnabled() &&
            client.player.getHealth() <= FishMasterConfig.getMinHealthThreshold()) {
            sendDebugMessage("Pre-start check failed: Health too low (" + client.player.getHealth() + " <= " + FishMasterConfig.getMinHealthThreshold() + ")");
            sendFailsafeMessage("Cannot start: Player health too low (≤" +
                (FishMasterConfig.getMinHealthThreshold() / 2) + " hearts)", true);
            return false;
        }

        if (!hasValidFishingRod()) {
            sendDebugMessage("Pre-start check failed: No valid fishing rod found");
            sendFailsafeMessage("Cannot start: No fishing rod found in inventory", true);
            return false;
        }

        if (client.getNetworkHandler() == null) {
            sendDebugMessage("Pre-start check failed: No network connection");
            sendFailsafeMessage("Cannot start: No network connection", true);
            return false;
        }

        // Initialize player position tracking
        lastPlayerX = client.player.getX();
        lastPlayerY = client.player.getY();
        lastPlayerZ = client.player.getZ();
        playerPositionInitialized = true;
        sendDebugMessage("Pre-start checks passed - Player position initialized: (" + lastPlayerX + ", " + lastPlayerY + ", " + lastPlayerZ + ")");

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
            sendDebugMessage("Anti-AFK initialized - Enabled: " + antiAfkEnabled + ", Original position: Yaw=" + originalYaw + ", Pitch=" + originalPitch);
        }
    }

    private static boolean hasValidFishingRod() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            sendDebugMessage("hasValidFishingRod: Player is null");
            return false;
        }

        // Check main hand and offhand
        ItemStack mainHand = client.player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = client.player.getStackInHand(Hand.OFF_HAND);

        if ((mainHand.getItem() instanceof FishingRodItem && !mainHand.isEmpty()) ||
            (offHand.getItem() instanceof FishingRodItem && !offHand.isEmpty())) {
            sendDebugMessage("hasValidFishingRod: Found fishing rod in hands");
            return true;
        }

        // Check inventory for fishing rods
        int rodsFound = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FishingRodItem && !stack.isEmpty()) {
                rodsFound++;
            }
        }

        sendDebugMessage("hasValidFishingRod: Found " + rodsFound + " fishing rods in inventory");
        return rodsFound > 0;
    }

    private static void ungrabMouse() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse != null) {
            mouseWasGrabbed = client.mouse.isCursorLocked();
            client.mouse.unlockCursor();
            sendDebugMessage("Mouse ungrabbed - Previously grabbed: " + mouseWasGrabbed);
        }
    }

    private static void restoreMouseGrab() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse != null) {
            if (mouseWasGrabbed && !enabled) {
                client.mouse.lockCursor();
                sendDebugMessage("Mouse grab restored");
            } else {
                sendDebugMessage("Mouse grab not restored - Was grabbed: " + mouseWasGrabbed + ", Enabled: " + enabled);
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
            sendDebugMessage("Tick: Player or world became null - triggering emergency stop");
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
                sendDebugMessage("Player movement detected - Distance: " + String.format("%.2f", distance) + " blocks (threshold: " + MOVEMENT_THRESHOLD + ")");
                emergencyStopWithReason("Player moved significantly - possible manual movement or teleport");
                return;
            }
        }

        // Perform periodic health checks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHealthCheck >= HEALTH_CHECK_INTERVAL) {
            sendDebugMessage("Performing periodic health check...");
            if (!performHealthCheck()) {
                return; // Emergency stop was triggered
            }
            lastHealthCheck = currentTime;
            sendDebugMessage("Health check passed");
        }

        // Check for emergency stop conditions
        if (emergencyStop) {
            sendDebugMessage("Emergency stop flag detected - stopping");
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
            // Only show error and disable if Sea Creature Killer is not active (to avoid spam during weapon swapping)
            if (!SeaCreatureKiller.isEnabled()) {
                sendDebugMessage("No fishing rod in hands - stopping auto fishing");
                sendFailsafeMessage("Auto fishing stopped: No fishing rod in hands", true);
                enabled = false;
                resetFishingState();
            } else {
                sendDebugMessage("No fishing rod in hands but SCK is active - pausing auto fishing");
                // Don't disable the macro when SCK is active, just pause fishing logic
                resetFishingState();
            }
            return;
        }

        if (delayTimer > 0) {
            delayTimer--;
            if (delayTimer % 20 == 0) { // Debug message every second
                sendDebugMessage("Delay timer: " + delayTimer + " ticks remaining");
            }
            return;
        }

        if (castCooldownTimer > 0) {
            castCooldownTimer--;
            if (castCooldownTimer % 20 == 0) { // Debug message every second
                sendDebugMessage("Cast cooldown timer: " + castCooldownTimer + " ticks remaining");
            }
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
                    fishingStartTime = System.currentTimeMillis(); // Record when fishing started
                    sendDebugMessage("Bobber in water - State: CASTING → FISHING, starting fishing timer");
                } else {
                    // Handle recast mechanism if bobber is not detected
                    handleRecastMechanism(client);
                }
                break;

            case FISHING:
                if (client.player.fishHook == null) {
                    sendDebugMessage("Bobber disappeared - resetting state");
                    resetFishingState();
                    delayTimer = 20;
                } else if (hasBobberInWater(client.player)) {
                    // Only check for fish bites after minimum fishing time has passed
                    long timeFishing = System.currentTimeMillis() - fishingStartTime;
                    if (timeFishing >= MIN_FISHING_TIME && detectArmorStandFishBite(client)) {
                        long currentTimeMillis = System.currentTimeMillis();
                        if (currentTimeMillis - lastDetectionTime >= DETECTION_COOLDOWN) {
                            lastDetectionTime = currentTimeMillis;
                            sendDebugMessage("Fish detected after " + timeFishing + "ms of fishing! Reeling in...");
                            performSingleClick(client);
                            // After catching a fish, reset to idle state and wait for bobber to settle
                            resetFishingState();
                            delayTimer = getAfterFishingDelay(); // Use faster delay when SCK is off
                            sendDebugMessage("Fish caught - waiting " + getAfterFishingDelay() + " ticks before next cast");
                        }
                    } else if (timeFishing < MIN_FISHING_TIME) {
                        // Still waiting for minimum fishing time
                        long remainingTime = MIN_FISHING_TIME - timeFishing;
                        if (remainingTime % 1000 == 0) { // Debug message every second
                            sendDebugMessage("Waiting for fish... " + (remainingTime / 1000) + "s remaining");
                        }
                    }
                }
                break;
        }

        // Check session time limit
        if (currentTime - sessionStartTime >= FishMasterConfig.getMaxSessionTime()) {
            sendDebugMessage("Session time limit reached - Runtime: " + (currentTime - sessionStartTime) + "ms, Limit: " + FishMasterConfig.getMaxSessionTime() + "ms");
            emergencyStopWithReason("Maximum session time reached (" +
                (FishMasterConfig.getMaxSessionTime() / 60000) + " minutes)");
            return;
        }

        // Check if we haven't caught anything in too long
        if (currentTime - lastSuccessfulFish >= FishMasterConfig.getMaxIdleTime()) {
            sendDebugMessage("Idle time limit reached - Last fish: " + (currentTime - lastSuccessfulFish) + "ms ago, Limit: " + FishMasterConfig.getMaxIdleTime() + "ms");
            emergencyStopWithReason("No fish caught for " +
                (FishMasterConfig.getMaxIdleTime() / 60000) + " minutes - possible detection");
            return;
        }

        // Check consecutive failures
        if (consecutiveFailures >= FishMasterConfig.getMaxConsecutiveFailures()) {
            sendDebugMessage("Consecutive failure limit reached - Failures: " + consecutiveFailures + ", Limit: " + FishMasterConfig.getMaxConsecutiveFailures());
            emergencyStopWithReason("Too many consecutive fishing failures (" +
                consecutiveFailures + "/" + FishMasterConfig.getMaxConsecutiveFailures() + ")");
            return;
        }
    }

    private static boolean performHealthCheck() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Skip health checks if disabled in config
        if (!FishMasterConfig.isHealthChecksEnabled()) {
            sendDebugMessage("Health checks disabled in config - skipping");
            return true;
        }

        // Check player health
        float currentHealth = client.player.getHealth();
        float threshold = FishMasterConfig.getMinHealthThreshold() - 2.0f;
        if (currentHealth <= threshold) {
            sendDebugMessage("Health check failed - Current: " + currentHealth + ", Threshold: " + threshold);
            emergencyStopWithReason("Player health critically low (≤" + (threshold / 2) + " hearts)");
            return false;
        }

        // Check if player is in lava
        if (client.player.isInLava()) {
            sendDebugMessage("Health check failed - Player is in lava");
            emergencyStopWithReason("Player is in lava");
            return false;
        }

        // Check if player is on fire
        if (client.player.isOnFire()) {
            sendDebugMessage("Health check failed - Player is on fire");
            emergencyStopWithReason("Player is on fire");
            return false;
        }

        // Check if player is falling from high height
        double velocityY = client.player.getVelocity().y;
        double playerY = client.player.getY();
        if (velocityY < -0.5 && playerY > 100) {
            sendDebugMessage("Health check failed - Player falling from height - Velocity Y: " + velocityY + ", Height: " + playerY);
            emergencyStopWithReason("Player is falling from dangerous height");
            return false;
        }

        // Check network connection
        if (client.getNetworkHandler() == null || serverConnectionLost) {
            sendDebugMessage("Health check failed - Network connection lost");
            emergencyStopWithReason("Lost connection to server");
            return false;
        }

        // Check if fishing rod is still available and not broken
        if (!hasValidFishingRod()) {
            sendDebugMessage("Health check failed - No valid fishing rod available");
            emergencyStopWithReason("No valid fishing rod available");
            return false;
        }

        // Check if player moved too far (possible teleportation or admin intervention)
        if (sessionStartTime > 0) {
            if (playerY < -64 || playerY > 320) {
                sendDebugMessage("Health check failed - Player position out of bounds - Y: " + playerY);
                emergencyStopWithReason("Player position out of bounds");
                return false;
            }
        }

        // Check for suspicious game state changes
        if (client.isPaused()) {
            sendDebugMessage("Health check failed - Game is paused");
            emergencyStopWithReason("Game is paused");
            return false;
        }

        sendDebugMessage("Health check passed - Health: " + currentHealth + ", Y: " + playerY + ", On fire: " + client.player.isOnFire() + ", In lava: " + client.player.isInLava());
        return true;
    }

    private static void emergencyStopWithReason(String reason) {
        sendDebugMessage("EMERGENCY STOP triggered - Reason: " + reason);
        emergencyStop = true;
        enabled = false;
        stop();
        sendFailsafeMessage("EMERGENCY STOP: " + reason, true);
        restoreMouseGrab();
        // Disable sea creature killer when emergency stop occurs
        SeaCreatureKiller.setAutoFishEnabled(false);
        sendDebugMessage("Emergency stop complete - SCK disabled, mouse restored");
    }

    // Public method for keybind emergency stop
    public static void emergencyStop() {
        emergencyStopWithReason("Manual emergency stop via keybind");
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
        if (client.player == null || client.interactionManager == null) {
            sendDebugMessage("startCasting: Player or interaction manager is null");
            return;
        }

        Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                   Hand.MAIN_HAND : Hand.OFF_HAND;

        sendDebugMessage("Starting cast - Hand: " + hand + ", Previous state: " + currentState);
        client.interactionManager.interactItem(client.player, hand);

        currentState = FishingState.CASTING;
        // Use the proper bobber settling mechanism instead of a short delay
        waitingForBobberSettle = true;
        bobberSettleTimer = getBobberSettleDelay(); // Use dynamic delay based on SCK state
        castAttempts = 1; // This is the first attempt
        lastCastTime = System.currentTimeMillis();
        isFishing = false;

        sendDebugMessage("Cast initiated - State: CASTING, Bobber settle timer: " + bobberSettleTimer + " ticks, Attempt: " + castAttempts);
    }

    private static void resetFishingState() {
        FishingState previousState = currentState;
        currentState = FishingState.IDLE;
        isFishing = false;
        castCooldownTimer = 0;
        // Reset recast mechanism variables
        int previousCastAttempts = castAttempts;
        castAttempts = 0;
        waitingForBobberSettle = false;
        bobberSettleTimer = 0;
        lastCastTime = 0;

        sendDebugMessage("Fishing state reset - Previous: " + previousState + " → Current: IDLE, Previous cast attempts: " + previousCastAttempts);
    }

    private static void stop() {
        sendDebugMessage("Stopping auto fishing - Resetting all timers and states");
        resetFishingState();
        delayTimer = 0;
        lastDetectionTime = 0;
        // Disable anti-AFK when stopping
        antiAfkEnabled = false;
        antiAfkTimer = 0;
        // Reset smooth movement variables
        isMovingToTarget = false;
        smoothSteps = 0;
        sendDebugMessage("Auto fishing stopped - Anti-AFK disabled, smooth movement reset");
    }

    public static void onDisconnect() {
        serverConnectionLost = true;
        enabled = false;
        stop();
        restoreMouseGrab();
        sendFailsafeMessage("Disconnected from server - auto fishing stopped", false);
        // Disable sea creature killer when disconnected
        SeaCreatureKiller.setAutoFishEnabled(false);
    }

    public static void onServerSwitch() {
        emergencyStopWithReason("Server change detected");
        // emergencyStopWithReason already handles disabling sea creature killer
    }

    public static void onDimensionChange() {
        emergencyStopWithReason("Dimension change detected");
        // emergencyStopWithReason already handles disabling sea creature killer
    }

    // Method to be called when player takes damage
    public static void onPlayerDamage() {
        if (enabled) {
            sendFailsafeMessage("Player took damage - monitoring health", false);
        }
    }


    // Method to toggle debug mode (for keybind)
    public static void toggleDebugMode() {
        debugMode = !debugMode;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
            if (debugMode) {
                Text message = Text.literal("Debug mode enabled - additional info will be shown").formatted(Formatting.YELLOW);
                client.player.sendMessage(prefix.copy().append(message), false);
            } else {
                Text message = Text.literal("Debug mode disabled").formatted(Formatting.GRAY);
                client.player.sendMessage(prefix.copy().append(message), false);
            }
        }
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    private static void sendDebugMessage(String message) {
        if (debugMode) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                Text prefix = Text.literal("[Debug] ").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
                Text content = Text.literal(message).formatted(Formatting.LIGHT_PURPLE);
                client.player.sendMessage(prefix.copy().append(content), false);
            }
        }
    }

    private static boolean switchToFishingRod() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) {
            sendDebugMessage("switchToFishingRod: Player or interaction manager is null");
            return false;
        }

        // Don't interfere with SCK weapon swapping during combat
        if (SeaCreatureKiller.isEnabled()) {
            sendDebugMessage("switchToFishingRod: SCK is active, skipping weapon swap to avoid interference");
            return false;
        }

        ItemStack mainHand = client.player.getStackInHand(Hand.MAIN_HAND);
        if (mainHand.getItem() instanceof FishingRodItem) {
            sendDebugMessage("switchToFishingRod: Already have fishing rod in main hand");
            return true;
        }

        // Check hotbar first
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FishingRodItem) {
                client.player.getInventory().setSelectedSlot(i);
                sendDebugMessage("switchToFishingRod: Switched to fishing rod in hotbar slot " + i);
                return true;
            }
        }

        // Check inventory
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

                sendDebugMessage("switchToFishingRod: Swapped fishing rod from inventory slot " + i + " to hotbar slot " + currentSlot);
                return true;
            }
        }

        sendDebugMessage("switchToFishingRod: No fishing rod found in inventory");
        return false;
    }

    private static boolean hasBobberInWater(PlayerEntity player) {
        FishingBobberEntity bobber = player.fishHook;
        if (bobber == null) {
            return false;
        }
        // Check for both water and lava to support lava fishing on Hypixel Skyblock
        boolean inWater = bobber.isTouchingWater();
        boolean inLava = bobber.isInLava();
        boolean result = inWater || inLava;

        if (result) {
            sendDebugMessage("Bobber status - In water: " + inWater + ", In lava: " + inLava + ", Position: (" +
                String.format("%.2f", bobber.getX()) + ", " + String.format("%.2f", bobber.getY()) + ", " + String.format("%.2f", bobber.getZ()) + ")");
        }

        return result;
    }

    private static void handleRecastMechanism(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Check if we are waiting for the bobber to settle
        if (waitingForBobberSettle) {
            if (bobberSettleTimer > 0) {
                bobberSettleTimer--;
                return;
            } else {
                // Check if bobber is now in water after settling
                if (client.player.fishHook != null && hasBobberInWater(client.player)) {
                    // Success! Bobber is in water
                    currentState = FishingState.FISHING;
                    isFishing = true;
                    waitingForBobberSettle = false;
                    int successfulAttempts = castAttempts;
                    castAttempts = 0; // Reset cast attempts on success
                    if (successfulAttempts > 1) {
                        sendFailsafeMessage("Bobber successfully cast after " + successfulAttempts + " attempts", false);
                    }
                    return;
                } else {
                    // Still not in water, prepare for recast
                    waitingForBobberSettle = false;
                }
            }
        }

        // Check if we've exceeded maximum cast attempts
        if (castAttempts >= MAX_CAST_ATTEMPTS) {
            // Wait before trying again
            if (currentTime - lastCastTime >= CAST_TIMEOUT) {
                sendFailsafeMessage("Failed to cast bobber after " + MAX_CAST_ATTEMPTS + " attempts. Retrying...", false);
                castAttempts = 0;
                consecutiveFailures++;
                lastCastTime = currentTime; // Reset the timer

                // If too many consecutive failures, trigger emergency stop
                if (consecutiveFailures >= Math.max(3, FishMasterConfig.getMaxConsecutiveFailures() / 2)) {
                    emergencyStopWithReason("Too many failed cast attempts - possible obstruction or invalid fishing area");
                    return;
                }
            }
            return;
        }

        // Check if enough time has passed since last cast attempt
        if (currentTime - lastCastTime < getRecastDelay() * 100) // Convert to milliseconds
        {
            return;
        }

        // Ensure we have a valid fishing rod before attempting to cast
        if (!hasValidFishingRod()) {
            if (!switchToFishingRod()) {
                emergencyStopWithReason("No valid fishing rod available for recasting");
                return;
            }
        }

        // If we have a bobber but it's not in water, reel it in first
        if (client.player.fishHook != null && !hasBobberInWater(client.player)) {
            try {
                Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                           Hand.MAIN_HAND : Hand.OFF_HAND;
                client.interactionManager.interactItem(client.player, hand);
                delayTimer = getRecastDelay(); // Wait before recasting
                lastCastTime = currentTime;
            } catch (Exception e) {
                castAttempts++;
                sendFailsafeMessage("Failed to reel in bobber: " + e.getMessage(), false);
            }
        } else if (client.player.fishHook == null) {
            // No bobber exists, try to cast
            try {
                Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                           Hand.MAIN_HAND : Hand.OFF_HAND;

                // Verify the fishing rod is valid before casting
                ItemStack rodStack = client.player.getStackInHand(hand);
                if (!(rodStack.getItem() instanceof FishingRodItem) || rodStack.isEmpty()) {
                    // Try the other hand
                    hand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                    rodStack = client.player.getStackInHand(hand);
                    if (!(rodStack.getItem() instanceof FishingRodItem) || rodStack.isEmpty()) {
                        castAttempts++;
                        sendFailsafeMessage("No valid fishing rod in hands for casting", false);
                        return;
                    }
                }

                client.interactionManager.interactItem(client.player, hand);

                castAttempts++;
                lastCastTime = currentTime;
                waitingForBobberSettle = true;
                bobberSettleTimer = getBobberSettleDelay(); // Use dynamic delay based on SCK state

                if (castAttempts > 1) {
                    sendFailsafeMessage("Recasting bobber (attempt " + castAttempts + "/" + MAX_CAST_ATTEMPTS + ")", false);
                }
            } catch (Exception e) {
                castAttempts++;
                consecutiveFailures++;
                lastCastTime = currentTime;
                sendFailsafeMessage("Failed to cast bobber: " + e.getMessage(), false);
            }
        }
    }

    private static void performSingleClick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            consecutiveFailures++;
            return;
        }

        try {
            Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                       Hand.MAIN_HAND : Hand.OFF_HAND;
            client.interactionManager.interactItem(client.player, hand);
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

        int armorStandsChecked = 0;
        int fishBiteStands = 0;

        for (Entity entity : entities) {
            if (entity instanceof ArmorStandEntity armorStand) {
                armorStandsChecked++;
                if (armorStand.hasCustomName()) {
                    Text customName = armorStand.getCustomName();
                    if (customName != null) {
                        String nameString = customName.getString();
                        if ("!!!".equals(nameString)) {
                            fishBiteStands++;
                            double distance = armorStand.squaredDistanceTo(client.player);
                            if (distance <= 50 * 50) {
                                sendDebugMessage("Fish bite detected! Armor stand with '!!!' found at distance: " + String.format("%.2f", Math.sqrt(distance)) + " blocks");
                                return true;
                            }
                        }
                    }
                }
            }
        }

        if (armorStandsChecked > 0) {
            sendDebugMessage("Fish bite check - Armor stands checked: " + armorStandsChecked + ", Fish bite stands: " + fishBiteStands);
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
                float potentialPitch = originalPitch + pitchOffset;

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
}
