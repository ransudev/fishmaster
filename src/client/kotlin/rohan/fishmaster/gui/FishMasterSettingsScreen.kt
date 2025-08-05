package rohan.fishmaster.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
import java.awt.Color

/**
 * Example settings screen for Fish Master using Elementa GUI library
 */
class FishMasterSettingsScreen : WindowScreen(ElementaVersion.V2) {

    // Track current settings
    private var autoFishingEnabled = false
    private var seaCreatureKillerEnabled = false
    private var recastDelayTicks = 2

    // Main components
    private val contentContainer by UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = RelativeConstraint(0.8f)
        height = RelativeConstraint(0.8f)
    } childOf window effect OutlineEffect(Color(0, 170, 255), 1f)

    // Header
    private val titleContainer by UIContainer().constrain {
        x = CenterConstraint()
        y = PixelConstraint(10f)
        width = RelativeConstraint(0.9f)
        height = PixelConstraint(30f)
    } childOf contentContainer

    private val titleText by UIText("Fish Master Settings").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        textScale = 2.pixels()
        color = Color(0, 170, 255).toConstraint()
    } childOf titleContainer

    // Content area with scroll capability
    private val scrollContainer by ScrollComponent(
        innerPadding = 4f
    ).constrain {
        x = CenterConstraint()
        y = PixelConstraint(50f)
        width = RelativeConstraint(0.9f)
        height = RelativeConstraint(0.8f)
    } childOf contentContainer effect ScissorEffect()

    // Auto fishing section
    private val autoFishingContainer by UIContainer().constrain {
        x = CenterConstraint()
        y = 0.pixels()
        width = RelativeConstraint(1f)
        height = PixelConstraint(40f)
    } childOf scrollContainer effect OutlineEffect(Color(200, 200, 200), 1f)

    private val autoFishingText by UIText("Auto Fishing").constrain {
        x = PixelConstraint(10f)
        y = CenterConstraint()
    } childOf autoFishingContainer

    private val autoFishingSwitch by UIBlock().constrain {
        x = PixelConstraint(10f, true)
        y = CenterConstraint()
        width = PixelConstraint(34f)
        height = PixelConstraint(14f)
        color = Color(80, 80, 80).toConstraint()
    } childOf autoFishingContainer

    private val autoFishingKnob by UIBlock().constrain {
        x = PixelConstraint(0f)
        y = CenterConstraint()
        width = PixelConstraint(20f)
        height = PixelConstraint(20f)
        color = Color(200, 200, 200).toConstraint()
    } childOf autoFishingSwitch

    // Sea Creature Killer section
    private val sckContainer by UIContainer().constrain {
        x = CenterConstraint()
        y = PixelConstraint(50f)
        width = RelativeConstraint(1f)
        height = PixelConstraint(40f)
    } childOf scrollContainer effect OutlineEffect(Color(200, 200, 200), 1f)

    private val sckText by UIText("Sea Creature Killer").constrain {
        x = PixelConstraint(10f)
        y = CenterConstraint()
    } childOf sckContainer

    private val sckSwitch by UIBlock().constrain {
        x = PixelConstraint(10f, true)
        y = CenterConstraint()
        width = PixelConstraint(34f)
        height = PixelConstraint(14f)
        color = Color(80, 80, 80).toConstraint()
    } childOf sckContainer

    private val sckKnob by UIBlock().constrain {
        x = PixelConstraint(0f)
        y = CenterConstraint()
        width = PixelConstraint(20f)
        height = PixelConstraint(20f)
        color = Color(200, 200, 200).toConstraint()
    } childOf sckSwitch

    // Delay slider section
    private val delayContainer by UIContainer().constrain {
        x = CenterConstraint()
        y = PixelConstraint(100f)
        width = RelativeConstraint(1f)
        height = PixelConstraint(60f)
    } childOf scrollContainer effect OutlineEffect(Color(200, 200, 200), 1f)

    private val delayText by UIText("Recast Delay: $recastDelayTicks ticks").constrain {
        x = CenterConstraint()
        y = PixelConstraint(10f)
    } childOf delayContainer

    private val delaySlider by UIBlock().constrain {
        x = CenterConstraint()
        y = PixelConstraint(35f)
        width = RelativeConstraint(0.8f)
        height = PixelConstraint(8f)
        color = Color(80, 80, 80).toConstraint()
    } childOf delayContainer

    private val delayKnob by UIBlock().constrain {
        y = CenterConstraint()
        width = PixelConstraint(16f)
        height = PixelConstraint(16f)
        color = Color(0, 170, 255).toConstraint()
    } childOf delaySlider

    // Button container
    private val buttonContainer by UIContainer().constrain {
        x = CenterConstraint()
        y = PixelConstraint(10f, true)
        width = RelativeConstraint(0.9f)
        height = PixelConstraint(30f)
    } childOf contentContainer

    // Save button
    private val saveButton by UIBlock().constrain {
        x = PixelConstraint(0f)
        y = CenterConstraint()
        width = PixelConstraint(100f)
        height = PixelConstraint(24f)
        color = Color(0, 170, 255).toConstraint()
    } childOf buttonContainer

    private val saveText by UIText("Save").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        color = Color.WHITE.toConstraint()
    } childOf saveButton

    // Cancel button
    private val cancelButton by UIBlock().constrain {
        x = PixelConstraint(110f)
        y = CenterConstraint()
        width = PixelConstraint(100f)
        height = PixelConstraint(24f)
        color = Color(150, 150, 150).toConstraint()
    } childOf buttonContainer

    private val cancelText by UIText("Cancel").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        color = Color.WHITE.toConstraint()
    } childOf cancelButton

    init {
        // Initialize with current settings (you would load these from config)
        updateSwitchState(autoFishingSwitch, autoFishingKnob, autoFishingEnabled)
        updateSwitchState(sckSwitch, sckKnob, seaCreatureKillerEnabled)
        updateSliderPosition()

        // Set up auto fishing switch interaction
        autoFishingSwitch.onMouseClick {
            autoFishingEnabled = !autoFishingEnabled
            updateSwitchState(autoFishingSwitch, autoFishingKnob, autoFishingEnabled)
        }

        // Set up sea creature killer switch interaction
        sckSwitch.onMouseClick {
            seaCreatureKillerEnabled = !seaCreatureKillerEnabled
            updateSwitchState(sckSwitch, sckKnob, seaCreatureKillerEnabled)
        }

        // Set up slider interaction
        delaySlider.onMouseClick { event ->
            val relativeX = (event.absoluteX - delaySlider.getLeft()) / delaySlider.getWidth()
            recastDelayTicks = (1 + (9 * relativeX.coerceIn(0f, 1f))).toInt()
            updateSliderPosition()
            updateDelayText()
        }

        delaySlider.onMouseDrag { mouseX, _, _ ->
            val relativeX = (mouseX - delaySlider.getLeft()) / delaySlider.getWidth()
            recastDelayTicks = (1 + (9 * relativeX.coerceIn(0f, 1f))).toInt()
            updateSliderPosition()
            updateDelayText()
        }

        // Set up save button
        saveButton.onMouseEnter {
            saveButton.setColor(Color(0, 140, 210).toConstraint())
        }

        saveButton.onMouseLeave {
            saveButton.setColor(Color(0, 170, 255).toConstraint())
        }

        saveButton.onMouseClick {
            // Save settings to config
            saveSettings()
            restorePreviousScreen()
        }

        // Set up cancel button
        cancelButton.onMouseEnter {
            cancelButton.setColor(Color(120, 120, 120).toConstraint())
        }

        cancelButton.onMouseLeave {
            cancelButton.setColor(Color(150, 150, 150).toConstraint())
        }

        cancelButton.onMouseClick {
            restorePreviousScreen()
        }
    }

    private fun updateSwitchState(switchBlock: UIBlock, knob: UIBlock, enabled: Boolean) {
        // Update switch background
        switchBlock.setColor(
            if (enabled) Color(0, 170, 255).toConstraint() else Color(80, 80, 80).toConstraint()
        )

        // Update knob position and color
        knob.setX(
            if (enabled) PixelConstraint(switchBlock.getWidth() - knob.getWidth()) else PixelConstraint(0f)
        )

        knob.setColor(
            if (enabled) Color.WHITE.toConstraint() else Color(200, 200, 200).toConstraint()
        )
    }

    private fun updateSliderPosition() {
        val percent = (recastDelayTicks - 1) / 9f // Convert 1-10 to 0-1 range
        val xPos = percent * (delaySlider.getWidth() - delayKnob.getWidth())
        delayKnob.setX(PixelConstraint(xPos))
    }

    private fun updateDelayText() {
        delayText.setText("Recast Delay: $recastDelayTicks ticks")
    }

    private fun saveSettings() {
        // Here you would save the settings to your mod's configuration
        // For example:
        // FishMasterConfig.setAutoFishingEnabled(autoFishingEnabled)
        // FishMasterConfig.setSCKEnabled(seaCreatureKillerEnabled)
        // FishMasterConfig.setRecastDelay(recastDelayTicks)
    }
}

/**
 * Function to show the settings screen
 */
fun showFishMasterSettings() {
    try {
        // Create the screen instance
        val screen = FishMasterSettingsScreen()

        // Use the Minecraft client directly to set the screen
        // This is the most reliable way to open a screen
        net.minecraft.client.MinecraftClient.getInstance().setScreen(screen)

        // Log that we attempted to open the screen
        println("FishMaster: Attempting to open settings screen")
    } catch (e: Exception) {
        // Log any errors that occur
        println("FishMaster: Error opening settings screen: ${e.message}")
        e.printStackTrace()
    }
}
