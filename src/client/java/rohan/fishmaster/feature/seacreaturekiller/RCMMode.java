package rohan.fishmaster.feature.seacreaturekiller;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        }
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
