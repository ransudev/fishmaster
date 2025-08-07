package rohan.fishmaster.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

/**
 * Settings screen for Fish Master using Elementa GUI library
 * This is an empty template for you to customize
 */
class FishMasterSettingsScreen : WindowScreen(ElementaVersion.V2) {
    // Empty screen - implement your own GUI components here

    init {
        // Initialize your GUI elements here
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
