package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;


/**
 * Fire Veil Wand mode
 * - Switches to the Fire Veil Wand (by display name contains "fire veil wand")
 * - Activates it by right-clicking (item interact)
 * - Independent 5s cooldown per cast
 * - Does not need to click the ground (different than RCM)
 */
public class FireVeilWandMode extends SeaCreatureKillerMode {

    private static final long COOLDOWN_MS = 10000L; // 10 seconds
    private static long lastCastTime = 0L;


    @Override
    public void performAttack(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return;

        // Only act on known sea creatures from the shared TARGET_CREATURES list
        if (!rohan.fishmaster.feature.SeaCreatureKiller.isTargetSeaCreature(target)) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastCastTime < COOLDOWN_MS) {
            return; // Still on cooldown
        }

        if (!switchToFireVeilWand()) {
            client.player.sendMessage(net.minecraft.text.Text.literal("SCK: ")
                .formatted(net.minecraft.util.Formatting.RED)
                .append(net.minecraft.text.Text.literal("No Fire Veil Wand!")
                .formatted(net.minecraft.util.Formatting.YELLOW)), false);
            return;
        }

        // Activate the wand ability using mouse simulation instead of interactionManager.interactItem
        simulateRightClick(client);
        lastCastTime = now; // Start cooldown
    }

    @Override
    public String getModeName() {
        return "Fire Veil Wand";
    }

    private boolean switchToFireVeilWand() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        ItemStack current = client.player.getMainHandStack();
        if (isFireVeilWand(current)) return true;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isFireVeilWand(stack)) {
                client.player.getInventory().setSelectedSlot(i);
                return true;
            }
        }
        return false;
    }

    private boolean isFireVeilWand(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String display = stack.getName().getString();
        String lower = display.toLowerCase();
        if (display.equalsIgnoreCase("Fire Veil Wand")) return true;
        if (lower.contains("fire veil wand")) return true;
        // Fallback: check item registry name just in case
        String itemId = stack.getItem().toString().toLowerCase();
        return itemId.contains("fire_veil") || itemId.contains("fireveil");
    }
}
