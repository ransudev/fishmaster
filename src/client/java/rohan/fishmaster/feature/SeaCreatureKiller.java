package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.feature.seacreaturekiller.*;
import rohan.fishmaster.utils.AngleUtils;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SeaCreatureKiller {
    private static SeaCreatureKiller instance;
    private static boolean enabled = false;
    private static boolean autoFishEnabled = false;
    private static Entity targetEntity = null;
    private static final double DETECTION_RANGE = 6.0;
    private static int killCount = 0;
    
    // Combat state variables
    private static boolean inCombatMode = false;
    private static long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 500; // 500ms between attacks

    // Rotation and weapon switching variables (managed centrally)
    private static float originalYaw = 0.0f;
    private static float originalPitch = 0.0f;
    private static boolean isTransitioning = false;
    private static long transitionStartTime = 0;
    private static final long TRANSITION_DURATION = 350;
    private static float transitionStartYaw = 0.0f;
    private static float transitionStartPitch = 0.0f;
    private static boolean isTransitioningToGround = false;
    private static boolean canAttack = false;

    // Weapon switching variables
    private static long lastWeaponSwitchTime = 0;
    private static final long WEAPON_SWITCH_DELAY = 400;
    private static final long COMBAT_TO_FISHING_DELAY = 200;
    private static boolean needsToSwitchBack = false;
    private static long combatEndTime = 0;
    private static int originalSlot = -1;

    // Mode system - only for attack methods
    private static SeaCreatureKillerMode currentMode;
    private static RCMMode rcmMode = new RCMMode();
    private static FireVeilWandMode fireVeilWandMode = new FireVeilWandMode();
    private static StrideSurferMode strideSurferMode = new StrideSurferMode();

    // Set of specific sea creature names to target
    private static final Set<String> TARGET_CREATURES = new HashSet<>();

    static {
        // Initialize target creatures list
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
        TARGET_CREATURES.add("Frog Man");
        TARGET_CREATURES.add("Bogged");
        TARGET_CREATURES.add("Tadgang");
        TARGET_CREATURES.add("Wetwing");
        TARGET_CREATURES.add("Ent");
        TARGET_CREATURES.add("Tidetot");
        TARGET_CREATURES.add("Stridersurfer");
        
        // Set RCM as default mode
        updateMode();
    }

    // Public accessors for FishingTracker and other features
    public static boolean isTargetCreature(String name) {
        return name != null && TARGET_CREATURES.contains(name);
    }

    public static Set<String> getTargetCreatures() {
        return java.util.Collections.unmodifiableSet(TARGET_CREATURES);
    }

    public static boolean isEnabled() {
        return enabled && autoFishEnabled;
    }

    public static void toggle() {
        enabled = !enabled;
        FishMasterConfig.setSeaCreatureKillerEnabled(enabled);

        String status = enabled ? "ENABLED" : "DISABLED";
        MinecraftClient.getInstance().player.sendMessage(Text.literal("[FishMaster] Sea Creature Killer: " + status).formatted(enabled ? Formatting.GREEN : Formatting.RED), false);
        
        if (!enabled) {
            exitCombat();
        }
    }

    public static void setEnabled(boolean newState) {
        if (enabled != newState) {
            enabled = newState;
            FishMasterConfig.setSeaCreatureKillerEnabled(enabled);

            if (MinecraftClient.getInstance().player != null) {
                String status = enabled ? "ENABLED" : "DISABLED";
                MinecraftClient.getInstance().player.sendMessage(Text.literal("[FishMaster] Sea Creature Killer: " + status).formatted(enabled ? Formatting.GREEN : Formatting.RED), false);
            }
            
            if (!enabled) {
                exitCombat();
            }
        }
    }

    public static void setAutoFishEnabled(boolean active) {
        autoFishEnabled = active;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (active) {
                if (enabled) {
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.GREEN)
                        .append(Text.literal("ACTIVE").formatted(Formatting.BOLD, Formatting.GREEN))
                        .append(Text.literal(" [" + (currentMode != null ? currentMode.getModeName() : "Unknown") + "]").formatted(Formatting.GRAY)), false);
                    killCount = 0;
                }
            } else {
                if (enabled) {
                    exitCombat();
                    targetEntity = null;
                    
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("STANDBY").formatted(Formatting.BOLD, Formatting.YELLOW)), false);
                }
            }
        }
    }

    /**
     * Updates the current mode based on config setting
     */
    private static void updateMode() {
        String mode = FishMasterConfig.getSeaCreatureKillerMode();
        switch (mode) {
            case "Fire Veil Wand":
                currentMode = fireVeilWandMode;
                break;
            case "StrideSurfer":
                currentMode = strideSurferMode;
                break;
            case "RCM":
            default:
                currentMode = rcmMode;
                break;
        }
    }

    /**
     * Reset StrideSurfer mode for a new cycle
     */
    public static void resetStrideSurferMode() {
        if (strideSurferMode != null) {
            strideSurferMode.resetForNewCycle();
        }
    }

    public static void tick() {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Update rotation handler easing each tick
        rohan.fishmaster.utils.RotationHandler.getInstance().tick();

        // Update mode if it changed in config
        updateMode();

        // Check if current target is still valid (only while in combat mode)
        if (inCombatMode && targetEntity != null && (targetEntity.isRemoved() || !isTargetSeaCreature(targetEntity) ||
            client.player.distanceTo(targetEntity) > DETECTION_RANGE)) {
            exitCombat();
            return;
        }

        // Handle post-combat transitions and weapon switching
        if (!inCombatMode) {
            // Handle weapon switching back to fishing rod after combat
            if (needsToSwitchBack && System.currentTimeMillis() - combatEndTime > COMBAT_TO_FISHING_DELAY) {
                switchBackToFishingRod();
            }

            // Update rotations if transitioning back to original position
            if (isTransitioning) {
                updateRotationTransition();
                return;
            }

            // Find new target if we don't have one and not transitioning
            if (targetEntity == null) {
                findNearestTargetCreature();
            }

            // Enter combat mode if we have a target
            if (targetEntity != null) {
                enterCombat(targetEntity);
            }
            return;
        }

        // Combat mode active - handle transitions and attacks

        // Update rotations if transitioning to ground (for RCM mode)
        if (isTransitioningToGround) {
            updateStartupRotation();
            return; // Don't attack until transition is complete
        }

        // Delegate attack to the current mode
        if (targetEntity != null && canAttack() && currentMode != null) {
            currentMode.performAttack(targetEntity);
            lastAttackTime = System.currentTimeMillis();
        }
    }

    /**
     * Centralized combat entry logic
     */
    private static void enterCombat(Entity target) {
        targetEntity = target;
        inCombatMode = true;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Store original rotation and slot for restoration later
            originalYaw = client.player.getYaw();
            originalPitch = client.player.getPitch();
            originalSlot = client.player.getInventory().getSelectedSlot();

            String mode = FishMasterConfig.getSeaCreatureKillerMode();

            if ("RCM".equals(mode)) {
                // Start transition to look at ground for RCM mode
                transitionStartYaw = client.player.getYaw();
                transitionStartPitch = client.player.getPitch();
                isTransitioningToGround = true;
                transitionStartTime = System.currentTimeMillis();
                canAttack = false;
            } else {
                // For melee modes, we can attack immediately
                canAttack = true;
                
                // Only rotate for modes other than Fire Veil Wand
                if (!"Fire Veil Wand".equals(mode)) {
                    // Calculate look angles to target and smoothly rotate towards it
                    if (client.player != null) {
                        double diffX = target.getX() - client.player.getX();
                        double diffY = target.getY() + target.getHeight() / 2.0 - (client.player.getY() + client.player.getEyeHeight(client.player.getPose()));
                        double diffZ = target.getZ() - client.player.getZ();

                        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
                        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
                        float pitch = (float)-(Math.atan2(diffY, dist) * 180.0 / Math.PI);

                        // Smoothly rotate towards the target
                        rohan.fishmaster.utils.RotationHandler.getInstance().easeTo(yaw, pitch, 400L);
                    }
                }
            }

            lastWeaponSwitchTime = System.currentTimeMillis();
            needsToSwitchBack = false;

            sendCombatMessage(getEntityDisplayName(target));
        }
    }

    /**
     * Centralized combat exit logic with fishing resumption
     */
    private static void exitCombat() {
        if (inCombatMode) {
            combatEndTime = System.currentTimeMillis();
            needsToSwitchBack = true;

            // Only rotate back to original orientation for modes other than Fire Veil Wand
            String mode = FishMasterConfig.getSeaCreatureKillerMode();
            if (!"Fire Veil Wand".equals(mode)) {
                // Always rotate back to original orientation after combat for legit visuals
                startRotationTransition();
            }

            killCount++;
        }

        targetEntity = null;
        inCombatMode = false;
        canAttack = false;
        isTransitioningToGround = false;
    }

    private static boolean canAttack() {
        return canAttack && System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN;
    }

    private static void sendCombatMessage(String entityName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("SCK: ")
                .formatted(Formatting.RED)
                .append(Text.literal("Attacking " + entityName)
                .formatted(Formatting.YELLOW)), false);
        }
    }

    // Rotation transition methods for RCM mode
    private static void updateRotationTransition() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        long currentTime = System.currentTimeMillis();
        float progress = MathHelper.clamp((currentTime - transitionStartTime) / (float)TRANSITION_DURATION, 0.0f, 1.0f);

        float[] rotationResult = AngleUtils.smoothRotationInterpolation(
            transitionStartYaw, transitionStartPitch,
            originalYaw, originalPitch,
            progress
        );

        client.player.setYaw(rotationResult[0]);
        client.player.setPitch(rotationResult[1]);

        if (progress >= 1.0f) {
            isTransitioning = false;
            client.player.setYaw(AngleUtils.normalizeYaw(originalYaw));
            client.player.setPitch(originalPitch);
        }
    }

    private static void updateStartupRotation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        long currentTime = System.currentTimeMillis();
        float progress = MathHelper.clamp((currentTime - transitionStartTime) / (float)TRANSITION_DURATION, 0.0f, 1.0f);
        float easedProgress = AngleUtils.easeInOutCubic(progress);

        float targetPitch = 90.0f;
        float currentYaw = client.player.getYaw();
        float yawDiff = AngleUtils.getShortestRotationPath(transitionStartYaw, currentYaw);

        float newYaw = transitionStartYaw + (yawDiff * easedProgress * 0.1f);
        float newPitch = transitionStartPitch + ((targetPitch - transitionStartPitch) * easedProgress);

        newYaw = AngleUtils.normalizeYaw(newYaw);
        newPitch = AngleUtils.clampPitch(newPitch);

        client.player.setYaw(newYaw);
        client.player.setPitch(newPitch);

        if (progress >= 1.0f) {
            isTransitioningToGround = false;
            canAttack = true;
            client.player.setPitch(90.0f);
        }
    }

    private static void startRotationTransition() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        transitionStartYaw = AngleUtils.normalizeYaw(client.player.getYaw());
        transitionStartPitch = AngleUtils.clampPitch(client.player.getPitch());
        isTransitioning = true;
        transitionStartTime = System.currentTimeMillis();
    }

    private static void switchBackToFishingRod() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || inCombatMode || originalSlot == -1) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeaponSwitchTime < WEAPON_SWITCH_DELAY) return;

        ItemStack originalItem = client.player.getInventory().getStack(originalSlot);
        if (isFishingRod(originalItem)) {
            client.player.getInventory().setSelectedSlot(originalSlot);
            lastWeaponSwitchTime = currentTime;
            needsToSwitchBack = false;
            originalSlot = -1;

            // Do not auto-cast here; let AutoFishingFeature handle casting to avoid double right-click
        } else {
            // Find any fishing rod if original slot doesn't have one
            for (int i = 0; i < 9; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (isFishingRod(stack)) {
                    client.player.getInventory().setSelectedSlot(i);
                    lastWeaponSwitchTime = currentTime;
                    needsToSwitchBack = false;
                    originalSlot = -1;

                    // Do not auto-cast here; let AutoFishingFeature handle casting to avoid double right-click
                    break;
                }
            }
        }
    }

    // Removed auto-cast on combat end to prevent conflicting with AutoFishingFeature's casting

    private static boolean isFishingRod(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        String itemName = stack.getItem().toString().toLowerCase();
        String displayName = stack.getName().getString().toLowerCase();

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

    public static boolean isTargetSeaCreature(Entity entity) {
        if (entity == null) return false;

        String entityName = getEntityDisplayName(entity);

        for (String targetName : TARGET_CREATURES) {
            if (entityName.contains(targetName)) {
                return true;
            }
        }

        return entity instanceof SquidEntity ||
               entity instanceof GlowSquidEntity ||
               entity instanceof GuardianEntity ||
               entity instanceof ElderGuardianEntity;
    }

    private static String getEntityDisplayName(Entity entity) {
        if (entity == null) return "";

        if (entity.hasCustomName() && entity.getCustomName() != null) {
            return entity.getCustomName().getString();
        }

        String typeName = entity.getType().getTranslationKey();

        if (typeName.startsWith("entity.minecraft.")) {
            typeName = typeName.substring("entity.minecraft.".length());
        }

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

    public static int getKillCount() {
        return killCount;
    }

    public static Entity getCurrentTarget() {
        return targetEntity;
    }

    public static String getCurrentMode() {
        return currentMode != null ? currentMode.getModeName() : "Unknown";
    }

    public static void reset() {
        enabled = false;
        targetEntity = null;
        killCount = 0;
        
        // Combat management is now handled centrally, no need to call mode.exitCombat()
        exitCombat();
    }

    public static SeaCreatureKiller getInstance() {
        if (instance == null) {
            instance = new SeaCreatureKiller();
        }
        return instance;
    }

    public static void init() {
        enabled = FishMasterConfig.isSeaCreatureKillerEnabled();
        updateMode();
    }

    private static void findNearestTargetCreature() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

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
}
