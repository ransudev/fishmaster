package rohan.fishmaster.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.feature.FishingTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishingTrackerHud {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(DrawContext context, int x, int y) {
        if (!FishMasterConfig.isFishingTrackerEnabled()) return;
        if (mc.player == null || !mc.player.getMainHandStack().isOf(Items.FISHING_ROD)) return;

        List<String> display = new ArrayList<>();
        Map<String, Integer> mobsCaught = FishingTracker.getMobsCaught();
        Map<String, Double> lastTimeCaught = FishingTracker.getLastTimeCaught();

        // Add header
        display.add("-----------------");

        // Add normal fishing creatures
        if (FishMasterConfig.getFishingTrackerType() == 0) {
            addNormalFishingMobs(display, mobsCaught);
        }

        // Add lava fishing creatures
        if (FishMasterConfig.getFishingTrackerType() == 1) {
            addLavaFishingMobs(display, mobsCaught);
        }

        // Add marina event creatures
        if (FishMasterConfig.isFishingTrackerMarinaEnabled()) {
            addMarinaEventMobs(display, mobsCaught);
        }

        // Add spooky fishing creatures
        if (FishMasterConfig.isFishingTrackerSpookyEnabled()) {
            addSpookyFishingMobs(display, mobsCaught);
        }

        // Add winter fishing creatures
        if (FishMasterConfig.isFishingTrackerWinterEnabled()) {
            addWinterFishingMobs(display, mobsCaught);
        }

        // Add footer
        display.add("-----------------");

        // Add time since last catch section if enabled
        if (FishMasterConfig.isFishingTrackerTimeSinceEnabled()) {
            display.add(Formatting.BOLD + "Time Since");
            addTimeSinceSection(display, lastTimeCaught);
            display.add("-----------------");
        }

        // Render the display
        TextRenderer textRenderer = mc.textRenderer;
        int yOffset = 0;
        for (String line : display) {
            context.drawTextWithShadow(textRenderer, Text.literal(line), x, y + yOffset, 0xFFFFFF);
            yOffset += 9;
        }
    }

    private static void addNormalFishingMobs(List<String> display, Map<String, Integer> mobsCaught) {
        addMobEntry(display, mobsCaught, "Squid", Formatting.WHITE);
        addMobEntry(display, mobsCaught, "Sea Walker", Formatting.WHITE);
        addMobEntry(display, mobsCaught, "Sea Guardian", Formatting.WHITE);
        addMobEntry(display, mobsCaught, "Sea Witch", Formatting.GREEN);
        addMobEntry(display, mobsCaught, "Sea Archer", Formatting.GREEN);
        addMobEntry(display, mobsCaught, "Rider Of The Deep", Formatting.GREEN);
        addMobEntry(display, mobsCaught, "Catfish", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Carrot King", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Sea Leech", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Guardian Defender", Formatting.DARK_PURPLE);
        addMobEntry(display, mobsCaught, "Deep Sea Protector", Formatting.DARK_PURPLE);
        addMobEntry(display, mobsCaught, "Water Hydra", Formatting.GOLD);
        addMobEntry(display, mobsCaught, "Sea Emperor", Formatting.GOLD);
    }

    private static void addLavaFishingMobs(List<String> display, Map<String, Integer> mobsCaught) {
        addMobEntry(display, mobsCaught, "Magma Slug", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Moogma", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Lava Leech", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Pyroclastic Worm", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Lava Flame", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Fire Eel", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Taurus", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Thunder", Formatting.LIGHT_PURPLE);
        addMobEntry(display, mobsCaught, "Lord Jawbus", Formatting.LIGHT_PURPLE);
    }

    private static void addMarinaEventMobs(List<String> display, Map<String, Integer> mobsCaught) {
        display.add("-----------------");
        addMobEntry(display, mobsCaught, "Nurse Shark", Formatting.RED);
        addMobEntry(display, mobsCaught, "Blue Shark", Formatting.RED);
        addMobEntry(display, mobsCaught, "Tiger Shark", Formatting.RED);
        addMobEntry(display, mobsCaught, "Great White Shark", Formatting.RED);
    }

    private static void addSpookyFishingMobs(List<String> display, Map<String, Integer> mobsCaught) {
        display.add("-----------------");
        addMobEntry(display, mobsCaught, "Scarecrow", Formatting.WHITE);
        addMobEntry(display, mobsCaught, "Nightmare", Formatting.GREEN);
        addMobEntry(display, mobsCaught, "Werewolf", Formatting.DARK_PURPLE);
        addMobEntry(display, mobsCaught, "Phantom Fisher", Formatting.GOLD);
        addMobEntry(display, mobsCaught, "Grim Reaper", Formatting.GOLD);
    }

    private static void addWinterFishingMobs(List<String> display, Map<String, Integer> mobsCaught) {
        display.add("-----------------");
        addMobEntry(display, mobsCaught, "Frozen Steve", Formatting.WHITE);
        addMobEntry(display, mobsCaught, "Frosty The Snowman", Formatting.WHITE);
        addMobEntry(display, mobsCaught, "Grinch", Formatting.GREEN);
        addMobEntry(display, mobsCaught, "Nutcracker", Formatting.BLUE);
        addMobEntry(display, mobsCaught, "Yeti", Formatting.GOLD);
        addMobEntry(display, mobsCaught, "Reindrake", Formatting.GOLD);
    }

    private static void addTimeSinceSection(List<String> display, Map<String, Double> lastTimeCaught) {
        if (FishMasterConfig.isFishingTrackerMarinaEnabled()) {
            addTimeSinceEntry(display, lastTimeCaught, "Great White Shark", Formatting.RED);
        }
        if (FishMasterConfig.isFishingTrackerSpookyEnabled()) {
            addTimeSinceEntry(display, lastTimeCaught, "Grim Reaper", Formatting.GOLD);
        }
        if (FishMasterConfig.isFishingTrackerWinterEnabled()) {
            addTimeSinceEntry(display, lastTimeCaught, "Yeti", Formatting.GOLD);
            addTimeSinceEntry(display, lastTimeCaught, "Reindrake", Formatting.GOLD);
        }
        if (FishMasterConfig.getFishingTrackerType() == 1) {
            addTimeSinceEntry(display, lastTimeCaught, "Thunder", Formatting.LIGHT_PURPLE);
            addTimeSinceEntry(display, lastTimeCaught, "Lord Jawbus", Formatting.LIGHT_PURPLE);
        }
        if (FishMasterConfig.getFishingTrackerType() == 0) {
            addTimeSinceEntry(display, lastTimeCaught, "Sea Emperor", Formatting.GOLD);
        }
    }

    private static void addMobEntry(List<String> display, Map<String, Integer> mobsCaught, String mobName, Formatting color) {
        int count = mobsCaught.getOrDefault(mobName, 0);
        display.add(color + mobName + ": " + count);
    }

    private static void addTimeSinceEntry(List<String> display, Map<String, Double> lastTimeCaught, String mobName, Formatting color) {
        Double lastTime = lastTimeCaught.get(mobName);
        String timeSince = lastTime == null ? "Never" : getTimeBetween(lastTime, System.currentTimeMillis() / 1000.0);
        display.add(color + mobName + ": " + timeSince);
    }

    private static String getTimeBetween(double start, double end) {
        long difference = (long)(end - start);
        long days = difference / (24 * 3600);
        long hours = (difference % (24 * 3600)) / 3600;
        long minutes = (difference % 3600) / 60;
        long seconds = difference % 60;

        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
