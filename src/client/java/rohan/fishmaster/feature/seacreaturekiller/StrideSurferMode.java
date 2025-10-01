package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rohan.fishmaster.feature.AutoFishingFeature;

import java.util.*;

/**
 * StrideSurfer Mode - Accumulates 20-30 StrideSurfers and notifies player when max reached
 * Player must press B to restart fishing after notification
 */
public class StrideSurferMode extends SeaCreatureKillerMode {
    
    private static final double DETECTION_RANGE = 6.0;
    private static final int MIN_STRIDERS = 20;
    private static final int MAX_STRIDERS = 30;
    private static final long NOTIFICATION_DURATION = 5000; // 5 seconds
    
    private final Set<UUID> trackedStriders = new HashSet<>();
    private final Random random = new Random();
    private int targetStriderCount = 0;
    private boolean maxReached = false;
    private long maxReachedTime = 0;
    
    public StrideSurferMode() {
        // Generate random target count between 20-30 on initialization
        this.targetStriderCount = MIN_STRIDERS + random.nextInt(MAX_STRIDERS - MIN_STRIDERS + 1);
    }

    @Override
    public void performAttack(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Check if target is a StrideSurfer
        if (!isStrideSurfer(target)) {
            return;
        }

        // If max already reached, don't process further
        if (maxReached) {
            return;
        }

        // Track this StrideSurfer
        trackedStriders.add(target.getUuid());

        // Count nearby StrideSurfers
        int nearbyCount = countNearbyStrideSurfers(client);

        // If we haven't reached the target count yet
        if (nearbyCount < targetStriderCount) {
            // Show progress every 5 StrideSurfers or on initial spawn
            if (nearbyCount % 5 == 0 || nearbyCount == 1) {
                client.player.sendMessage(Text.literal("[StrideSurfer] ")
                    .formatted(Formatting.GOLD, Formatting.BOLD)
                    .append(Text.literal(nearbyCount + "/" + targetStriderCount)
                    .formatted(Formatting.AQUA)), false);
            }
            return;
        }

        // We've reached the target count - show notification and stop
        reachedMaxStrideSurfers(client, nearbyCount);
    }

    @Override
    public String getModeName() {
        return "StrideSurfer";
    }

    /**
     * Check if entity is a StrideSurfer
     */
    private boolean isStrideSurfer(Entity entity) {
        if (entity == null) return false;
        
        String displayName = getEntityDisplayName(entity);
        return displayName.toLowerCase().contains("stridersurfer") || 
               displayName.toLowerCase().contains("strider surfer");
    }

    /**
     * Count nearby StrideSurfers within detection range
     */
    private int countNearbyStrideSurfers(MinecraftClient client) {
        if (client.player == null || client.world == null) return 0;

        Vec3d playerPos = client.player.getPos();
        Box searchBox = new Box(
            playerPos.x - DETECTION_RANGE, playerPos.y - DETECTION_RANGE, playerPos.z - DETECTION_RANGE,
            playerPos.x + DETECTION_RANGE, playerPos.y + DETECTION_RANGE, playerPos.z + DETECTION_RANGE
        );

        List<Entity> entities = client.world.getOtherEntities(client.player, searchBox);
        int count = 0;

        for (Entity entity : entities) {
            if (isStrideSurfer(entity) && !entity.isRemoved()) {
                count++;
            }
        }

        return count;
    }


    /**
     * Called when max StrideSurfers reached
     */
    private void reachedMaxStrideSurfers(MinecraftClient client, int striderCount) {
        maxReached = true;
        maxReachedTime = System.currentTimeMillis();

        // Show big notification
        client.player.sendMessage(
            Text.literal("\n\n")
                .append(Text.literal("════════════════════════════════════")
                    .formatted(Formatting.GOLD, Formatting.BOLD))
                .append(Text.literal("\n"))
                .append(Text.literal("⚡ STRIDESURFER MAX ⚡")
                    .formatted(Formatting.RED, Formatting.BOLD))
                .append(Text.literal("\n"))
                .append(Text.literal(striderCount + " StrideSurfers Accumulated!")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal("\n"))
                .append(Text.literal("Press B to resume fishing")
                    .formatted(Formatting.AQUA, Formatting.ITALIC))
                .append(Text.literal("\n"))
                .append(Text.literal("════════════════════════════════════")
                    .formatted(Formatting.GOLD, Formatting.BOLD))
                .append(Text.literal("\n\n")),
            false
        );

        // Show title overlay
        client.inGameHud.setTitle(Text.literal("⚡ STRIDESURFER MAX ⚡")
            .formatted(Formatting.RED, Formatting.BOLD));
        client.inGameHud.setSubtitle(Text.literal(striderCount + " StrideSurfers Ready!")
            .formatted(Formatting.YELLOW));

        // Stop auto fishing
        if (AutoFishingFeature.isEnabled()) {
            AutoFishingFeature.toggle();
            client.player.sendMessage(Text.literal("[AutoFish] ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("Stopped - Press B to restart")
                .formatted(Formatting.YELLOW)), false);
        }
    }

    /**
     * Reset for next cycle when player restarts fishing
     */
    public void resetForNewCycle() {
        maxReached = false;
        trackedStriders.clear();
        
        // Generate new random target count for next cycle
        targetStriderCount = MIN_STRIDERS + random.nextInt(MAX_STRIDERS - MIN_STRIDERS + 1);
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[StrideSurfer] ")
                .formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal("New target: " + targetStriderCount + " StrideSurfers")
                .formatted(Formatting.AQUA)), false);
        }
    }






    /**
     * Reset the mode state
     */
    public void reset() {
        maxReached = false;
        trackedStriders.clear();
        targetStriderCount = MIN_STRIDERS + random.nextInt(MAX_STRIDERS - MIN_STRIDERS + 1);
    }

    /**
     * Get current progress info
     */
    public String getProgressInfo() {
        MinecraftClient client = MinecraftClient.getInstance();
        int current = countNearbyStrideSurfers(client);
        String status = maxReached ? "MAX REACHED" : current + "/" + targetStriderCount;
        return "StrideSurfers: " + status;
    }

    /**
     * Check if max has been reached
     */
    public boolean isMaxReached() {
        return maxReached;
    }
}
