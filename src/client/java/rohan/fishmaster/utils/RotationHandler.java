package rohan.fishmaster.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Handles smooth rotation of the player's view.
 * Compatible with Fabric 1.21.5
 */

public class RotationHandler {
    private static RotationHandler instance;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Random random = new Random();
    
    private final Queue<RotationConfiguration> rotationQueue = new ArrayDeque<>();
    private RotationConfiguration currentConfig;
    private boolean isRunning = false;
    private long startTime;
    private long endTime;
    
    // Rotation state
    private float startYaw;
    private float startPitch;
    private float targetYaw;
    private float targetPitch;
    private float lastYaw;
    private float lastPitch;
    
    // For server-side rotation
    private float serverYaw;
    private float serverPitch;
    
    // Configuration
    private int randomMultiplier1 = 1;
    private int randomMultiplier2 = 1;
    private boolean followingTarget = false;

    private RotationHandler() {
        // Register event listeners
        WorldRenderEvents.END.register(this::onRenderWorld);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }
    
    public static RotationHandler getInstance() {
        if (instance == null) {
            instance = new RotationHandler();
        }
        return instance;
    }
    
    public void queueRotation(RotationConfiguration config) {
        rotationQueue.add(config);
        if (!isRunning) {
            startNextRotation();
        }
    }
    
    private void startNextRotation() {
        if (rotationQueue.isEmpty()) {
            isRunning = false;
            return;
        }
        
        currentConfig = rotationQueue.poll();
        isRunning = true;
        startTime = System.currentTimeMillis();
        
        // Get current angles
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            stop();
            return;
        }
        
        // Initialize rotation state
        startYaw = player.getYaw();
        startPitch = player.getPitch();
        
        // Get target angles from configuration
        targetYaw = currentConfig.getTargetYaw();
        targetPitch = currentConfig.getTargetPitch();
        
        // Calculate duration based on angle difference
        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - startYaw));
        float pitchDiff = Math.abs(targetPitch - startPitch);
        float distance = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        
        // Set end time based on rotation distance
        endTime = startTime + (long) (distance * currentConfig.getSpeedMultiplier());
        
        // Initialize server-side rotation if needed
        if (currentConfig.isServerSide()) {
            serverYaw = startYaw;
            serverPitch = startPitch;
        }
        
        // Initialize bezier curve parameters
        randomMultiplier1 = random.nextBoolean() ? 1 : -1;
        randomMultiplier2 = random.nextBoolean() ? 1 : -1;
        
        lastYaw = 0;
        lastPitch = 0;
    }

    public void stop() {
        rotationQueue.clear();
        isRunning = false;
        currentConfig = null;
    }
    
    public void cancel() {
        rotationQueue.clear();
        currentConfig = null;
        isRunning = false;
    }
    
    /**
     * @return true if a rotation is currently in progress, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    public void tick() {
        if (!isRunning || currentConfig == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= endTime) {
            completeRotation();
            return;
        }
        
        // Calculate progress (0.0 to 1.0)
        float progress = (float) (currentTime - startTime) / (endTime - startTime);
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);
        
        // Apply easing function
        float easedProgress = applyEasing(progress);
        
        // Calculate new angles
        float newYaw = startYaw + (targetYaw - startYaw) * easedProgress;
        float newPitch = startPitch + (targetPitch - startPitch) * easedProgress;
        
        // Update player rotation
        ClientPlayerEntity player = mc.player;
        if (player != null) {
            if (currentConfig.isServerSide()) {
                // For server-side rotation, we need to send packets
                serverYaw = newYaw;
                serverPitch = newPitch;
                // In a real implementation, you'd send a packet to update the server
                // player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                //     newYaw, newPitch, player.isOnGround()
                // ));
            } else {
                // For client-side rotation, just update the player's rotation
                player.setYaw(newYaw);
                player.setPitch(newPitch);
            }
        }
        
        lastYaw = newYaw;
        lastPitch = newPitch;
    }
    
    private float applyEasing(float progress) {
        // Simple ease-out cubic easing function
        float t = progress - 1.0f;
        return t * t * t + 1.0f;
    }
    
    private void completeRotation() {
        if (currentConfig != null && currentConfig.shouldContinue()) {
            // If the rotation should continue, queue it again
            queueRotation(currentConfig);
        } else if (currentConfig != null && currentConfig.getCallback() != null) {
            // Otherwise, run the callback if it exists
            currentConfig.getCallback().run();
        }
        
        // Start the next rotation if available
        if (!rotationQueue.isEmpty()) {
            startNextRotation();
        } else {
            isRunning = false;
            currentConfig = null;
        }
    }

    private void onRenderWorld(WorldRenderContext context) {
        if (!isRunning || currentConfig == null || !currentConfig.isClientSide()) {
            return;
        }
        
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        
        // This method is called every frame, so we can use it for smooth client-side rotation
        // The actual rotation is handled in the tick() method, but we can add visual effects here if needed
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null || !isRunning) return;
        
        tick();
    }

    public void easeTo(float yaw, float pitch, long duration, boolean serverSide, Runnable callback) {
        RotationConfiguration config = new RotationConfiguration(yaw, pitch, duration, serverSide, callback);
        queueRotation(config);
    }
    
    public void easeTo(float yaw, float pitch, long duration) {
        easeTo(yaw, pitch, duration, false, null);
    }
    
    public void easeTo(float yaw, float pitch) {
        easeTo(yaw, pitch, 500, false, null);
    }

    /**
     * Simple configuration class for rotation parameters
     */
    public static class RotationConfiguration {
        private final float targetYaw;
        private final float targetPitch;
        private final long duration;
        private final boolean serverSide;
        private final Runnable callback;
        private boolean shouldContinue = false;
        private final float speedMultiplier;
        
        public RotationConfiguration(float targetYaw, float targetPitch, long duration, boolean serverSide, Runnable callback) {
            this.targetYaw = targetYaw;
            this.targetPitch = targetPitch;
            this.duration = duration;
            this.serverSide = serverSide;
            this.callback = callback;
            this.speedMultiplier = 1.0f;
        }
        
        public RotationConfiguration(float targetYaw, float targetPitch, long duration, boolean serverSide) {
            this(targetYaw, targetPitch, duration, serverSide, null);
        }
        
        public RotationConfiguration continueRotation() {
            this.shouldContinue = true;
            return this;
        }
        
        public RotationConfiguration withSpeedMultiplier(float multiplier) {
            return new RotationConfiguration(targetYaw, targetPitch, duration, serverSide, callback) {
                @Override
                public float getSpeedMultiplier() {
                    return multiplier;
                }
            };
        }
        
        public float getTargetYaw() {
            return targetYaw;
        }
        
        public float getTargetPitch() {
            return targetPitch;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public boolean isServerSide() {
            return serverSide;
        }
        
        public boolean isClientSide() {
            return !serverSide;
        }
        
        public boolean shouldContinue() {
            return shouldContinue;
        }
        
        public Runnable getCallback() {
            return callback;
        }
        
        public float getSpeedMultiplier() {
            return speedMultiplier;
        }
    }
}
