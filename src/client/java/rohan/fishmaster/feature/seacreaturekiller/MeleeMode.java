package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rohan.fishmaster.config.FishMasterConfig;

/**
 * Melee mode - Work in Progress
 */
public class MeleeMode extends SeaCreatureKillerMode {

    // Track last time we switched to the melee weapon to avoid instant attacking
    private long lastWeaponSelectTime = 0L;
    private static final long POST_SWITCH_DELAY_MS = 200L;
    // 5 clicks per second => 200ms between attacks
    private static final long ATTACK_INTERVAL_MS = 200L;
    private static final double ATTACK_RANGE_SQ = 3.2 * 3.2; // ~3.2 blocks reach
    private long lastAttackTime = 0L;

    @Override
    public void performAttack(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null || target == null) return;

        // Resolve nametag/marker entities to the actual sea creature (LivingEntity) below
        target = resolveActualTarget(target);

        // Ensure melee weapon is selected
        boolean justSwitched = ensureMeleeWeaponSelected();

        // Use duration-based easing rotation when available
        rohan.fishmaster.utils.RotationHandler rot = rohan.fishmaster.utils.RotationHandler.getInstance();
        if (!rot.isEnabled()) {
            // Trigger a short ease on first acquire or if handler is idle
            rot.easeTo(target, 180L);
        } else {
            // While handler is running, let it drive the rotation; avoid double-updating here
        }
        // Also perform small corrective look to closest hitbox point if handler finished
        if (!rot.isEnabled()) {
            lookAtTarget(target);
        }

        // Keep player stationary by zeroing horizontal velocity each tick to prevent drift
        try {
            client.player.setVelocity(0.0, client.player.getVelocity().y, 0.0);
        } catch (Throwable ignored) {}

        // If we just switched to weapon, wait a small delay before attacking
        if (justSwitched || System.currentTimeMillis() - lastWeaponSelectTime < POST_SWITCH_DELAY_MS) {
            return;
        }

        // Throttle to 5 CPS
        long now = System.currentTimeMillis();
        if (now - lastAttackTime < ATTACK_INTERVAL_MS) return;
        // Optional: ensure not at absolute zero cooldown to remain semi-legit
        if (client.player.getAttackCooldownProgress(0.0f) < 0.15f) return;

        // Range and basic line-of-sight check to keep it legit
        if (!withinHitReach(target)) return;
        // Ensure crosshair is actually on the mob before clicking; allow a small tolerance fallback
        boolean crosshairOk = isCrosshairOnTarget(target);
        boolean lowerBodyRayOk = rayHitsLowerBody(target);
        boolean aimedOk = isAimedWithinTolerance(target, 6.0f, 6.0f);
        if (!(crosshairOk || lowerBodyRayOk || aimedOk)) return;

        // Attack using interactionManager (legit left-click, no manual packets)
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        lastAttackTime = now;
    }

    @Override
    public String getModeName() {
        return "Melee";
    }

    private boolean ensureMeleeWeaponSelected() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        ItemStack current = client.player.getMainHandStack();
        if (isMeleeWeapon(current)) return false;

        String wanted = FishMasterConfig.getCustomMeleeWeapon().toLowerCase();
        if (wanted.isEmpty()) return false;

        // Search hotbar for the configured melee weapon by display name contains
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String name = stack.getName().getString().toLowerCase();
                if (name.contains(wanted)) {
                    client.player.getInventory().setSelectedSlot(i);
                    lastWeaponSelectTime = System.currentTimeMillis();
                    return true;
                }
            }
        }
        return false;
    }

    private void lookAtTarget(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Vec3d eyePos = client.player.getEyePos();
        Vec3d aimPoint = aimPointBelowNametag(target.getBoundingBox());
        Vec3d targetPos = aimPoint;

        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        if (distXZ < 1.0e-3 && Math.abs(dy) < 1.0e-3) {
            // Too close to compute a stable angle; keep current view
            return;
        }

        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));

        // Current rotation
        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();

        // Compute shortest path yaw diff and pitch diff
        float yawDiff = rohan.fishmaster.utils.AngleUtils.getShortestRotationPath(currentYaw, targetYaw);
        float pitchDiff = targetPitch - currentPitch;

        // Eased step fraction based on how far we are from the target
        float absYaw = Math.abs(yawDiff);
        float absPitch = Math.abs(pitchDiff);

        float maxYawStep = 18.0f;     // faster acquisition
        float maxPitchStep = 12.0f;   // faster pitch movement

        float yawProgress = MathHelper.clamp(absYaw / maxYawStep, 0.0f, 1.0f);
        float pitchProgress = MathHelper.clamp(absPitch / maxPitchStep, 0.0f, 1.0f);

        float easedYaw = rohan.fishmaster.utils.AngleUtils.easeInOutCubic(yawProgress);
        float easedPitch = rohan.fishmaster.utils.AngleUtils.easeInOutCubic(pitchProgress);

        float yawStep = MathHelper.clamp(yawDiff, -maxYawStep, maxYawStep) * easedYaw;
        float pitchStep = MathHelper.clamp(pitchDiff, -maxPitchStep, maxPitchStep) * easedPitch;

        float newYaw = currentYaw + yawStep;
        float newPitch = currentPitch + pitchStep;

        client.player.setYaw(rohan.fishmaster.utils.AngleUtils.normalizeYaw(newYaw));
        client.player.setPitch((float)newPitch);
    }

    // If the provided target is a non-attackable name tag entity hovering above the mob,
    // find the nearest LivingEntity directly below it and use that instead.
    private Entity resolveActualTarget(Entity original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || original == null) return original;
        if (original instanceof LivingEntity) return original;

        Box search = original.getBoundingBox().offset(0.0, -2.0, 0.0).expand(1.0, 2.5, 1.0);
        Entity best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Entity e : client.world.getOtherEntities(null, search)) {
            if (!(e instanceof LivingEntity)) continue;
            if (e == client.player) continue;
            double d = e.squaredDistanceTo(original);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = e;
            }
        }
        return best != null ? best : original;
    }

    private boolean isCrosshairOnTarget(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;
        if (client.crosshairTarget == null) return false;
        if (client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult ehr) {
            return ehr.getEntity() == target;
        }
        return false;
    }

    private boolean isAimedWithinTolerance(Entity target, float yawTolDeg, float pitchTolDeg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        Vec3d eyePos = client.player.getEyePos();
        Vec3d aimPoint = aimPointBelowNametag(target.getBoundingBox());

        double dx = aimPoint.x - eyePos.x;
        double dy = aimPoint.y - eyePos.y;
        double dz = aimPoint.z - eyePos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));

        float yawNow = client.player.getYaw();
        float pitchNow = client.player.getPitch();
        float yawDiff = wrapDegrees(targetYaw - yawNow);
        float pitchDiff = MathHelper.clamp(targetPitch, -90.0f, 90.0f) - MathHelper.clamp(pitchNow, -90.0f, 90.0f);

        // Optional LOS check; ignore errors on mappings
        try {
            if (!client.player.canSee(target)) {
                return false;
            }
        } catch (Throwable ignored) {}

        return Math.abs(yawDiff) <= yawTolDeg && Math.abs(pitchDiff) <= pitchTolDeg;
    }

    // Raycast from eye along look vector and check intersection with the lower portion of the target's hitbox
    private boolean rayHitsLowerBody(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        Vec3d eyePos = client.player.getEyePos();
        Vec3d look = client.player.getRotationVec(1.0f);
        double reach;
        try {
            boolean creative = client.interactionManager != null && client.interactionManager.getCurrentGameMode() != null && client.interactionManager.getCurrentGameMode().isCreative();
            reach = creative ? 5.0 : 3.0;
        } catch (Throwable ignored) {
            reach = 3.0;
        }
        Vec3d end = eyePos.add(look.multiply(reach));

        Box full = target.getBoundingBox();
        double height = Math.max(0.0, full.maxY - full.minY);
        double cutoff = height < 0.6 ? full.maxY : (full.maxY - 1.0);
        Box lower = new Box(full.minX, full.minY, full.minZ, full.maxX, cutoff, full.maxZ).expand(0.02);

        return lower.raycast(eyePos, end).isPresent();
    }

    private boolean withinHitReach(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return false;
        Vec3d eyePos = client.player.getEyePos();
        Vec3d closest = closestPointOnAABB(eyePos, target.getBoundingBox());
        double dx = closest.x - eyePos.x;
        double dy = closest.y - eyePos.y;
        double dz = closest.z - eyePos.z;
        double distSq = dx*dx + dy*dy + dz*dz;
        double reach;
        try {
            boolean creative = client.interactionManager.getCurrentGameMode() != null
                    && client.interactionManager.getCurrentGameMode().isCreative();
            reach = creative ? 5.0 : 3.0;
        } catch (Throwable ignored) {
            reach = 3.0;
        }
        double reachSq = reach * reach;
        return distSq <= reachSq + 1e-6; // small epsilon
    }

    // Compute closest point on an AABB to a given point
    private static Vec3d closestPointOnAABB(Vec3d point, Box box) {
        double cx = MathHelper.clamp(point.x, box.minX, box.maxX);
        double cy = MathHelper.clamp(point.y, box.minY, box.maxY);
        double cz = MathHelper.clamp(point.z, box.minZ, box.maxZ);
        return new Vec3d(cx, cy, cz);
    }

    // More robust aim point that avoids singularities when we are inside/very close to the hitbox
    private static Vec3d aimPointBelowNametag(Box box) {
        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        double height = Math.max(0.0, box.maxY - box.minY);
        double y = box.maxY - 1.0; // a full block below the top of hitbox
        if (height < 0.6) {
            // Very small mobs: aim at mid-height to avoid overshoot
            y = (box.minY + box.maxY) * 0.5;
        }
        y = MathHelper.clamp(y, box.minY, box.maxY);
        return new Vec3d(cx, y, cz);
    }

    private float stepTowards(float current, float target, float maxStep) {
        float diff = target - current;
        diff = MathHelper.clamp(diff, -maxStep, maxStep);
        return current + diff;
    }

    private float stepTowardsAngle(float current, float target, float maxStep) {
        float diff = wrapDegrees(target - current);
        diff = MathHelper.clamp(diff, -maxStep, maxStep);
        return current + diff;
    }

    private float wrapDegrees(float value) {
        value = value % 360.0f;
        if (value >= 180.0f) value -= 360.0f;
        if (value < -180.0f) value += 360.0f;
        return value;
    }
}
