package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

/**
 * RCM (Right Click Mage) mode - Uses mage weapons by right-clicking the ground
 */
public class RCMMode extends SeaCreatureKillerMode {

    @Override
    public void performAttack(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        if (!switchToMageWeapon()) {
            client.player.sendMessage(net.minecraft.text.Text.literal("SCK: ")
                .formatted(net.minecraft.util.Formatting.RED)
                .append(net.minecraft.text.Text.literal("No mage weapon!")
                .formatted(net.minecraft.util.Formatting.YELLOW)), false);
            return;
        }

        // Look down and use mouse simulation for right-click
        float originalPitch = client.player.getPitch();
        client.player.setPitch(90.0f); // Look straight down
        
        simulateRightClick(client);
        
        // Restore original look direction
        client.player.setPitch(originalPitch);
    }

    @Override
    public String getModeName() {
        return "RCM";
    }

    private boolean switchToMageWeapon() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        ItemStack currentItem = client.player.getMainHandStack();
        if (isMageWeapon(currentItem)) {
            return true;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isMageWeapon(stack)) {
                client.player.getInventory().setSelectedSlot(i);
                return true;
            }
        }

        return false;
    }
}
