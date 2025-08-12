package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * Fire Veil Wand mode - Work in Progress
 */
public class FireVeilWandMode extends SeaCreatureKillerMode {

    @Override
    public void performAttack(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Fire Veil Wand mode is work in progress
        client.player.sendMessage(net.minecraft.text.Text.literal("SCK: ")
            .formatted(net.minecraft.util.Formatting.YELLOW)
            .append(net.minecraft.text.Text.literal("Fire Veil Wand mode is Work In Progress!")
            .formatted(net.minecraft.util.Formatting.RED)), false);
    }

    @Override
    public String getModeName() {
        return "Fire Veil Wand";
    }
}
