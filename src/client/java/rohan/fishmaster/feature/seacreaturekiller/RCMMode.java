package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rohan.fishmaster.utils.AngleUtils;

/**
 * RCM (Right Click Mage) mode - Uses mage weapons by right-clicking the ground
 */
public class RCMMode extends SeaCreatureKillerMode {

    // Smooth rotation transition variables
    private float originalYaw = 0.0f;
    private float originalPitch = 0.0f;
    private boolean isTransitioning = false;
    private long transitionStartTime = 0;
    private static final long TRANSITION_DURATION = 350;
    private float transitionStartYaw = 0.0f;
    private float transitionStartPitch = 0.0f;
    private boolean isTransitioningToGround = false;
    private boolean canAttack = false;

    // Weapon switching variables
    private long lastWeaponSwitchTime = 0;
    private static final long WEAPON_SWITCH_DELAY = 400;
    private static final long COMBAT_TO_FISHING_DELAY = 200;
    private boolean needsToSwitchBack = false;
    private long combatEndTime = 0;
    private int originalSlot = -1;

    @Override
    public void enterCombat(Entity target) {
        this.targetEntity = target;
        this.inCombatMode = true;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Store original rotation and slot
            originalYaw = client.player.getYaw();
            originalPitch = client.player.getPitch();
            originalSlot = client.player.getInventory().getSelectedSlot();

            // Start transition to look at ground
            transitionStartYaw = client.player.getYaw();
            transitionStartPitch = client.player.getPitch();
            isTransitioningToGround = true;
            transitionStartTime = System.currentTimeMillis();
            canAttack = false;

            lastWeaponSwitchTime = System.currentTimeMillis();
            needsToSwitchBack = false;

            sendCombatMessage(getEntityDisplayName(target));
        }
    }

    @Override
    public void performCombat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Handle weapon switching back to fishing rod after combat
        if (needsToSwitchBack && System.currentTimeMillis() - combatEndTime > COMBAT_TO_FISHING_DELAY) {
            switchBackToFishingRod();
        }

        // Check if target is still valid
        if (targetEntity != null && (targetEntity.isRemoved() ||
            client.player.distanceTo(targetEntity) > DETECTION_RANGE)) {
            exitCombat();
            return;
        }

        // Update rotations if transitioning
        if (isTransitioning) {
            updateRotation();
        }

        if (isTransitioningToGround) {
            updateStartupRotation();
        }

        // Attack if we can
        if (targetEntity != null && !isTransitioning && canAttack && canAttack()) {
            attackGround();
        }
    }

    @Override
    public void exitCombat() {
        if (inCombatMode) {
            combatEndTime = System.currentTimeMillis();
            needsToSwitchBack = true;
            startRotationTransition();
        }

        targetEntity = null;
        inCombatMode = false;
        canAttack = false;
    }

    @Override
    public String getModeName() {
        return "RCM";
    }

    private void updateRotation() {
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

    private void updateStartupRotation() {
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

    private void startRotationTransition() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        transitionStartYaw = AngleUtils.normalizeYaw(client.player.getYaw());
        transitionStartPitch = AngleUtils.clampPitch(client.player.getPitch());
        isTransitioning = true;
        transitionStartTime = System.currentTimeMillis();
    }

    private void attackGround() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        if (!switchToMageWeapon()) {
            if (client.player != null) {
                client.player.sendMessage(net.minecraft.text.Text.literal("SCK: ")
                    .formatted(net.minecraft.util.Formatting.RED)
                    .append(net.minecraft.text.Text.literal("No mage weapon!")
                    .formatted(net.minecraft.util.Formatting.YELLOW)), false);
            }
            return;
        }

        Vec3d playerPos = client.player.getPos();
        BlockPos groundPos = new BlockPos((int)playerPos.x, (int)playerPos.y - 1, (int)playerPos.z);

        while (groundPos.getY() > client.player.getBlockY() - 5 &&
               client.world.getBlockState(groundPos).isAir()) {
            groundPos = groundPos.down();
        }

        if (client.interactionManager != null) {
            BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(groundPos),
                Direction.UP,
                groundPos,
                false
            );

            ActionResult result = client.interactionManager.interactBlock(
                client.player,
                Hand.MAIN_HAND,
                hitResult
            );

            if (result == ActionResult.PASS) {
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            }

            updateAttackTime();
        }
    }

    private boolean switchToMageWeapon() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeaponSwitchTime < WEAPON_SWITCH_DELAY) {
            return false;
        }

        ItemStack currentItem = client.player.getMainHandStack();
        if (isMageWeapon(currentItem)) {
            return true;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isMageWeapon(stack)) {
                client.player.getInventory().setSelectedSlot(i);
                lastWeaponSwitchTime = currentTime;
                return true;
            }
        }

        return false;
    }

    private void switchBackToFishingRod() {
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
        } else {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (isFishingRod(stack)) {
                    client.player.getInventory().setSelectedSlot(i);
                    lastWeaponSwitchTime = currentTime;
                    needsToSwitchBack = false;
                    originalSlot = -1;
                    break;
                }
            }
        }
    }

    private boolean isMageWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        String displayName = stack.getName().getString().toLowerCase();
        String customWeapon = rohan.fishmaster.config.FishMasterConfig.getCustomMageWeapon().toLowerCase();

        if (customWeapon.isEmpty()) return false;

        return displayName.contains(customWeapon);
    }

    private boolean isFishingRod(ItemStack stack) {
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
}
