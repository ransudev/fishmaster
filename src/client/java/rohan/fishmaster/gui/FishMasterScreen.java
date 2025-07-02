package rohan.fishmaster.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rohan.fishmaster.feature.AutoFishingFeature;
import rohan.fishmaster.feature.SeaCreatureKiller;

public class FishMasterScreen extends Screen {
    private final Screen parent;
    private final FishMasterSettings settings;

    // Layout constants
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 25;
    private static final int SECTION_SPACING = 35;

    // Available keybind options
    private static final String[] KEYBIND_OPTIONS = {
        "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M", "NONE"
    };

    public FishMasterScreen(Screen parent) {
        super(Text.literal("FishMaster Settings").formatted(Formatting.AQUA, Formatting.BOLD));
        this.parent = parent;
        this.settings = FishMasterSettings.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 6;
        int currentY = startY;

        // === FEATURE TOGGLES ===
        // Auto Fishing Toggle
        CyclingButtonWidget<Boolean> autoFishingButton = CyclingButtonWidget.onOffBuilder(
            Text.literal("Auto Fishing"), Text.literal("Auto Fishing"))
            .initially(settings.autoFishingEnabled)
            .build(centerX - BUTTON_WIDTH/2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal("Auto Fishing"), (button, enabled) -> {
                    settings.autoFishingEnabled = enabled;
                    if (enabled != AutoFishingFeature.isEnabled()) {
                        AutoFishingFeature.toggle();
                    }
                    settings.saveSettings();
                });
        this.addDrawableChild(autoFishingButton);
        currentY += SPACING;

        // Sea Creature Killer Toggle
        CyclingButtonWidget<Boolean> seaCreatureButton = CyclingButtonWidget.onOffBuilder(
            Text.literal("Sea Creature Killer"), Text.literal("Sea Creature Killer"))
            .initially(settings.seaCreatureKillerEnabled)
            .build(centerX - BUTTON_WIDTH/2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal("Sea Creature Killer"), (button, enabled) -> {
                    settings.seaCreatureKillerEnabled = enabled;
                    if (enabled != SeaCreatureKiller.isEnabled()) {
                        SeaCreatureKiller.toggle();
                    }
                    settings.saveSettings();
                });
        this.addDrawableChild(seaCreatureButton);
        currentY += SECTION_SPACING;

        // === KEYBINDS ===
        // Auto Fishing Keybind
        CyclingButtonWidget<String> autoFishKeybind = CyclingButtonWidget.<String>builder(Text::literal)
            .values(KEYBIND_OPTIONS)
            .initially(KEYBIND_OPTIONS[settings.autoFishingKeybind])
            .build(centerX - BUTTON_WIDTH/2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal("Auto Fishing Key"), (button, key) -> {
                    settings.autoFishingKeybind = getKeybindIndex(key);
                    settings.saveSettings();
                });
        this.addDrawableChild(autoFishKeybind);
        currentY += SPACING;

        // Sea Creature Killer Keybind
        CyclingButtonWidget<String> seaCreatureKeybind = CyclingButtonWidget.<String>builder(Text::literal)
            .values(KEYBIND_OPTIONS)
            .initially(KEYBIND_OPTIONS[settings.seaCreatureKillerKeybind])
            .build(centerX - BUTTON_WIDTH/2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal("Sea Creature Killer Key"), (button, key) -> {
                    settings.seaCreatureKillerKeybind = getKeybindIndex(key);
                    settings.saveSettings();
                });
        this.addDrawableChild(seaCreatureKeybind);
        currentY += SPACING;

        // Emergency Stop Keybind
        CyclingButtonWidget<String> emergencyKeybind = CyclingButtonWidget.<String>builder(Text::literal)
            .values(KEYBIND_OPTIONS)
            .initially(KEYBIND_OPTIONS[settings.emergencyStopKeybind])
            .build(centerX - BUTTON_WIDTH/2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal("Emergency Stop Key"), (button, key) -> {
                    settings.emergencyStopKeybind = getKeybindIndex(key);
                    settings.saveSettings();
                });
        this.addDrawableChild(emergencyKeybind);
        currentY += SECTION_SPACING;

        // === BOTTOM BUTTONS ===
        // Emergency Stop Button
        ButtonWidget emergencyButton = ButtonWidget.builder(
            Text.literal("EMERGENCY STOP").formatted(Formatting.RED, Formatting.BOLD),
            button -> {
                settings.emergencyStop();
                this.clearAndInit();
            })
            .dimensions(centerX - BUTTON_WIDTH/2, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
        this.addDrawableChild(emergencyButton);
        currentY += SPACING + 10;

        // Done Button
        ButtonWidget doneButton = ButtonWidget.builder(
            Text.literal("Done"),
            button -> this.close())
            .dimensions(centerX - 75, currentY, 150, BUTTON_HEIGHT)
            .build();
        this.addDrawableChild(doneButton);
    }

    private int getKeybindIndex(String key) {
        for (int i = 0; i < KEYBIND_OPTIONS.length; i++) {
            if (KEYBIND_OPTIONS[i].equals(key)) {
                return i;
            }
        }
        return KEYBIND_OPTIONS.length - 1; // Default to "NONE"
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackground(context, mouseX, mouseY, delta);

        // Render title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
            this.width / 2, 20, 0xFFFFFF);

        // Section headers
        int centerX = this.width / 2;
        int startY = this.height / 6;

        // Features header
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("Features").formatted(Formatting.YELLOW, Formatting.BOLD),
            centerX, startY - 20, 0xFFFFFF);

        // Keybinds header
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("Keybinds").formatted(Formatting.AQUA, Formatting.BOLD),
            centerX, startY + SECTION_SPACING * 2 - 20, 0xFFFFFF);

        // Status display
        renderStatus(context);

        // Render widgets
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderStatus(DrawContext context) {
        int bottomY = this.height - 50;
        int centerX = this.width / 2;

        // Current status
        String autoStatus = AutoFishingFeature.isEnabled() ? "ON" : "OFF";
        String seaStatus = SeaCreatureKiller.isEnabled() ? "ON" : "OFF";

        Formatting autoColor = AutoFishingFeature.isEnabled() ? Formatting.GREEN : Formatting.RED;
        Formatting seaColor = SeaCreatureKiller.isEnabled() ? Formatting.GREEN : Formatting.RED;

        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("Auto Fishing: ").formatted(Formatting.GRAY)
                .append(Text.literal(autoStatus).formatted(autoColor))
                .append(Text.literal(" | Sea Creature Killer: ").formatted(Formatting.GRAY))
                .append(Text.literal(seaStatus).formatted(seaColor)),
            centerX, bottomY, 0xFFFFFF);

        // Fishing tracker status (always active)
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("Fishing Tracker: ").formatted(Formatting.GRAY)
                .append(Text.literal("ALWAYS ACTIVE").formatted(Formatting.GREEN)),
            centerX, bottomY + 12, 0xFFFFFF);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
