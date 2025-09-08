package rohan.fishmaster.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import rohan.fishmaster.utils.AngleUtils;
import net.minecraft.util.math.Box;

public class EntityUtil {
    /**
     * Returns all living entities in the world matching the given target creature names.
     * Only returns valid, alive, non-player entities.
     */
    public static List<LivingEntity> getTargetCreatures(Set<String> targetNames) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<LivingEntity> entities = new ArrayList<>();
        if (client.world == null || client.player == null) return entities;
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity living && !living.isDead() && living.isAlive() && !living.equals(client.player)) {
                String name = entity.getName().getString();
                boolean matches = targetNames.stream().anyMatch(name::contains);
                if (matches) {
                    entities.add(living);
                }
            }
        }
        return entities;
    }
    // Stub for getEntityCuttingOtherEntity
    public static Entity getEntityCuttingOtherEntity(Entity e, Class<?> entityType) {
        // TODO: Implement for 1.21.5
        return null;
    }
    // Stub for getHealthFromStandName
    public static int getHealthFromStandName(String name) {
        // TODO: Implement for 1.21.5
        return 1;
    }
    public static Entity resolveActualTarget(Entity original) {
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
}
