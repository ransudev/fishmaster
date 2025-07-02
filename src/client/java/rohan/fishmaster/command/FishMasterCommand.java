package rohan.fishmaster.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.gui.FishMasterSettings;

public class FishMasterCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("fm")
            .executes(FishMasterCommand::openGui));

        dispatcher.register(ClientCommandManager.literal("fishmaster")
            .executes(FishMasterCommand::openGui));
    }

    private static int openGui(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        try {
            // Open the FishMaster settings GUI
            FishMasterSettings settings = FishMasterSettings.getInstance();
            settings.openGui();

            // Send confirmation message
            client.player.sendMessage(
                Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                    .append(Text.literal("Opening settings GUI...").formatted(Formatting.WHITE)),
                false
            );

        } catch (Exception e) {
            // Send error message if GUI fails to open
            client.player.sendMessage(
                Text.literal("[FishMaster] ").formatted(Formatting.RED)
                    .append(Text.literal("Failed to open GUI: " + e.getMessage()).formatted(Formatting.WHITE)),
                false
            );
        }

        return 1;
    }
}
