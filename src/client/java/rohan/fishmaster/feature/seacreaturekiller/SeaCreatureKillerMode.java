package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import rohan.fishmaster.config.FishMasterConfig;

/**
 * Abstract base class for different Sea Creature Killer attack modes
 */
public abstract class SeaCreatureKillerMode {

    protected static final double DETECTION_RANGE = 6.0;
    protected static final long ATTACK_COOLDOWN = 250;

    protected Entity targetEntity;
    protected long lastAttackTime = 0;
    public boolean inCombatMode = false;

    /**
     * Called when entering combat mode with a target
     */
    public abstract void enterCombat(Entity target);

    /**
     * Called every tick to perform combat actions
     */
    public abstract void performCombat();

    /**
     * Called when exiting combat mode
     */
    public abstract void exitCombat();

    /**
     * Returns the display name for this mode
     */
    public abstract String getModeName();

    /**
     * Checks if this mode can currently attack
     */
    protected boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN;
    }

    /**
     * Updates the last attack time
     */
    protected void updateAttackTime() {
        lastAttackTime = System.currentTimeMillis();
    }

    /**
     * Sends a combat message to the player
     */
    protected void sendCombatMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.RED)
                .append(Text.literal("COMBAT").formatted(Formatting.BOLD, Formatting.RED))
                .append(Text.literal(" - " + message).formatted(Formatting.YELLOW)), false);
        }
    }

    /**
     * Gets the display name of an entity
     */
    protected String getEntityDisplayName(Entity entity) {
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

    /**
     * Gets the player's position
     */
    protected Vec3d getPlayerPosition() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null ? client.player.getPos() : Vec3d.ZERO;
    }
}
