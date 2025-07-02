package rohan.fishmaster.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FishingData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fishmaster").resolve("fishydata.json");

    private static Map<String, Integer> mobsCaught = new HashMap<>();
    private static Map<String, Double> lastTimeCaught = new HashMap<>();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                save(); // Create default empty file
                return;
            }

            String json = Files.readString(CONFIG_PATH);
            JsonObject data = GSON.fromJson(json, JsonObject.class);

            Type mapType = new TypeToken<Map<String, SeaCreatureData>>(){}.getType();
            Map<String, SeaCreatureData> loadedData = GSON.fromJson(json, mapType);

            if (loadedData != null) {
                loadedData.forEach((name, seaCreatureData) -> {
                    mobsCaught.put(name, seaCreatureData.catches);
                    lastTimeCaught.put(name, seaCreatureData.timeSince);
                });
            }
        } catch (IOException e) {
            System.err.println("Failed to load fishing data: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            Map<String, SeaCreatureData> saveData = new HashMap<>();
            mobsCaught.forEach((name, catches) -> {
                saveData.put(name, new SeaCreatureData(
                    catches,
                    lastTimeCaught.getOrDefault(name, 0.0)
                ));
            });

            String json = GSON.toJson(saveData);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save fishing data: " + e.getMessage());
        }
    }

    public static Map<String, Integer> getMobsCaught() {
        return mobsCaught;
    }

    public static Map<String, Double> getLastTimeCaught() {
        return lastTimeCaught;
    }

    public static void setMobsCaught(String mob, int count) {
        mobsCaught.put(mob, count);
        save();
    }

    public static void setLastTimeCaught(String mob, double time) {
        lastTimeCaught.put(mob, time);
        save();
    }

    private static class SeaCreatureData {
        final int catches;
        final double timeSince;

        SeaCreatureData(int catches, double timeSince) {
            this.catches = catches;
            this.timeSince = timeSince;
        }
    }
}
