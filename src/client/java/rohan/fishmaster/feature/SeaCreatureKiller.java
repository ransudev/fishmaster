package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import rohan.fishmaster.utils.AngleUtils;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SeaCreatureKiller {
    private static SeaCreatureKiller instance;

    private static boolean enabled = false;
    private static boolean autoFishEnabled = false; // Track if autofish is enabled
    private static Entity targetEntity = null;
    private static long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 250; // Faster attack rate for combat mode
    private static final double DETECTION_RANGE = 6.0;
    private static int killCount = 0;
    private static boolean inCombatMode = false;

    // Smooth rotation transition variables with mouse-like movement
    private static float originalYaw = 0.0f;
    private static float originalPitch = 0.0f;
    private static boolean isTransitioning = false;
    private static long transitionStartTime = 0;
    private static final long TRANSITION_DURATION = 350; // Reduced from 800ms to 300ms for faster transitions
    private static float transitionStartYaw = 0.0f;
    private static float transitionStartPitch = 0.0f;
    private static boolean isTransitioningToGround = false;
    private static boolean canAttack = false; // Flag to control when attacking is allowed

    // Weapon switching delay variables - optimized for efficiency
    private static long lastWeaponSwitchTime = 0;
    private static final long WEAPON_SWITCH_DELAY = 400; // Reduced from 1200ms to 400ms for faster weapon switches
    private static final long COMBAT_TO_FISHING_DELAY = 200; // Reduced from 2000ms to 800ms for faster return to fishing rod
    private static int originalSlot = -1; // Remember original fishing rod slot
    private static boolean needsToSwitchBack = false;
    private static long combatEndTime = 0;

    // Set of specific sea creature names to target
    private static final Set<String> TARGET_CREATURES = new HashSet<>();

    static {
        TARGET_CREATURES.add("Squid");
        TARGET_CREATURES.add("Sea Walker");
        TARGET_CREATURES.add("Night Squid");
        TARGET_CREATURES.add("Sea Guardian");
        TARGET_CREATURES.add("Sea Witch");
        TARGET_CREATURES.add("Sea Archer");
        TARGET_CREATURES.add("Rider of the Deep");
        TARGET_CREATURES.add("Catfish");
        TARGET_CREATURES.add("Carrot King");
        TARGET_CREATURES.add("Agarimoo");
        TARGET_CREATURES.add("Sea Leech");
        TARGET_CREATURES.add("Guardian Defender");
        TARGET_CREATURES.add("Deep Sea Protector");
        TARGET_CREATURES.add("Water Hydra");
        TARGET_CREATURES.add("Sea Emperor");
        TARGET_CREATURES.add("Oasis Rabbit");
        TARGET_CREATURES.add("Oasis Sheep");
        TARGET_CREATURES.add("Water Worm");
        TARGET_CREATURES.add("Poisoned Water Worm");
        TARGET_CREATURES.add("Abyssal Miner");
        TARGET_CREATURES.add("Scarecrow");
        TARGET_CREATURES.add("Nightmare");
        TARGET_CREATURES.add("Werewolf");
        TARGET_CREATURES.add("Phantom Fisher");
        TARGET_CREATURES.add("Grim Reaper");
        TARGET_CREATURES.add("Frozen Steve");
        TARGET_CREATURES.add("Frosty");
        TARGET_CREATURES.add("Grinch");
        TARGET_CREATURES.add("Yeti");
        TARGET_CREATURES.add("Nutcracker");
        TARGET_CREATURES.add("Reindrake");
        TARGET_CREATURES.add("Nurse Shark");
        TARGET_CREATURES.add("Blue Shark");
        TARGET_CREATURES.add("Tiger Shark");
        TARGET_CREATURES.add("Great White Shark");
        TARGET_CREATURES.add("Trash Gobbler");
        TARGET_CREATURES.add("Dumpster Diver");
        TARGET_CREATURES.add("Bayou Sludge");
        TARGET_CREATURES.add("Titanoboa");
        TARGET_CREATURES.add("Flaming Worm");
        TARGET_CREATURES.add("Lava Blaze");
        TARGET_CREATURES.add("Lava Pigman");
        TARGET_CREATURES.add("Magma Slug");
        TARGET_CREATURES.add("Moogma");
        TARGET_CREATURES.add("Lava Leech");
        TARGET_CREATURES.add("Pyroclastic Worm");
        TARGET_CREATURES.add("Lava Flame");
        TARGET_CREATURES.add("Fire Eel");
        TARGET_CREATURES.add("Taurus");
        TARGET_CREATURES.add("Plhlegblast");
        TARGET_CREATURES.add("Thunder");
        TARGET_CREATURES.add("Lord Jawbus");
        TARGET_CREATURES.add("The Loch Emperor");
        TARGET_CREATURES.add("Alligator");
        TARGET_CREATURES.add("Banshee");
        TARGET_CREATURES.add("Blue Ringed Octopus");
        TARGET_CREATURES.add("Snapping Turtle");
        TARGET_CREATURES.add("Wiki Tiki");
        TARGET_CREATURES.add("Frog man");
        TARGET_CREATURES.add("Bayou Sludgling");
    }

    public static boolean isEnabled() {
        // Only enabled if both the feature is toggled AND autofish is enabled
        return enabled && autoFishEnabled;
    }



    public static void toggle() {
        // Allow toggling regardless of autofish state
        enabled = !enabled;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (enabled) {
                if (autoFishEnabled) {
                    // SeaCreatureKiller is enabled and autofish is active - fully functional
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.GREEN)
                        .append(Text.literal("ON").formatted(Formatting.BOLD, Formatting.GREEN)), false);
                    killCount = 0;
                } else {
                    // SeaCreatureKiller is enabled but autofish is not active - standby mode
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("STANDBY").formatted(Formatting.BOLD, Formatting.YELLOW)), false);
                }
            } else {
                client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.RED)
                    .append(Text.literal("OFF").formatted(Formatting.BOLD, Formatting.RED)), false);
                // Reset combat state when disabled
                targetEntity = null;
                inCombatMode = false;
                isTransitioning = false;
            }
        }
    }

    public static void setAutoFishEnabled(boolean active) {
        autoFishEnabled = active;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (active) {
                // When autofish starts, check if sea creature killer is already enabled
                if (enabled) {
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.GREEN)
                        .append(Text.literal("ACTIVE").formatted(Formatting.BOLD, Formatting.GREEN)), false);
                    killCount = 0;
                }
            } else {
                // When autofish stops, sea creature killer goes into standby mode
                if (enabled) {
                    // Clear combat state but keep enabled flag
                    targetEntity = null;
                    inCombatMode = false;
                    isTransitioning = false;

                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("STANDBY").formatted(Formatting.BOLD, Formatting.YELLOW)), false);
                }
            }
        }
    }

    public static void tick() {
        // Only run if both enabled and autofish is active
        if (!isEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        // Handle weapon switching back to fishing rod after combat
        if (needsToSwitchBack && System.currentTimeMillis() - combatEndTime > COMBAT_TO_FISHING_DELAY) {
            switchBackToFishingRod();
        }

        // Check if current target is still valid
        if (targetEntity != null && (targetEntity.isRemoved() || !isTargetSeaCreature(targetEntity) ||
            client.player.distanceTo(targetEntity) > DETECTION_RANGE)) {

            if (inCombatMode) {
                // Combat ended - set timer to switch back to fishing rod
                combatEndTime = System.currentTimeMillis();
                needsToSwitchBack = true;

                // Start smooth transition back to original rotation
                startRotationTransition();
            }

            targetEntity = null;
            inCombatMode = false;
            canAttack = false; // Reset attack flag
        }

        // Find new target if we don't have one and not transitioning
        if (targetEntity == null && !isTransitioning) {
            findNearestTargetCreature();
        }

        // Enter combat mode and attack if we have a target
        if (targetEntity != null && !isTransitioning) {
            if (!inCombatMode) {
                enterCombatMode();
            }

            // Only attack if rotation to ground is complete and enough time has passed
            if (canAttack && System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN) {
                attackGround();
            }
        }

        // Update rotation if transitioning
        if (isTransitioning) {
            updateRotation();
        }

        // Handle startup transition to ground
        if (isTransitioningToGround) {
            updateStartupRotation();
        }
    }

    // Enhanced smooth rotation with proper angle handling
    private static void updateRotation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Calculate the rotation progress
        long currentTime = System.currentTimeMillis();
        float progress = MathHelper.clamp((currentTime - transitionStartTime) / (float)TRANSITION_DURATION, 0.0f, 1.0f);

        // Use AngleUtils for smooth rotation interpolation
        float[] rotationResult = AngleUtils.smoothRotationInterpolation(
            transitionStartYaw, transitionStartPitch,
            originalYaw, originalPitch,
            progress
        );

        float newYaw = rotationResult[0];
        float newPitch = rotationResult[1];

        // Update the player's rotation
        client.player.setYaw(newYaw);
        client.player.setPitch(newPitch);

        // If transition is complete, stop transitioning
        if (progress >= 1.0f) {
            isTransitioning = false;
            // Ensure final position is exact
            client.player.setYaw(AngleUtils.normalizeYaw(originalYaw));
            client.player.setPitch(originalPitch);
        }
    }

    private static void updateStartupRotation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Calculate the rotation progress
        long currentTime = System.currentTimeMillis();
        float progress = MathHelper.clamp((currentTime - transitionStartTime) / (float)TRANSITION_DURATION, 0.0f, 1.0f);

        // Apply smooth easing for natural movement
        float easedProgress = AngleUtils.easeInOutCubic(progress);

        // Calculate target ground-looking pitch (90 degrees down)
        float targetPitch = 90.0f;

        // Use shortest rotation path for smooth yaw transition (keep current yaw mostly)
        float currentYaw = client.player.getYaw();
        float yawDiff = AngleUtils.getShortestRotationPath(transitionStartYaw, currentYaw);

        // Smoothly interpolate to look at ground
        float newYaw = transitionStartYaw + (yawDiff * easedProgress * 0.1f); // Minimal yaw adjustment
        float newPitch = transitionStartPitch + ((targetPitch - transitionStartPitch) * easedProgress);

        // Normalize and clamp angles
        newYaw = AngleUtils.normalizeYaw(newYaw);
        newPitch = AngleUtils.clampPitch(newPitch);

        // Update the player's rotation
        client.player.setYaw(newYaw);
        client.player.setPitch(newPitch);

        // If transition is complete, stop transitioning and allow attacking
        if (progress >= 1.0f) {
            isTransitioningToGround = false;
            canAttack = true; // Now we can start attacking
            // Ensure final position is exact
            client.player.setPitch(90.0f);
        }
    }

    // Enhanced transition starter with better angle capture
    private static void startRotationTransition() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Store the current rotation as the starting point for transition
        transitionStartYaw = AngleUtils.normalizeYaw(client.player.getYaw());
        transitionStartPitch = AngleUtils.clampPitch(client.player.getPitch());

        // Start the transition
        isTransitioning = true;
        transitionStartTime = System.currentTimeMillis();
    }

    private static void enterCombatMode() {
        // Store the original rotation before entering combat mode
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            originalYaw = client.player.getYaw();
            originalPitch = client.player.getPitch();

            // Remember current slot (likely fishing rod)
            originalSlot = client.player.getInventory().getSelectedSlot();

            // Start smooth transition to ground when entering combat mode
            transitionStartYaw = client.player.getYaw();
            transitionStartPitch = client.player.getPitch();
            isTransitioningToGround = true;
            transitionStartTime = System.currentTimeMillis();
            canAttack = false; // Don't allow attacking until rotation is complete

            // Reset weapon switch timer to add initial delay before switching to mage weapon
            lastWeaponSwitchTime = System.currentTimeMillis();
        }

        inCombatMode = true;
        needsToSwitchBack = false; // Reset switch back flag
        if (client.player != null) {
            client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.RED)
                .append(Text.literal("COMBAT").formatted(Formatting.BOLD, Formatting.RED))
                .append(Text.literal(" - " + getEntityDisplayName(targetEntity)).formatted(Formatting.YELLOW)), false);
        }
    }

    private static void findNearestTargetCreature() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        Vec3d playerPos = client.player.getPos();
        Box searchBox = new Box(
            playerPos.x - DETECTION_RANGE, playerPos.y - DETECTION_RANGE, playerPos.z - DETECTION_RANGE,
            playerPos.x + DETECTION_RANGE, playerPos.y + DETECTION_RANGE, playerPos.z + DETECTION_RANGE
        );

        List<Entity> entities = client.world.getOtherEntities(client.player, searchBox, SeaCreatureKiller::isTargetSeaCreature);

        Entity nearestCreature = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            double distance = client.player.distanceTo(entity);
            if (distance < nearestDistance && distance <= DETECTION_RANGE) {
                nearestDistance = distance;
                nearestCreature = entity;
            }
        }

        targetEntity = nearestCreature;
    }

    private static boolean isTargetSeaCreature(Entity entity) {
        if (entity == null) return false;

        String entityName = getEntityDisplayName(entity);

        // Check if the entity name matches any of our target creatures
        for (String targetName : TARGET_CREATURES) {
            if (entityName.contains(targetName)) {
                return true;
            }
        }

        // Also check for basic Minecraft entities that might be sea creatures
        return entity instanceof SquidEntity ||
               entity instanceof GlowSquidEntity ||
               entity instanceof GuardianEntity ||
               entity instanceof ElderGuardianEntity;
    }

    private static String getEntityDisplayName(Entity entity) {
        if (entity == null) return "";

        // Try to get the display name first
        if (entity.hasCustomName() && entity.getCustomName() != null) {
            return entity.getCustomName().getString();
        }

        // Get the entity type name
        String typeName = entity.getType().getTranslationKey();

        // Remove the "entity.minecraft." prefix if present
        if (typeName.startsWith("entity.minecraft.")) {
            typeName = typeName.substring("entity.minecraft.".length());
        }

        // Convert underscores to spaces and capitalize
        typeName = typeName.replace("_", " ");
        String[] words = typeName.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    private static void attackGround() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        // Check if we have a mage weapon and switch to it with delay
        if (!switchToMageWeapon()) {
            // No mage weapon found, send message and return
            if (client.player != null) {
                client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.RED)
                    .append(Text.literal("No mage weapon!").formatted(Formatting.YELLOW)), false);
            }
            return;
        }

        // Player should already be looking at ground from the rotation transition
        // No need to call lookAtGround() here since rotation is handled by updateStartupRotation()

        // Find ground position to attack
        Vec3d playerPos = client.player.getPos();
        BlockPos groundPos = new BlockPos((int)playerPos.x, (int)playerPos.y - 1, (int)playerPos.z);

        // Find the actual ground level
        while (groundPos.getY() > client.player.getBlockY() - 5 &&
               client.world.getBlockState(groundPos).isAir()) {
            groundPos = groundPos.down();
        }

        // Right-click the ground with mage weapon
        if (client.interactionManager != null) {
            BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(groundPos),
                Direction.UP,
                groundPos,
                false
            );

            // Try to interact with the block (right-click)
            ActionResult result = client.interactionManager.interactBlock(
                client.player,
                Hand.MAIN_HAND,
                hitResult
            );

            // If block interaction didn't work, try using the item directly
            if (result == ActionResult.PASS) {
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            }

            lastAttackTime = System.currentTimeMillis();
        }
    }

    private static boolean switchToMageWeapon() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        // Check if enough time has passed since last switch for realistic delay
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeaponSwitchTime < WEAPON_SWITCH_DELAY) {
            return false; // Still waiting for delay - don't allow any weapon operations
        }

        // Check if current item is already a mage weapon
        ItemStack currentItem = client.player.getMainHandStack();
        if (isMageWeapon(currentItem)) {
            return true;
        }


        // Search for mage weapon in hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isMageWeapon(stack)) {
                // Switch to this slot with realistic delay
                client.player.getInventory().setSelectedSlot(i);
                lastWeaponSwitchTime = currentTime;
                return true;
            }
        }

        // No mage weapon found in hotbar
        return false;
    }

    private static void switchBackToFishingRod() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Only switch back if we're not in combat and enough time has passed
        if (inCombatMode || originalSlot == -1) {
            return;
        }

        // Check if enough time has passed since last weapon switch
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeaponSwitchTime < WEAPON_SWITCH_DELAY) {
            return; // Still waiting for delay
        }

        // Switch back to original slot (fishing rod)
        ItemStack originalItem = client.player.getInventory().getStack(originalSlot);
        if (isFishingRod(originalItem)) {
            client.player.getInventory().setSelectedSlot(originalSlot);
            lastWeaponSwitchTime = currentTime;
            needsToSwitchBack = false;
            originalSlot = -1; // Reset
        } else {
            // Original slot doesn't have fishing rod anymore, find one
            for (int i = 0; i < 9; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (isFishingRod(stack)) {
                    client.player.getInventory().setSelectedSlot(i);
                    lastWeaponSwitchTime = currentTime;
                    needsToSwitchBack = false;
                    originalSlot = -1; // Reset
                    break;
                }
            }
        }
    }

    private static boolean isMageWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        String displayName = stack.getName().getString().toLowerCase();
        String customWeapon = rohan.fishmaster.config.FishMasterConfig.getCustomMageWeapon().toLowerCase();
        
        // If no custom mage weapon is set, return false (no mage weapon detection)
        if (customWeapon.isEmpty()) {
            return false;
        }
        
        // Check if the item matches the custom mage weapon
        return displayName.contains(customWeapon);
    }

    private static boolean isFishingRod(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        String itemName = stack.getItem().toString().toLowerCase();
        String displayName = stack.getName().getString().toLowerCase();

        // Check for fishing rod patterns
        return itemName.contains("fishing_rod") ||
               displayName.contains("fishing rod") ||
               displayName.contains("rod of the sea") ||
               displayName.contains("auger rod") ||
               displayName.contains("prismarine rod") ||
               displayName.contains("winter rod") ||
               displayName.contains("challenging rod") ||
               displayName.contains("lucky rod") ||
               displayName.contains("magma rod") ||
               displayName.contains("lava rod") ||
               displayName.contains("salty rod") ||
               displayName.contains("rod of legends") ||
               displayName.contains("rod of championing");
    }

    private static void lookAtGround() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Set pitch to look down at the ground (90 degrees down)
        float targetPitch = 90.0f;

        // Keep the current yaw (don't change horizontal rotation)
        float currentYaw = client.player.getYaw();

        // Set the player's rotation to look at the ground
        client.player.setYaw(currentYaw);
        client.player.setPitch(targetPitch);
    }



    public static int getKillCount() {
        return killCount;
    }

    public static Entity getCurrentTarget() {
        return targetEntity;
    }

    public static void reset() {
        enabled = false;
        targetEntity = null;
        killCount = 0;
        lastAttackTime = 0;
        inCombatMode = false;
        isTransitioning = false;
        isTransitioningToGround = false;
        canAttack = false; // Reset attack flag
        // Reset weapon switching variables
        needsToSwitchBack = false;
        originalSlot = -1;
        combatEndTime = 0;
        lastWeaponSwitchTime = 0;
    }

    public static SeaCreatureKiller getInstance() {
        if (instance == null) {
            instance = new SeaCreatureKiller();
        }
        return instance;
    }
}
