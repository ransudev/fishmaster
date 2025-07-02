package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.data.FishingData;

import java.util.HashMap;
import java.util.Map;

public class FishingTracker {
    public static final Map<String, String> seaCreatureMessages = new HashMap<>();
    private static final Map<String, Integer> mobsCaught = new HashMap<>();
    private static final Map<String, Double> lastTimeCaught = new HashMap<>();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    static {
        initializeSeaCreatures();
    }

    private static void initializeSeaCreatures() {
        // Initialize sea creature messages
        seaCreatureMessages.put("A Squid appeared.", "Squid");
        seaCreatureMessages.put("You caught a Sea Walker.", "Sea Walker");
        seaCreatureMessages.put("You stumbled upon a Sea Guardian.", "Sea Guardian");
        seaCreatureMessages.put("It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!", "Sea Witch");
        seaCreatureMessages.put("You reeled in a Sea Archer.", "Sea Archer");
        seaCreatureMessages.put("The Rider of the Deep has emerged.", "Rider Of The Deep");
        seaCreatureMessages.put("Huh? A Catfish!", "Catfish");
        seaCreatureMessages.put("Is this even a fish? It's the Carrot King!", "Carrot King");
        seaCreatureMessages.put("Gross! A Sea Leech!", "Sea Leech");
        seaCreatureMessages.put("You've discovered a Guardian Defender of the sea.", "Guardian Defender");
        seaCreatureMessages.put("You have awoken the Deep Sea Protector, prepare for a battle!", "Deep Sea Protector");
        seaCreatureMessages.put("The Water Hydra has come to test your strength.", "Water Hydra");
        seaCreatureMessages.put("The Sea Emperor arises from the depths.", "Sea Emperor");
        seaCreatureMessages.put("Phew! It's only a Scarecrow.", "Scarecrow");
        seaCreatureMessages.put("You hear trotting from beneath the waves, you caught a Nightmare.", "Nightmare");
        seaCreatureMessages.put("It must be a full moon, a Werewolf appears.", "Werewolf");
        seaCreatureMessages.put("The spirit of a long lost Phantom Fisher has come to haunt you.", "Phantom Fisher");
        seaCreatureMessages.put("This can't be! The manifestation of death himself!", "Grim Reaper");
        seaCreatureMessages.put("Frozen Steve fell into the pond long ago, never to resurface...until now!", "Frozen Steve");
        seaCreatureMessages.put("It's a snowman! He looks harmless", "Frosty The Snowman");
        seaCreatureMessages.put("The Grinch stole Jerry's Gifts...get them back!", "Grinch");
        seaCreatureMessages.put("What is this creature!?", "Yeti");
        seaCreatureMessages.put("You found a forgotten Nutcracker laying beneath the ice.", "Nutcracker");
        seaCreatureMessages.put("A Reindrake forms from the depths.", "Reindrake");
        seaCreatureMessages.put("A tiny fin emerges from the water, you've caught a Nurse Shark.", "Nurse Shark");
        seaCreatureMessages.put("You spot a fin as blue as the water it came from, it's a Blue Shark.", "Blue Shark");
        seaCreatureMessages.put("A striped beast bounds from the depths, the wild Tiger Shark!", "Tiger Shark");
        seaCreatureMessages.put("Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", "Great White Shark");
        seaCreatureMessages.put("From beneath the lava appears a Magma Slug.", "Magma Slug");
        seaCreatureMessages.put("You hear a faint Moo from the lava... A Moogma appears.", "Moogma");
        seaCreatureMessages.put("A small but fearsome Lava Leech emerges.", "Lava Leech");
        seaCreatureMessages.put("You feel the heat radiating as a Pyroclastic Worm surfaces.", "Pyroclastic Worm");
        seaCreatureMessages.put("A Lava Flame flies out from beneath the lava.", "Lava Flame");
        seaCreatureMessages.put("A Fire Eel slithers out from the depths.", "Fire Eel");
        seaCreatureMessages.put("Taurus and his steed emerge.", "Taurus");
        seaCreatureMessages.put("You hear a massive rumble as Thunder emerges.", "Thunder");
        seaCreatureMessages.put("You have angered a legendary creature... Lord Jawbus has arrived", "Lord Jawbus");
        seaCreatureMessages.put("A Water Worm surfaces!", "Water Worm");
        seaCreatureMessages.put("A Poisoned Water Worm surfaces!", "Poisoned Water Worm");
        seaCreatureMessages.put("A Zombie miner surfaces!", "Zombie Miner");
        seaCreatureMessages.put("A flaming worm surfaces from the depths!", "Flaming Worm");
        seaCreatureMessages.put("A Lava Blaze has surfaced from the depths!", "Lava Blaze");
        seaCreatureMessages.put("A Lava Pigman arose from the depths!", "Lava Pigman");

        // Load saved data from FishingData
        if (FishingData.getMobsCaught() != null) {
            mobsCaught.putAll(FishingData.getMobsCaught());
        }
        if (FishingData.getLastTimeCaught() != null) {
            lastTimeCaught.putAll(FishingData.getLastTimeCaught());
        }
    }

    public static void onChatMessage(String message) {
        for (Map.Entry<String, String> mobMessage : seaCreatureMessages.entrySet()) {
            if (message.startsWith(mobMessage.getKey())) {
                String mobName = mobMessage.getValue();
                updateMobCatch(mobName);
                break;
            }
        }
    }

    private static void updateMobCatch(String mobName) {
        // Update mob count
        int newCount = mobsCaught.getOrDefault(mobName, 0) + 1;
        mobsCaught.put(mobName, newCount);
        FishingData.setMobsCaught(mobName, newCount);

        // Update last caught time for special mobs
        switch (mobName) {
            case "Yeti":
            case "Reindrake":
            case "Thunder":
            case "Lord Jawbus":
            case "Great White Shark":
            case "Grim Reaper":
            case "Sea Emperor":
                double currentTime = System.currentTimeMillis() / 1000.0;
                lastTimeCaught.put(mobName, currentTime);
                FishingData.setLastTimeCaught(mobName, currentTime);
                break;
        }
    }

    public static void tick() {
        if (!FishMasterConfig.isFishingTrackerEnabled()) return;
        if (!FishMasterConfig.isAutoDetectFishingTypeEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.getMainHandStack().isOf(Items.FISHING_ROD)) return;

        FishingBobberEntity fishHook = client.player.fishHook;
        if (fishHook == null) return;

        // Auto-detect fishing type
        if (fishHook.isTouchingWater()) {
            FishMasterConfig.setFishingTrackerType(0); // Water fishing
        } else if (fishHook.isInLava()) {
            FishMasterConfig.setFishingTrackerType(1); // Lava fishing
        }
    }

    public static Map<String, Integer> getMobsCaught() {
        return mobsCaught;
    }

    public static Map<String, Double> getLastTimeCaught() {
        return lastTimeCaught;
    }

    public static void saveData() {
        // Save data to config
        // This would be implemented based on your config system
    }
}
