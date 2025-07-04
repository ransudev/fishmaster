package rohan.fishmaster.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.SeaCreatureKiller;

public class FishMasterCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("fm")
            .executes(FishMasterCommand::showStatus));

        dispatcher.register(ClientCommandManager.literal("fishmaster")
            .executes(FishMasterCommand::showStatus));
    }

    private static int showStatus(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        // Show current status instead of opening GUI
        boolean autoFishStatus = AutoFishingFeature.isEnabled();
        boolean seaCreatureStatus = SeaCreatureKiller.isEnabled();

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Status:").formatted(Formatting.WHITE)), false);

        client.player.sendMessage(
            Text.literal("Auto Fishing: ").formatted(Formatting.GRAY)
                .append(Text.literal(autoFishStatus ? "ON" : "OFF")
                    .formatted(autoFishStatus ? Formatting.GREEN : Formatting.RED)), false);

        client.player.sendMessage(
            Text.literal("Sea Creature Killer: ").formatted(Formatting.GRAY)
                .append(Text.literal(seaCreatureStatus ? "ON" : "OFF")
                    .formatted(seaCreatureStatus ? Formatting.GREEN : Formatting.RED)), false);

        return 1;
    }
}
