package rohan.fishmaster.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.SeaCreatureKiller;
import rohan.fishmaster.handler.WebhookHandler;

public class FishMasterCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("fm")
            .executes(FishMasterCommand::showStatus));

        dispatcher.register(ClientCommandManager.literal("fishmaster")
            .executes(FishMasterCommand::showStatus));

        // Register webhook commands
        dispatcher.register(ClientCommandManager.literal("fmwh")
            .then(ClientCommandManager.literal("set")
                .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                    .executes(FishMasterCommand::setWebhookUrl)))
            .then(ClientCommandManager.literal("enable")
                .executes(FishMasterCommand::enableWebhook))
            .then(ClientCommandManager.literal("disable")
                .executes(FishMasterCommand::disableWebhook))
            .then(ClientCommandManager.literal("test")
                .executes(FishMasterCommand::testWebhook))
            .then(ClientCommandManager.literal("interval")
                .then(ClientCommandManager.argument("minutes", IntegerArgumentType.integer(1, 60))
                    .executes(FishMasterCommand::setHealthCheckInterval)))
            .then(ClientCommandManager.literal("clear")
                .executes(FishMasterCommand::clearWebhook))
            .executes(FishMasterCommand::showWebhookStatus));
    }

    private static int showStatus(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        // Show current status instead of opening GUI
        boolean autoFishStatus = AutoFishingFeature.getInstance().isEnabled();
        boolean seaCreatureStatus = SeaCreatureKiller.getInstance().isEnabled();

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

        client.player.sendMessage(
            Text.literal("Webhook: ").formatted(Formatting.GRAY)
                .append(Text.literal(FishMasterConfig.isWebhookEnabled() ? "ON" : "OFF")
                    .formatted(FishMasterConfig.isWebhookEnabled() ? Formatting.GREEN : Formatting.RED)), false);

        return 1;
    }

    private static int setWebhookUrl(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        String url = StringArgumentType.getString(context, "url");

        // Validate URL format
        if (!url.startsWith("https://discord.com/api/webhooks/") &&
            !url.startsWith("https://discordapp.com/api/webhooks/")) {
            client.player.sendMessage(
                Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                    .append(Text.literal("Invalid webhook URL! Must be a Discord webhook URL.").formatted(Formatting.RED)), false);
            return 0;
        }

        FishMasterConfig.setWebhookUrl(url);
        FishMasterConfig.setWebhookEnabled(true);

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Webhook URL set and enabled!").formatted(Formatting.GREEN)), false);

        // Update webhook handler with new settings
        WebhookHandler.getInstance().updateWebhookSettings();

        return 1;
    }

    private static int enableWebhook(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        if (FishMasterConfig.getWebhookUrl().isEmpty()) {
            client.player.sendMessage(
                Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                    .append(Text.literal("No webhook URL set! Use '/fmwh set <url>' first.").formatted(Formatting.RED)), false);
            return 0;
        }

        FishMasterConfig.setWebhookEnabled(true);
        WebhookHandler.getInstance().updateWebhookSettings();

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Webhook enabled! Health checks will start in ")
                    .append(Text.literal(String.valueOf(FishMasterConfig.getHealthCheckInterval() / 60000)))
                    .append(Text.literal(" minutes.")).formatted(Formatting.GREEN)), false);

        return 1;
    }

    private static int disableWebhook(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        FishMasterConfig.setWebhookEnabled(false);
        WebhookHandler.getInstance().updateWebhookSettings();

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Webhook disabled! Health checks stopped.").formatted(Formatting.YELLOW)), false);

        return 1;
    }

    private static int testWebhook(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        if (!FishMasterConfig.isWebhookEnabled() || FishMasterConfig.getWebhookUrl().isEmpty()) {
            client.player.sendMessage(
                Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                    .append(Text.literal("Webhook not configured! Use '/fmwh set <url>' first.").formatted(Formatting.RED)), false);
            return 0;
        }

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Sending test message...").formatted(Formatting.YELLOW)), false);

        WebhookHandler.getInstance().sendTestMessage();

        return 1;
    }

    private static int setHealthCheckInterval(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        int minutes = IntegerArgumentType.getInteger(context, "minutes");
        long intervalMs = minutes * 60000L;

        FishMasterConfig.setHealthCheckInterval(intervalMs);

        if (FishMasterConfig.isWebhookEnabled() && !FishMasterConfig.getWebhookUrl().isEmpty()) {
            WebhookHandler.getInstance().updateWebhookSettings();
        }

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Health check interval set to " + minutes + " minutes.").formatted(Formatting.GREEN)), false);

        return 1;
    }

    private static int clearWebhook(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        FishMasterConfig.setWebhookUrl("");
        FishMasterConfig.setWebhookEnabled(false);
        WebhookHandler.getInstance().updateWebhookSettings();

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Webhook URL cleared and disabled.").formatted(Formatting.YELLOW)), false);

        return 1;
    }

    private static int showWebhookStatus(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return 0;
        }

        boolean webhookEnabled = FishMasterConfig.isWebhookEnabled();
        String webhookUrl = FishMasterConfig.getWebhookUrl();
        long intervalMinutes = FishMasterConfig.getHealthCheckInterval() / 60000;

        client.player.sendMessage(
            Text.literal("[FishMaster] ").formatted(Formatting.AQUA)
                .append(Text.literal("Webhook Status:").formatted(Formatting.WHITE)), false);

        client.player.sendMessage(
            Text.literal("Enabled: ").formatted(Formatting.GRAY)
                .append(Text.literal(webhookEnabled ? "YES" : "NO")
                    .formatted(webhookEnabled ? Formatting.GREEN : Formatting.RED)), false);

        client.player.sendMessage(
            Text.literal("URL: ").formatted(Formatting.GRAY)
                .append(Text.literal(webhookUrl.isEmpty() ? "Not set" : "Set")
                    .formatted(webhookUrl.isEmpty() ? Formatting.RED : Formatting.GREEN)), false);

        client.player.sendMessage(
            Text.literal("Health Check Interval: ").formatted(Formatting.GRAY)
                .append(Text.literal(intervalMinutes + " minutes").formatted(Formatting.YELLOW)), false);

        client.player.sendMessage(
            Text.literal("Commands:").formatted(Formatting.YELLOW), false);
        client.player.sendMessage(
            Text.literal("  /fmwh set <url> - Set webhook URL").formatted(Formatting.GRAY), false);
        client.player.sendMessage(
            Text.literal("  /fmwh enable/disable - Toggle webhook").formatted(Formatting.GRAY), false);
        client.player.sendMessage(
            Text.literal("  /fmwh test - Send test message").formatted(Formatting.GRAY), false);
        client.player.sendMessage(
            Text.literal("  /fmwh interval <minutes> - Set health check interval").formatted(Formatting.GRAY), false);
        client.player.sendMessage(
            Text.literal("  /fmwh clear - Clear webhook URL").formatted(Formatting.GRAY), false);

        return 1;
    }
}
