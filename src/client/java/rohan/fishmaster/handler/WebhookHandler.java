package rohan.fishmaster.handler;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.SeaCreatureKiller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WebhookHandler {
    private static WebhookHandler instance;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private boolean initialized = false;
    private long startTime;
    private ScheduledFuture<?> healthCheckTask;

    private WebhookHandler() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.startTime = System.currentTimeMillis();
    }

    public static WebhookHandler getInstance() {
        if (instance == null) {
            instance = new WebhookHandler();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        System.out.println("[FishMaster] WebhookHandler initialized");

        // Send startup message if webhook is enabled
        if (FishMasterConfig.isWebhookEnabled() && !FishMasterConfig.getWebhookUrl().isEmpty()) {
            sendStartupMessage();
            startHealthChecks();
        }
    }

    private void sendStartupMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        String playerName = client.player != null ? client.player.getName().getString() : "Unknown";

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "🐟 FishMaster Mod Started");
        embed.addProperty("description", "The FishMaster mod has been initialized and is ready to use.");
        embed.addProperty("color", 3066993); // Green color

        JsonObject field1 = new JsonObject();
        field1.addProperty("name", "Player");
        field1.addProperty("value", playerName);
        field1.addProperty("inline", true);

        JsonObject field2 = new JsonObject();
        field2.addProperty("name", "Started At");
        field2.addProperty("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        field2.addProperty("inline", true);

        JsonObject field3 = new JsonObject();
        field3.addProperty("name", "Health Check Interval");
        field3.addProperty("value", (FishMasterConfig.getHealthCheckInterval() / 60000) + " minutes");
        field3.addProperty("inline", true);

        com.google.gson.JsonArray fields = new com.google.gson.JsonArray();
        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        embed.add("fields", fields);

        embed.addProperty("timestamp", java.time.Instant.now().toString());

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "FishMaster v1.0");
        embed.add("footer", footer);

        sendWebhookMessage(embed);
        System.out.println("[FishMaster] Startup message sent to Discord webhook");
    }

    public void startHealthChecks() {
        if (healthCheckTask != null) {
            healthCheckTask.cancel(false);
        }

        if (!FishMasterConfig.isWebhookEnabled() || FishMasterConfig.getWebhookUrl().isEmpty()) {
            return;
        }

        long intervalMs = FishMasterConfig.getHealthCheckInterval();
        healthCheckTask = scheduler.scheduleAtFixedRate(this::sendHealthCheck, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        System.out.println("[FishMaster] Health checks started with interval: " + (intervalMs / 60000) + " minutes");
    }

    public void stopHealthChecks() {
        if (healthCheckTask != null) {
            healthCheckTask.cancel(false);
            healthCheckTask = null;
            System.out.println("[FishMaster] Health checks stopped");
        }
    }

    private void sendHealthCheck() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        String playerName = client.player.getName().getString();
        boolean isModActive = AutoFishingFeature.getInstance().isEnabled();
        boolean isPlayerKicked = !client.player.isAlive() || client.player.isRemoved();
        long uptime = System.currentTimeMillis() - startTime;

        JsonObject embed = new JsonObject();

        if (isPlayerKicked) {
            embed.addProperty("title", "⚠️ Player Status Alert");
            embed.addProperty("description", "Player appears to have been kicked or disconnected!");
            embed.addProperty("color", 15158332); // Red color
        } else {
            embed.addProperty("title", "💓 Health Check");
            embed.addProperty("description", "Regular status update from FishMaster mod");
            embed.addProperty("color", isModActive ? 3066993 : 15844367); // Green if active, gold if inactive
        }

        JsonObject field1 = new JsonObject();
        field1.addProperty("name", "Player");
        field1.addProperty("value", playerName);
        field1.addProperty("inline", true);

        JsonObject field2 = new JsonObject();
        field2.addProperty("name", "Mod Status");
        field2.addProperty("value", isModActive ? "🟢 Active" : "🔴 Inactive");
        field2.addProperty("inline", true);

        JsonObject field3 = new JsonObject();
        field3.addProperty("name", "Session Uptime");
        field3.addProperty("value", formatUptime(uptime));
        field3.addProperty("inline", true);

        JsonObject field4 = new JsonObject();
        field4.addProperty("name", "Timestamp");
        field4.addProperty("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        field4.addProperty("inline", true);

        com.google.gson.JsonArray fields = new com.google.gson.JsonArray();
        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        fields.add(field4);

        if (isPlayerKicked) {
            JsonObject field5 = new JsonObject();
            field5.addProperty("name", "Status");
            field5.addProperty("value", "🚨 DISCONNECTED");
            field5.addProperty("inline", true);
            fields.add(field5);
        }

        embed.add("fields", fields);
        embed.addProperty("timestamp", java.time.Instant.now().toString());

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "FishMaster Health Monitor");
        embed.add("footer", footer);

        sendWebhookMessage(embed);
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public void sendTestMessage() {
        if (!FishMasterConfig.isWebhookEnabled() || FishMasterConfig.getWebhookUrl().isEmpty()) {
            System.out.println("[FishMaster] Cannot send test message: Webhook not configured");
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String playerName = client.player != null ? client.player.getName().getString() : "Unknown";

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "🧪 Test Message");
        embed.addProperty("description", "This is a test message to verify the webhook configuration is working correctly.");
        embed.addProperty("color", 3447003); // Blue color

        JsonObject field = new JsonObject();
        field.addProperty("name", "Sent by");
        field.addProperty("value", playerName);
        field.addProperty("inline", true);

        embed.add("fields", new com.google.gson.JsonArray());
        embed.getAsJsonArray("fields").add(field);
        embed.addProperty("timestamp", java.time.Instant.now().toString());

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "FishMaster Test");
        embed.add("footer", footer);

        sendWebhookMessage(embed);
        System.out.println("[FishMaster] Test message sent successfully");
    }

    public void sendWebhookMessage(JsonObject embed) {
        String webhookUrl = FishMasterConfig.getWebhookUrl();
        if (webhookUrl.isEmpty() || !FishMasterConfig.isWebhookEnabled()) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.add("embeds", new com.google.gson.JsonArray());
        payload.getAsJsonArray("embeds").add(embed);

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 204) {
                    System.out.println("[FishMaster] Webhook message sent successfully");
                } else {
                    System.err.println("[FishMaster] Webhook failed with status: " + response.statusCode());
                    System.err.println("[FishMaster] Response: " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("[FishMaster] Failed to send webhook: " + e.getMessage());
            }
        }, scheduler);
    }

    public void updateWebhookSettings() {
        if (FishMasterConfig.isWebhookEnabled() && !FishMasterConfig.getWebhookUrl().isEmpty()) {
            startHealthChecks();
        } else {
            stopHealthChecks();
        }
    }

    public void shutdown() {
        stopHealthChecks();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Perform a health check - called by the responsive scheduler
     */
    public void performHealthCheck() {
        if (!FishMasterConfig.isWebhookEnabled() || FishMasterConfig.getWebhookUrl().isEmpty()) {
            return;
        }

        // Use the existing sendHealthCheck method
        sendHealthCheck();
    }
}
