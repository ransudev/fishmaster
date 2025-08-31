package rohan.fishmaster.utils;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class for angle calculations and rotations.
 * Provides methods for normalizing angles, calculating differences, and smooth transitions.
 */
public class AngleUtils {

    /**
     * Converts a yaw angle to a 0-360 degree range.
     */
    public static float get360RotationYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    /**
     * Normalizes an angle to the range (-180, 180].
     */
    public static float normalizeAngle(float angle) {
        while (angle > 180) {
            angle -= 360;
        }
        while (angle <= -180) {
            angle += 360;
        }
        return angle;
    }

    /**
     * Normalizes a yaw angle to the range (-180, 180].
     */
    public static float normalizeYaw(float yaw) {
        float newYaw = yaw % 360F;
        if (newYaw < -180F) {
            newYaw += 360F;
        }
        if (newYaw > 180F) {
            newYaw -= 360F;
        }
        return newYaw;
    }

    /**
     * Calculates the smallest angle difference between two angles.
     */
    public static float smallestAngleDifference(float from, float to) {
        float diff = normalizeAngle(to - from);
        return Math.abs(diff);
    }

    /**
     * Gets the shortest rotation path between two angles.
     * Returns the signed difference that represents the shortest rotation.
     */
    public static float getShortestRotationPath(float from, float to) {
        return normalizeAngle(to - from);
    }

    /**
     * Cubic easing function that mimics natural mouse movement.
     * Starts slow, accelerates in the middle, then decelerates at the end.
     */
    public static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            float f = 2.0f * t - 2.0f;
            return 1.0f + f * f * f / 2.0f;
        }
    }

    /**
     * Clamps a pitch angle to valid Minecraft range (-90 to 90 degrees).
     */
    public static float clampPitch(float pitch) {
        return MathHelper.clamp(pitch, -90.0f, 90.0f);
    }

    /**
     * Performs smooth interpolation between two angles using the shortest path.
     *
     * @param fromYaw Starting yaw angle
     * @param fromPitch Starting pitch angle
     * @param toYaw Target yaw angle
     * @param toPitch Target pitch angle
     * @param progress Interpolation progress (0.0 to 1.0)
     * @return Array containing [interpolated yaw, interpolated pitch]
     */
    public static float[] smoothRotationInterpolation(float fromYaw, float fromPitch,
                                                    float toYaw, float toPitch, float progress) {
        // Apply easing for natural movement
        float easedProgress = easeInOutCubic(progress);

        // Use shortest rotation path for yaw
        float yawDiff = getShortestRotationPath(fromYaw, toYaw);
        float pitchDiff = toPitch - fromPitch;

        float newYaw = fromYaw + (yawDiff * easedProgress);
        float newPitch = fromPitch + (pitchDiff * easedProgress);

        // Normalize and clamp angles
        newYaw = normalizeYaw(newYaw);
        newPitch = clampPitch(newPitch);

        return new float[]{newYaw, newPitch};
    }

    /**
     * Calculates the yaw angle from the origin to the given position.
     *
     * @param pos The target position
     * @return The calculated yaw angle
     */
    public static float getRotationYaw(Vec3d pos) {
        // Calculate yaw from origin to pos
        return (float)(Math.toDegrees(Math.atan2(pos.z, pos.x)) - 90.0F);
    }

    /**
     * Calculates the needed yaw change to rotate from one yaw to another.
     *
     * @param fromYaw The starting yaw
     * @param toYaw The target yaw
     * @return The yaw change needed
     */
    public static float getNeededYawChange(float fromYaw, float toYaw) {
        return getShortestRotationPath(fromYaw, toYaw);
    }
}
