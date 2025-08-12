package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Melee mode - Uses melee weapons to attack sea creatures directly
 * TODO: Implement melee attack logic
 */
public class MeleeMode extends SeaCreatureKillerMode {
    
    @Override
    public void enterCombat(Entity target) {
        this.targetEntity = target;
        this.inCombatMode = true;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.YELLOW)
                .append(Text.literal("MELEE MODE - Work In Progress").formatted(Formatting.BOLD, Formatting.YELLOW)), false);
        }
    }
    
    @Override
    public void performCombat() {
        // TODO: Implement melee combat logic
        // - Find and equip melee weapon
        // - Move towards target
        // - Attack target directly
        // - Handle target movement and pathfinding
        
        // For now, just exit combat to prevent getting stuck
        if (targetEntity != null && targetEntity.isRemoved()) {
            exitCombat();
        }
    }
    
    @Override
    public void exitCombat() {
        targetEntity = null;
        inCombatMode = false;
        
        // TODO: Implement cleanup logic
        // - Switch back to fishing rod
        // - Reset player movement/rotation
    }
    
    @Override
    public String getModeName() {
        return "Melee";
    }
}
