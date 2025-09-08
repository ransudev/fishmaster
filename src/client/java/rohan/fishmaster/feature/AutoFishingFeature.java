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
import net.minecraft.util.ActionResult;

import rohan.fishmaster.config.FishMasterConfig;

public class AutoFishingFeature {
    private static AutoFishingFeature instance;

    private static boolean enabled = false;
    private static boolean isFishing = false;
    private static int delayTimer = 0;
    private static int castCooldownTimer = 0;
    private static int reelingDelayTimer = 0; // New timer for reeling delay
    private static long lastDetectionTime = 0;
    private static boolean mouseWasGrabbed = false;

    // Debug mode variables
    private static boolean debugMode = false;

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
    private static boolean justCaughtFish = false; // Flag to prevent recast mechanism after successful catch
    private static long castStartTime = 0; // Track when casting started for bobber flight delay
    private static final int MAX_CAST_ATTEMPTS = 5;
    private static final long CAST_TIMEOUT = 5000; // 5 seconds to wait for bobber to settle (increased from 3s)
    // RECAST_DELAY is now configurable via FishMasterConfig.getRecastDelay()
    private static final int BOBBER_FLIGHT_DELAY = 10; // 10 ticks (0.5 seconds) for bobber to fly
    private static final long BOBBER_SETTLE_TIMEOUT = 3000; // 3 seconds for bobber to settle in water
    private static final long MIN_RECAST_INTERVAL = 1000; // Minimum 1 second between recast attempts

    // Fishing timing variables
    private static long fishingStartTime = 0;
    private static final int MIN_FISHING_TICKS = 20; // 20 ticks (1 second) after bobber touches water


    // Player movement detection for failsafe
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    private static boolean playerPositionInitialized = false;
    private static final double MOVEMENT_THRESHOLD = 10.0; // Player moved more than 10 blocks

    // --- Anti-AFK variables ---
    private static float originalYaw = 0f;
    private static float originalPitch = 0f;
    private static boolean originalAnglesSet = false;
    private static int antiAfkMoveCount = 0;
    private static long lastAntiAfkMove = 0;
    private static final long ANTI_AFK_INTERVAL_MS = 5000; // 5 seconds between moves
    private static float targetYaw = 0f;
    private static float targetPitch = 0f;
    private static boolean returningToOriginal = false;

    private enum FishingState {
        IDLE,
        CASTING,
        FISHING
    }

    private static FishingState currentState = FishingState.IDLE;

    private static final long DETECTION_COOLDOWN = 50;
    private static final int BOBBER_SETTLE_TIME = 3;

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
            restoreMouseGrab();
            emergencyStop = false;
            SeaCreatureKiller.setAutoFishEnabled(false);
            sendDebugMessage("Auto fishing stopped - SCK disabled, tracker remains active");
            // Reset anti-AFK original angles when disabling
            originalAnglesSet = false;
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
            sessionStartTime = System.currentTimeMillis();
            lastSuccessfulFish = sessionStartTime;
            consecutiveFailures = 0;
            emergencyStop = false;
            serverConnectionLost = false;
            SeaCreatureKiller.setAutoFishEnabled(true);
            sendDebugMessage("Auto fishing started - Session time: " + sessionStartTime + ", SCK enabled, tracker active");
            // Set anti-AFK original angles exactly when enabling
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                originalYaw = client.player.getYaw();
                originalPitch = client.player.getPitch();
                originalAnglesSet = true;
                targetYaw = originalYaw;
                targetPitch = originalPitch;
            }
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (enabled) {
                Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
                Text message = Text.literal("Auto fishing enabled").formatted(Formatting.GREEN);
                client.player.sendMessage(prefix.copy().append(message), false);
            } else {
                Text prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD);
                Text message = Text.literal("Auto fishing disabled").formatted(Formatting.RED);
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

        if (reelingDelayTimer > 0) {
            reelingDelayTimer--;
            if (reelingDelayTimer % 10 == 0) { // Debug message every half second
                sendDebugMessage("Reeling delay timer: " + reelingDelayTimer + " ticks remaining");
            }
            if (reelingDelayTimer == 0) {
                // Timer expired, now reel in the fish
                sendDebugMessage("Reeling delay completed - reeling in fish!");
                performSingleClick(client);
                justCaughtFish = true;
                resetFishingState();
                delayTimer = (int) FishMasterConfig.getRecastDelay();
                sendDebugMessage("Fish reeled in - waiting " + FishMasterConfig.getRecastDelay() + " ticks before next cast");
            }
            return; // Don't continue with other logic while waiting to reel in
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
                    // Bobber is in water - transition to fishing immediately
                    currentState = FishingState.FISHING;
                    isFishing = true;
                    fishingStartTime = System.currentTimeMillis(); // Record when fishing started
                    castAttempts = 0; // Reset cast attempts on successful cast
                    sendDebugMessage("Bobber in water - State: CASTING → FISHING, starting 20-tick delay");
                } else {
                    // Only try to recast if we haven't successfully cast yet
                    // If we already have a bobber out, just wait (don't trigger recast mechanism)
                    if (client.player.fishHook == null) {
                        handleRecastMechanism(client);
                    } else {
                        // We have a bobber but it's not in water yet, just wait
                        sendDebugMessage("Bobber exists but not in water yet - waiting...");
                    }
                }
                break;

            case FISHING:
                if (client.player.fishHook == null) {
                    sendDebugMessage("Bobber disappeared - resetting state");
                    resetFishingState();
                    delayTimer = (int) FishMasterConfig.getRecastDelay();
                } else if (hasBobberInWater(client.player)) {
                    // Only check for fish bites if we're not already waiting to reel in
                    if (reelingDelayTimer == 0) {
                        // Only check for fish bites after minimum fishing time has passed
                        long timeFishing = System.currentTimeMillis() - fishingStartTime;
                        if (timeFishing >= MIN_FISHING_TICKS * 50 && detectArmorStandFishBite(client)) {
                            long currentTimeMillis = System.currentTimeMillis();
                            if (currentTimeMillis - lastDetectionTime >= DETECTION_COOLDOWN) {
                                lastDetectionTime = currentTimeMillis;
                                sendDebugMessage("Fish detected after " + timeFishing + "ms of fishing! Starting reeling delay...");
                                // Instead of immediately reeling in, start the reeling delay timer
                                reelingDelayTimer = (int) FishMasterConfig.getReelingDelay();
                                sendDebugMessage("Reeling delay started - waiting " + FishMasterConfig.getReelingDelay() + " ticks (" + (FishMasterConfig.getReelingDelay() * 50) + "ms) before reeling in");
                            }
                        } else if (timeFishing < MIN_FISHING_TICKS * 50) {
                            // Still waiting for minimum fishing time
                            long remainingTime = MIN_FISHING_TICKS * 50 - timeFishing;
                            if (remainingTime % 1000 == 0) { // Debug message every second
                                sendDebugMessage("Waiting for fish... " + (remainingTime / 1000) + "s remaining");
                            }
                        }
                    }
                }
                break;
        }

        // Anti-AFK disabled per user request
        // handleAntiAfk();

        // Session management failsafes removed - to be added later per user preference
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
        if (client.player == null) {
            sendDebugMessage("startCasting: Player is null");
            return;
        }

        Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                   Hand.MAIN_HAND : Hand.OFF_HAND;

        sendDebugMessage("Starting cast - Hand: " + hand + ", Previous state: " + currentState);
        
        // Simulate right-click instead of using interactItem
        simulateRightClick(client, hand);

        currentState = FishingState.CASTING;
        castAttempts = 1; // This is the first attempt
        lastCastTime = System.currentTimeMillis();
        isFishing = false;
        justCaughtFish = false; // Clear the flag when starting a new cast
        castStartTime = lastCastTime; // Track when casting started
        delayTimer = 10; // Add 10 tick delay after casting to let bobber fly

        sendDebugMessage("Cast initiated - State: CASTING, Attempt: " + castAttempts + ", waiting 10 ticks for bobber to fly");
    }

    private static void resetFishingState() {
        FishingState previousState = currentState;
        currentState = FishingState.IDLE;
        isFishing = false;
        castCooldownTimer = 0;
        reelingDelayTimer = 0; // Reset reeling delay timer
        // Reset recast mechanism variables
        int previousCastAttempts = castAttempts;
        castAttempts = 0;
        lastCastTime = 0;

        sendDebugMessage("Fishing state reset - Previous: " + previousState + " → Current: IDLE, Previous cast attempts: " + previousCastAttempts);
    }

    private static void stop() {
        sendDebugMessage("Stopping auto fishing - Resetting all timers and states");
        resetFishingState();
        delayTimer = 0;
        reelingDelayTimer = 0; // Reset reeling delay timer
        lastDetectionTime = 0;
        // Disable anti-AFK when stopping
        sendDebugMessage("Auto fishing stopped - Anti-AFK disabled");
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


        return result;
    }

    private static void handleRecastMechanism(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        
        // Calculate time since cast started (including bobber flight time)
        long timeSinceCastStart = currentTime - castStartTime;
        long timeSinceLastCast = currentTime - lastCastTime;

        // Check if we've exceeded maximum cast attempts
        if (castAttempts >= MAX_CAST_ATTEMPTS) {
            // Wait longer before trying again to account for bobber settle time
            if (timeSinceLastCast >= CAST_TIMEOUT) {
                sendDebugMessage("Resetting cast attempts after timeout - Time since last cast: " + timeSinceLastCast + "ms");
                sendFailsafeMessage("Failed to cast bobber after " + MAX_CAST_ATTEMPTS + " attempts. Retrying...", false);
                castAttempts = 0;
                consecutiveFailures++;
                lastCastTime = currentTime;
                castStartTime = currentTime;

                // If too many consecutive failures, trigger emergency stop
                if (consecutiveFailures >= Math.max(3, FishMasterConfig.getMaxConsecutiveFailures() / 2)) {
                    emergencyStopWithReason("Too many failed cast attempts - possible obstruction or invalid fishing area");
                    return;
                }
            }
            return;
        }

        // Don't attempt recast too quickly - respect minimum interval
        if (timeSinceLastCast < MIN_RECAST_INTERVAL) {
            sendDebugMessage("Recast interval not met - Time since last: " + timeSinceLastCast + "ms, Required: " + MIN_RECAST_INTERVAL + "ms");
            return;
        }

        // Check if bobber exists but hasn't settled in water yet
        if (client.player.fishHook != null) {
            // If bobber exists but not in water, wait for settle timeout before attempting recast
            if (!hasBobberInWater(client.player)) {
                if (timeSinceCastStart < BOBBER_SETTLE_TIMEOUT) {
                    // Still within settle timeout, just wait
                    if (timeSinceCastStart % 1000 < 50) { // Debug message roughly every second
                        sendDebugMessage("Waiting for bobber to settle - Time elapsed: " + timeSinceCastStart + "ms, Timeout: " + BOBBER_SETTLE_TIMEOUT + "ms");
                    }
                    return;
                } else {
                    // Bobber settle timeout exceeded, reel in and try again
                    sendDebugMessage("Bobber settle timeout exceeded (" + timeSinceCastStart + "ms) - reeling in for recast");
                    try {
                        Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                                   Hand.MAIN_HAND : Hand.OFF_HAND;
                        simulateRightClick(client, hand);
                        delayTimer = (int) FishMasterConfig.getRecastDelay(); // Use configurable delay
                        lastCastTime = currentTime;
                        castAttempts++;
                        sendDebugMessage("Reeled in bobber due to settle timeout - Attempt: " + castAttempts);
                    } catch (Exception e) {
                        castAttempts++;
                        sendFailsafeMessage("Failed to reel in bobber: " + e.getMessage(), false);
                    }
                }
            }
            // If bobber is in water, don't interfere - let normal fishing logic handle it
            return;
        }

        // No bobber exists - need to cast
        // Ensure we have a valid fishing rod before attempting to cast
        if (!hasValidFishingRod()) {
            if (!switchToFishingRod()) {
                emergencyStopWithReason("No valid fishing rod available for recasting");
                return;
            }
        }

        // Attempt to cast
        try {
            Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                       Hand.MAIN_HAND : Hand.OFF_HAND;
            simulateRightClick(client, hand);
            castAttempts++;
            lastCastTime = currentTime;
            delayTimer = BOBBER_FLIGHT_DELAY; // Wait for bobber to fly out

            if (castAttempts > 1) {
                sendDebugMessage("Recasting bobber (attempt " + castAttempts + "/" + MAX_CAST_ATTEMPTS + ") - waiting " + BOBBER_FLIGHT_DELAY + " ticks for flight");
                sendFailsafeMessage("Recasting bobber (attempt " + castAttempts + "/" + MAX_CAST_ATTEMPTS + ")", false);
            } else {
                sendDebugMessage("Initial cast - waiting " + BOBBER_FLIGHT_DELAY + " ticks for bobber flight");
            }
        } catch (Exception e) {
            castAttempts++;
            consecutiveFailures++;
            lastCastTime = currentTime;
            sendFailsafeMessage("Failed to cast bobber: " + e.getMessage(), false);
        }
    }

    private static void performSingleClick(MinecraftClient client) {
        if (client.player == null) {
            consecutiveFailures++;
            return;
        }

        try {
            Hand hand = client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem ?
                       Hand.MAIN_HAND : Hand.OFF_HAND;
            simulateRightClick(client, hand);
            lastSuccessfulFish = System.currentTimeMillis();
            consecutiveFailures = 0; // Reset failure count on successful interaction
        } catch (Exception e) {
            consecutiveFailures++;
            sendFailsafeMessage("Failed to interact with fishing rod: " + e.getMessage(), false);
        }
    }

    /**
     * Simulates a right-click action using the item in the specified hand
     * This mimics actual player input rather than sending direct packets
     */
    private static void simulateRightClick(MinecraftClient client, Hand hand) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        try {
            ItemStack itemStack = client.player.getStackInHand(hand);
            if (itemStack.getItem() instanceof FishingRodItem) {
                sendDebugMessage("Simulating right-click with " + hand + " hand using item directly");
                
                // Method 1: Use the item directly (most authentic approach)
                ActionResult result = itemStack.use(client.world, client.player, hand);
                sendDebugMessage("Direct item use result: " + result);
                
                // Method 2: If direct use doesn't work, try using interaction manager
                // but only if the first method failed
                if (result == ActionResult.FAIL || result == ActionResult.PASS) {
                    sendDebugMessage("Direct use failed, trying interaction manager fallback");
                    client.interactionManager.interactItem(client.player, hand);
                }
            } else {
                sendDebugMessage("Warning: Attempted to simulate right-click with non-fishing rod item: " + itemStack.getItem());
            }
        } catch (Exception e) {
            sendDebugMessage("Exception in simulateRightClick: " + e.getMessage());
            // Fallback to interaction manager if all else fails
            try {
                client.interactionManager.interactItem(client.player, hand);
                sendDebugMessage("Used interaction manager as final fallback");
            } catch (Exception fallbackException) {
                sendDebugMessage("All right-click simulation methods failed: " + fallbackException.getMessage());
            }
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


    public static AutoFishingFeature getInstance() {
        if (instance == null) {
            instance = new AutoFishingFeature();
        }
        return instance;
    }
}
