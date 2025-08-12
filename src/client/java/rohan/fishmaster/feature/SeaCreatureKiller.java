package rohan.fishmaster.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rohan.fishmaster.config.FishMasterConfig;
import rohan.fishmaster.feature.seacreaturekiller.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SeaCreatureKiller {
    private static SeaCreatureKiller instance;
    private static boolean enabled = false;
    private static boolean autoFishEnabled = false;
    private static Entity targetEntity = null;
    private static final double DETECTION_RANGE = 6.0;
    private static int killCount = 0;
    
    // Mode system
    private static SeaCreatureKillerMode currentMode;
    private static RCMMode rcmMode = new RCMMode();
    private static MeleeMode meleeMode = new MeleeMode();
    private static FireVeilWandMode fireVeilWandMode = new FireVeilWandMode();

    // Set of specific sea creature names to target
    private static final Set<String> TARGET_CREATURES = new HashSet<>();

    static {
        // Initialize target creatures list
        TARGET_CREATURES.add("Squid");
        TARGET_CREATURES.add("Sea Walker");
        TARGET_CREATURES.add("Night Squid");
        TARGET_CREATURES.add("Sea Guardian");
        TARGET_CREATURES.add("Sea Witch");
        TARGET_CREATURES.add("Sea Archer");
        TARGET_CREATURES.add("Rider of the Deep");
        TARGET_CREATURES.add("Catfish");
        TARGET_CREATURES.add("Carrot King");
        TARGET_CREATURES.add("Agarimoo");
        TARGET_CREATURES.add("Sea Leech");
        TARGET_CREATURES.add("Guardian Defender");
        TARGET_CREATURES.add("Deep Sea Protector");
        TARGET_CREATURES.add("Water Hydra");
        TARGET_CREATURES.add("Sea Emperor");
        TARGET_CREATURES.add("Oasis Rabbit");
        TARGET_CREATURES.add("Oasis Sheep");
        TARGET_CREATURES.add("Water Worm");
        TARGET_CREATURES.add("Poisoned Water Worm");
        TARGET_CREATURES.add("Abyssal Miner");
        TARGET_CREATURES.add("Scarecrow");
        TARGET_CREATURES.add("Nightmare");
        TARGET_CREATURES.add("Werewolf");
        TARGET_CREATURES.add("Phantom Fisher");
        TARGET_CREATURES.add("Grim Reaper");
        TARGET_CREATURES.add("Frozen Steve");
        TARGET_CREATURES.add("Frosty");
        TARGET_CREATURES.add("Grinch");
        TARGET_CREATURES.add("Yeti");
        TARGET_CREATURES.add("Nutcracker");
        TARGET_CREATURES.add("Reindrake");
        TARGET_CREATURES.add("Nurse Shark");
        TARGET_CREATURES.add("Blue Shark");
        TARGET_CREATURES.add("Tiger Shark");
        TARGET_CREATURES.add("Great White Shark");
        TARGET_CREATURES.add("Trash Gobbler");
        TARGET_CREATURES.add("Dumpster Diver");
        TARGET_CREATURES.add("Bayou Sludge");
        TARGET_CREATURES.add("Titanoboa");
        TARGET_CREATURES.add("Flaming Worm");
        TARGET_CREATURES.add("Lava Blaze");
        TARGET_CREATURES.add("Lava Pigman");
        TARGET_CREATURES.add("Magma Slug");
        TARGET_CREATURES.add("Moogma");
        TARGET_CREATURES.add("Lava Leech");
        TARGET_CREATURES.add("Pyroclastic Worm");
        TARGET_CREATURES.add("Lava Flame");
        TARGET_CREATURES.add("Fire Eel");
        TARGET_CREATURES.add("Taurus");
        TARGET_CREATURES.add("Plhlegblast");
        TARGET_CREATURES.add("Thunder");
        TARGET_CREATURES.add("Lord Jawbus");
        TARGET_CREATURES.add("The Loch Emperor");
        TARGET_CREATURES.add("Alligator");
        TARGET_CREATURES.add("Banshee");
        TARGET_CREATURES.add("Blue Ringed Octopus");
        TARGET_CREATURES.add("Snapping Turtle");
        TARGET_CREATURES.add("Wiki Tiki");
        TARGET_CREATURES.add("Frog man");
        TARGET_CREATURES.add("Bayou Sludgling");
        TARGET_CREATURES.add("Frog Man");
        TARGET_CREATURES.add("Bogged");
        TARGET_CREATURES.add("Tadgang");
        TARGET_CREATURES.add("Wetwing");
        TARGET_CREATURES.add("Ent");
        TARGET_CREATURES.add("Tidetot");
        TARGET_CREATURES.add("Stridersurfer");
        
        // Set RCM as default mode
        updateMode();
    }

    public static boolean isEnabled() {
        return enabled && autoFishEnabled;
    }

    public static void toggle() {
        enabled = !enabled;
        FishMasterConfig.setSeaCreatureKillerEnabled(enabled);

        String status = enabled ? "ENABLED" : "DISABLED";
        MinecraftClient.getInstance().player.sendMessage(Text.literal("[FishMaster] Sea Creature Killer: " + status).formatted(enabled ? Formatting.GREEN : Formatting.RED), false);
        
        if (!enabled && currentMode != null) {
            currentMode.exitCombat();
        }
    }

    public static void setEnabled(boolean newState) {
        if (enabled != newState) {
            enabled = newState;
            FishMasterConfig.setSeaCreatureKillerEnabled(enabled);

            if (MinecraftClient.getInstance().player != null) {
                String status = enabled ? "ENABLED" : "DISABLED";
                MinecraftClient.getInstance().player.sendMessage(Text.literal("[FishMaster] Sea Creature Killer: " + status).formatted(enabled ? Formatting.GREEN : Formatting.RED), false);
            }
            
            if (!enabled && currentMode != null) {
                currentMode.exitCombat();
            }
        }
    }

    public static void setAutoFishEnabled(boolean active) {
        autoFishEnabled = active;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (active) {
                if (enabled) {
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.GREEN)
                        .append(Text.literal("ACTIVE").formatted(Formatting.BOLD, Formatting.GREEN))
                        .append(Text.literal(" [" + (currentMode != null ? currentMode.getModeName() : "Unknown") + "]").formatted(Formatting.GRAY)), false);
                    killCount = 0;
                }
            } else {
                if (enabled) {
                    if (currentMode != null) {
                        currentMode.exitCombat();
                    }
                    targetEntity = null;
                    
                    client.player.sendMessage(Text.literal("SCK: ").formatted(Formatting.YELLOW)
                        .append(Text.literal("STANDBY").formatted(Formatting.BOLD, Formatting.YELLOW)), false);
                }
            }
        }
    }

    /**
     * Updates the current mode based on config setting
     */
    private static void updateMode() {
        String mode = FishMasterConfig.getSeaCreatureKillerMode();
        switch (mode) {
            case "Melee":
                currentMode = meleeMode;
                break;
            case "Fire Veil Wand":
                currentMode = fireVeilWandMode;
                break;
            case "RCM":
            default:
                currentMode = rcmMode;
                break;
        }
    }

    public static void tick() {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Update mode if it changed in config
        updateMode();

        // Check if current target is still valid
        if (targetEntity != null && (targetEntity.isRemoved() || !isTargetSeaCreature(targetEntity) ||
            client.player.distanceTo(targetEntity) > DETECTION_RANGE)) {
            
            if (currentMode != null) {
                currentMode.exitCombat();
            }
            targetEntity = null;
        }

        // Find new target if we don't have one
        if (targetEntity == null) {
            findNearestTargetCreature();
        }

        // Enter combat mode if we have a target
        if (targetEntity != null && currentMode != null) {
            if (!currentMode.inCombatMode) {
                currentMode.enterCombat(targetEntity);
            }
            currentMode.performCombat();
        }
    }

    private static void findNearestTargetCreature() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Vec3d playerPos = client.player.getPos();
        Box searchBox = new Box(
            playerPos.x - DETECTION_RANGE, playerPos.y - DETECTION_RANGE, playerPos.z - DETECTION_RANGE,
            playerPos.x + DETECTION_RANGE, playerPos.y + DETECTION_RANGE, playerPos.z + DETECTION_RANGE
        );

        List<Entity> entities = client.world.getOtherEntities(client.player, searchBox, SeaCreatureKiller::isTargetSeaCreature);

        Entity nearestCreature = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            double distance = client.player.distanceTo(entity);
            if (distance < nearestDistance && distance <= DETECTION_RANGE) {
                nearestDistance = distance;
                nearestCreature = entity;
            }
        }

        targetEntity = nearestCreature;
    }

    private static boolean isTargetSeaCreature(Entity entity) {
        if (entity == null) return false;

        String entityName = getEntityDisplayName(entity);

        for (String targetName : TARGET_CREATURES) {
            if (entityName.contains(targetName)) {
                return true;
            }
        }

        return entity instanceof SquidEntity ||
               entity instanceof GlowSquidEntity ||
               entity instanceof GuardianEntity ||
               entity instanceof ElderGuardianEntity;
    }

    private static String getEntityDisplayName(Entity entity) {
        if (entity == null) return "";

        if (entity.hasCustomName() && entity.getCustomName() != null) {
            return entity.getCustomName().getString();
        }

        String typeName = entity.getType().getTranslationKey();

        if (typeName.startsWith("entity.minecraft.")) {
            typeName = typeName.substring("entity.minecraft.".length());
        }

        typeName = typeName.replace("_", " ");
        String[] words = typeName.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    public static int getKillCount() {
        return killCount;
    }

    public static Entity getCurrentTarget() {
        return targetEntity;
    }

    public static String getCurrentMode() {
        return currentMode != null ? currentMode.getModeName() : "Unknown";
    }

    public static void reset() {
        enabled = false;
        targetEntity = null;
        killCount = 0;
        
        if (currentMode != null) {
            currentMode.exitCombat();
        }
    }

    public static SeaCreatureKiller getInstance() {
        if (instance == null) {
            instance = new SeaCreatureKiller();
        }
        return instance;
    }

    public static void init() {
        enabled = FishMasterConfig.isSeaCreatureKillerEnabled();
        updateMode();
    }
}
