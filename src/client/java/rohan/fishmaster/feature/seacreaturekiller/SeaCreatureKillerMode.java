package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Base class for Sea Creature Killer modes
 * Now only handles attack logic - combat entry/exit is managed by SeaCreatureKiller
 */
public abstract class SeaCreatureKillerMode {
    protected static final double DETECTION_RANGE = 6.0;

    /**
     * Perform the attack for this mode
     * @param target The entity to attack
     */
    public abstract void performAttack(Entity target);

    /**
     * Get the display name for this mode
     * @return Mode name for display
     */
    public abstract String getModeName();

    /**
     * Check if the player can attack (cooldown check)
     * @return true if attack is ready
     */
    protected boolean canAttack() {
        // This is now handled by SeaCreatureKiller centrally
        return true;
    }

    /**
     * Update the last attack time - deprecated, handled centrally now
     */
    @Deprecated
    protected void updateAttackTime() {
        // No longer needed - handled centrally
    }

    /**
     * Send combat message - deprecated, handled centrally now
     */
    @Deprecated
    protected void sendCombatMessage(String entityName) {
        // No longer needed - handled centrally
    }

    /**
     * Get entity display name helper
     */
    protected String getEntityDisplayName(Entity entity) {
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

    // Weapon detection helper methods
    protected boolean isMageWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        String displayName = stack.getName().getString().toLowerCase();
        String customWeapon = rohan.fishmaster.config.FishMasterConfigNew.getCustomMageWeapon().toLowerCase();

        if (customWeapon.isEmpty()) return false;
        return displayName.contains(customWeapon);
    }

    protected boolean isFishingRod(ItemStack stack) {
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
               displayName.contains("rod of champions");
    }
}
