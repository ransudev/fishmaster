package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SeaCreatureKiller {
    private static boolean enabled = false;
    private static boolean passiveMode = false;
    private static Entity targetEntity = null;
    private static long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 250; // Faster attack rate for combat mode
    private static final double DETECTION_RANGE = 7.0; // 7 block range as requested
    private static int killCount = 0;
    private static boolean inCombatMode = false;

    // Smooth rotation transition variables
    private static float originalYaw = 0.0f;
    private static float originalPitch = 0.0f;
    private static boolean isTransitioning = false;
    private static long transitionStartTime = 0;
    private static final long TRANSITION_DURATION = 300; // Faster 300ms transition (was 1000ms)
    private static float transitionStartYaw = 0.0f;
    private static float transitionStartPitch = 0.0f;
    private static boolean isTransitioningToGround = false; // New: for startup transition

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
        TARGET_CREATURES.add("Banshee");
        TARGET_CREATURES.add("Bayou Sludge");
        TARGET_CREATURES.add("Alligator");
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
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        if (passiveMode) {
            // If in passive mode, don't allow manual toggle
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Sea Creature Killer is in passive mode (controlled by Auto Fishing)")
                    .formatted(Formatting.YELLOW), false);
            }
            return;
        }

        enabled = !enabled;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (enabled) {
                client.player.sendMessage(Text.literal("Sea Creature Killer: ").formatted(Formatting.GREEN)
                    .append(Text.literal("ENABLED").formatted(Formatting.BOLD, Formatting.GREEN)), false);
                killCount = 0;
            } else {
                client.player.sendMessage(Text.literal("Sea Creature Killer: ").formatted(Formatting.RED)
                    .append(Text.literal("DISABLED").formatted(Formatting.BOLD, Formatting.RED)), false);
                targetEntity = null;
                inCombatMode = false;
                isTransitioning = false; // Stop any ongoing transition
            }
        }
    }

    public static void setPassiveMode(boolean active) {
        passiveMode = active;
        enabled = active;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (active) {
                client.player.sendMessage(Text.literal("Sea Creature Killer: ").formatted(Formatting.AQUA)
                    .append(Text.literal("PASSIVE MODE ACTIVATED").formatted(Formatting.BOLD, Formatting.AQUA))
                    .append(Text.literal(" (Auto Fishing)").formatted(Formatting.GRAY)), false);
                killCount = 0;
            } else {
                client.player.sendMessage(Text.literal("Sea Creature Killer: ").formatted(Formatting.GRAY)
                    .append(Text.literal("PASSIVE MODE DEACTIVATED").formatted(Formatting.BOLD, Formatting.GRAY)), false);
                targetEntity = null;
                inCombatMode = false;
                isTransitioning = false; // Stop any ongoing transition
            }
        }
    }

    public static void tick() {
        if (!enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        // Check if current target is still valid
        if (targetEntity != null && (targetEntity.isRemoved() || !isTargetSeaCreature(targetEntity) ||
            client.player.distanceTo(targetEntity) > DETECTION_RANGE)) {

            if (inCombatMode) {
                client.player.sendMessage(Text.literal("Combat Mode: ").formatted(Formatting.RED)
                    .append(Text.literal("DEACTIVATED").formatted(Formatting.BOLD, Formatting.GRAY))
                    .append(Text.literal(" - Target lost").formatted(Formatting.GRAY)), false);

                // Start smooth transition back to original rotation
                startRotationTransition();
            }

            targetEntity = null;
            inCombatMode = false;
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

            if (System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN) {
                attackGround();
            }
        }

        // Update rotation if transitioning
        if (isTransitioning) {
            updateRotation();
        }

        // New: Handle startup transition to ground
        if (isTransitioningToGround) {
            updateStartupRotation();
        }
    }

    private static void startRotationTransition() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Store the current rotation as the starting point for transition
        transitionStartYaw = client.player.getYaw();
        transitionStartPitch = client.player.getPitch();

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

            // Start smooth transition to ground when entering combat mode
            transitionStartYaw = client.player.getYaw();
            transitionStartPitch = client.player.getPitch();
            isTransitioningToGround = true;
            transitionStartTime = System.currentTimeMillis();
        }

        inCombatMode = true;
        if (client.player != null) {
            client.player.sendMessage(Text.literal("Combat Mode: ").formatted(Formatting.RED)
                .append(Text.literal("ACTIVATED").formatted(Formatting.BOLD, Formatting.RED))
                .append(Text.literal(" - Target: " + getEntityDisplayName(targetEntity)).formatted(Formatting.YELLOW)), false);
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
        if (entity.hasCustomName()) {
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
            if (result.length() > 0) result.append(" ");
            if (word.length() > 0) {
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

        // Check if we have a mage weapon and switch to it
        if (!switchToMageWeapon()) {
            // No mage weapon found, send message and return
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Combat Mode: ").formatted(Formatting.RED)
                    .append(Text.literal("No mage weapon found!").formatted(Formatting.YELLOW)), false);
            }
            return;
        }

        // Look at the ground
        lookAtGround();

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

        // Check if current item is already a mage weapon
        ItemStack currentItem = client.player.getMainHandStack();
        if (isMageWeapon(currentItem)) {
            return true;
        }

        // Search for mage weapon in hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isMageWeapon(stack)) {
                // Switch to this slot
                client.player.getInventory().setSelectedSlot(i);
                return true;
            }
        }

        // No mage weapon found in hotbar
        return false;
    }

    private static boolean isMageWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        String itemName = stack.getItem().toString().toLowerCase();
        String displayName = stack.getName().getString().toLowerCase();

        // Check for common mage weapon patterns
        return itemName.contains("staff") ||
               itemName.contains("wand") ||
               itemName.contains("orb") ||
               itemName.contains("rod") && !itemName.contains("fishing") || // Rod but not fishing rod
               displayName.contains("staff") ||
               displayName.contains("wand") ||
               displayName.contains("orb") ||
               displayName.contains("mage") ||
               displayName.contains("magic") ||
               displayName.contains("spirit") ||
               displayName.contains("yeti") ||
               displayName.contains("sword") && (displayName.contains("mage") || displayName.contains("magic")) ||
               // Hypixel Skyblock specific mage weapons
               displayName.contains("bonzo") ||
               displayName.contains("frozen scythe") ||
               displayName.contains("staff") ||
               displayName.contains("wither") && displayName.contains("shield") ||
               displayName.contains("jerry") ||
               displayName.contains("midas") ||
               displayName.contains("spirit") ||
               displayName.contains("scorpion") ||
               displayName.contains("fire") && displayName.contains("rod") ||
               displayName.contains("ice") && displayName.contains("rod") ||
               displayName.contains("thunder") && displayName.contains("rod") ||
               // New specific mage weapons
               displayName.contains("scylla") ||
               displayName.contains("valkyrie") ||
               displayName.contains("astrea") ||
               displayName.contains("midas's staff") ||
               displayName.contains("midas staff") ||
               displayName.contains("fire veil wand") ||
               displayName.contains("fire veil") ||
               displayName.contains("hyperion");
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

    private static void updateRotation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Calculate the rotation progress
        long currentTime = System.currentTimeMillis();
        float progress = MathHelper.clamp((currentTime - transitionStartTime) / (float)TRANSITION_DURATION, 0.0f, 1.0f);

        // Smoothly interpolate the yaw and pitch
        float newYaw = MathHelper.lerp(progress, transitionStartYaw, originalYaw);
        float newPitch = MathHelper.lerp(progress, transitionStartPitch, originalPitch);

        // Update the player's rotation
        client.player.setYaw(newYaw);
        client.player.setPitch(newPitch);

        // If transition is complete, stop transitioning
        if (progress >= 1.0f) {
            isTransitioning = false;
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

        // Smoothly interpolate the pitch to look at the ground
        float newPitch = MathHelper.lerp(progress, transitionStartPitch, 90.0f);

        // Update the player's rotation
        client.player.setPitch(newPitch);

        // If transition is complete, stop transitioning
        if (progress >= 1.0f) {
            isTransitioningToGround = false;
        }
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
        isTransitioning = false; // Stop any ongoing transition
        isTransitioningToGround = false; // Stop startup transition
    }
}
