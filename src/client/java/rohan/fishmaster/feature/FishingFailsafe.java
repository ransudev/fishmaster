package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FishingFailsafe {
    private static boolean enabled = true;

    // Fish bite timeout failsafe
    private static long lastCastTime = 0;
    private static long lastBiteDetectionTime = 0;
    private static final long FISH_BITE_TIMEOUT = 20000; // 20 seconds in milliseconds (changed from 30)

    // General stuck detection
    private static long lastStateChangeTime = 0;
    private static String lastState = "";
    private static final long STUCK_TIMEOUT = 60000; // 60 seconds for general stuck detection

    // Bobber timeout failsafe
    private static long bobberInWaterTime = 0;
    private static boolean wasInWater = false;
    private static final long BOBBER_TIMEOUT = 5000; // 5 seconds if bobber not touching water

    // Rod switching failsafe variables (added missing variables)
    private static int rodSwitchAttempts = 0;
    private static long lastRodSwitchAttempt = 0;
    private static final int MAX_ROD_SWITCH_ATTEMPTS = 3;
    private static final long ROD_SWITCH_COOLDOWN = 5000; // 5 seconds between attempts

    public static void setEnabled(boolean enabled) {
        FishingFailsafe.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Called when a new cast is made
     */
    public static void onCast() {
        lastCastTime = System.currentTimeMillis();
        resetBiteTimer();
        resetStuckDetection("CASTING");
    }

    /**
     * Called when a fish bite is detected
     */
    public static void onBiteDetected() {
        lastBiteDetectionTime = System.currentTimeMillis();
        resetStuckDetection("BITE_DETECTED");
    }

    /**
     * Called to update the current fishing state
     */
    public static void updateState(String state) {
        if (!state.equals(lastState)) {
            lastStateChangeTime = System.currentTimeMillis();
            lastState = state;
        }
    }

    /**
     * Main failsafe tick method - should be called every game tick
     */
    public static boolean checkFailsafes() {
        if (!enabled) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !AutoFishingFeature.isEnabled()) {
            reset();
            return false;
        }

        // Check fish bite timeout
        if (checkFishBiteTimeout(client)) {
            return true;
        }

        // Check if stuck in same state too long
        if (checkStuckDetection(client)) {
            return true;
        }

        // Check bobber timeout
        if (checkBobberTimeout(client)) {
            return true;
        }

        // Update bobber tracking
        updateBobberTracking(client);

        return false;
    }

    private static boolean checkFishBiteTimeout(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();

        // Only check timeout if we're actively fishing and have cast recently
        if (AutoFishingFeature.isFishing() && lastCastTime > 0) {
            long timeSinceCast = currentTime - lastCastTime;

            // If we've been fishing for more than 20 seconds without a bite
            if (timeSinceCast > FISH_BITE_TIMEOUT) {
                sendFailsafeMessage(client, "Fish bite timeout (20s) - recasting...", Formatting.YELLOW);
                forceRecast(client);
                return true;
            }
        }

        return false;
    }

    private static boolean checkStuckDetection(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();

        if (lastStateChangeTime > 0) {
            long timeSinceStateChange = currentTime - lastStateChangeTime;

            if (timeSinceStateChange > STUCK_TIMEOUT) {
                sendFailsafeMessage(client, "Stuck in state '" + lastState + "' for too long - resetting...", Formatting.RED);
                forceReset(client);
                return true;
            }
        }

        return false;
    }

    private static boolean checkBobberTimeout(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();

        if (bobberInWaterTime > 0) {
            long timeNotInWater = currentTime - bobberInWaterTime;

            if (timeNotInWater > BOBBER_TIMEOUT) {
                // Silent recast - no message shown to player
                forceRecast(client);
                return true;
            }
        }

        return false;
    }

    private static void updateBobberTracking(MinecraftClient client) {
        // Check if bobber exists and is in a valid fishing state
        boolean bobberExists = client.player.fishHook != null;
        boolean currentlyInWater = bobberExists && client.player.fishHook.isTouchingWater();
        boolean currentlyInLava = bobberExists && client.player.fishHook.isInLava();
        boolean hookedToEntity = bobberExists && client.player.fishHook.getHookedEntity() != null;

        // Bobber is functional if it's in water OR lava and not hooked to an entity
        boolean bobberFunctional = bobberExists && (currentlyInWater || currentlyInLava) && !hookedToEntity;

        if (bobberExists) {
            // If bobber is in water/lava and functioning properly, reset timer
            if (bobberFunctional) {
                bobberInWaterTime = 0;
                wasInWater = true;
            }
            // If bobber is not in water/lava OR hooked to an entity, recast instantly
            else if (!currentlyInWater && !currentlyInLava || hookedToEntity) {
                // Instant recast when bobber is not touching water or lava
                forceRecast(client);
                return; // Exit early after triggering recast
            }
        } else {
            // No bobber exists, reset everything
            bobberInWaterTime = 0;
            wasInWater = false;
        }
    }

    /**
     * Force a recast by simulating the fishing action
     */
    private static void forceRecast(MinecraftClient client) {
        try {
            // If there's a bobber, reel it in first
            if (client.player.fishHook != null && client.interactionManager != null) {
                // Trigger hand swing animation for natural appearance
                client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                
                client.interactionManager.interactItem(client.player, net.minecraft.util.Hand.MAIN_HAND);

                // Wait a moment then cast again
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        if (client.interactionManager != null && client.player != null) {
                            // Trigger hand swing animation for natural appearance
                            client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                            
                            client.interactionManager.interactItem(client.player, net.minecraft.util.Hand.MAIN_HAND);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }

            onCast(); // Reset timers
        } catch (Exception e) {
            sendFailsafeMessage(client, "Error during recast: " + e.getMessage(), Formatting.RED);
        }
    }

    /**
     * Force a complete reset of the fishing system
     */
    private static void forceReset(MinecraftClient client) {
        try {
            // Reel in any existing bobber
            if (client.player.fishHook != null && client.interactionManager != null) {
                // Trigger hand swing animation for natural appearance
                client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                
                client.interactionManager.interactItem(client.player, net.minecraft.util.Hand.MAIN_HAND);
            }

            // Reset all failsafe timers
            reset();

            // Try to switch to fishing rod if needed
            attemptRodSwitch(client);

        } catch (Exception e) {
            sendFailsafeMessage(client, "Error during reset: " + e.getMessage(), Formatting.RED);
        }
    }

    private static void attemptRodSwitch(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRodSwitchAttempt < ROD_SWITCH_COOLDOWN) {
            return; // Still in cooldown
        }

        if (rodSwitchAttempts >= MAX_ROD_SWITCH_ATTEMPTS) {
            sendFailsafeMessage(client, "Max rod switch attempts reached - please check your inventory", Formatting.RED);
            return;
        }

        lastRodSwitchAttempt = currentTime;
        rodSwitchAttempts++;

        // This would need to call the rod switching logic from AutoFishingFeature
        sendFailsafeMessage(client, "Attempting to switch to fishing rod (attempt " + rodSwitchAttempts + ")", Formatting.YELLOW);
    }

    private static void sendFailsafeMessage(MinecraftClient client, String message, Formatting color) {
        if (client.player != null) {
            Text prefix = Text.literal("[Fish Master Failsafe] ").formatted(Formatting.DARK_RED, Formatting.BOLD);
            Text msg = Text.literal(message).formatted(color);
            client.player.sendMessage(prefix.copy().append(msg), false);
        }
    }

    private static void resetBiteTimer() {
        lastBiteDetectionTime = System.currentTimeMillis();
    }

    private static void resetStuckDetection(String newState) {
        lastStateChangeTime = System.currentTimeMillis();
        lastState = newState;
    }

    /**
     * Reset all failsafe timers and counters
     */
    public static void reset() {
        lastCastTime = 0;
        lastBiteDetectionTime = 0;
        lastStateChangeTime = 0;
        lastState = "";
        bobberInWaterTime = 0;
        wasInWater = false;
        rodSwitchAttempts = 0;
        lastRodSwitchAttempt = 0;
    }

    /**
     * Get time remaining until fish bite timeout (for debugging/display)
     */
    public static long getTimeUntilBiteTimeout() {
        if (lastCastTime == 0) return -1;

        long elapsed = System.currentTimeMillis() - lastCastTime;
        long remaining = FISH_BITE_TIMEOUT - elapsed;
        return Math.max(0, remaining);
    }
}
