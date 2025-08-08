package rohan.fishmaster.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import rohan.fishmaster.config.FishMasterConfig
import rohan.fishmaster.feature.AutoFishingFeature
import rohan.fishmaster.feature.SeaCreatureKiller
import kotlin.math.*

class FishMasterGui : Screen(Text.literal("FishMaster GUI")) {

    companion object {
        // Panel dimensions
        private const val PANEL_WIDTH = 280
        private const val PANEL_HEIGHT = 400
        private const val COMPONENT_HEIGHT = 25
        private const val MARGIN = 8
        private const val TITLE_HEIGHT = 30

        // Colors (ARGB format for Minecraft)
        private fun rgba(r: Int, g: Int, b: Int, a: Int = 255): Int {
            return (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        private val BG_COLOR = rgba(20, 20, 20, 150)
        private val PANEL_COLOR = rgba(35, 35, 35, 240)
        private val BUTTON_COLOR = rgba(55, 55, 55, 255)
        private val BUTTON_HOVER = rgba(75, 75, 75, 255)
        private val ENABLED_COLOR = rgba(85, 170, 85, 255)
        private val DISABLED_COLOR = rgba(170, 85, 85, 255)
        private val ACCENT_COLOR = rgba(100, 150, 255, 255)
        private val TEXT_COLOR = rgba(255, 255, 255, 255)
        private val BORDER_COLOR = rgba(100, 100, 100, 255)
    }

    private var panelX = 0
    private var panelY = 0
    private var isDragging = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    // Component tracking
    private val components = mutableListOf<GuiComponent>()
    private var waitingForKeybind: KeybindButton? = null

    // Animation
    private var animationTime = 0f

    init {
        setupComponents()
    }

    override fun init() {
        super.init()
        // Center the panel
        panelX = (width - PANEL_WIDTH) / 2
        panelY = (height - PANEL_HEIGHT) / 2
        updateComponentPositions()
    }

    private fun setupComponents() {
        components.clear()

        // Category: Combat
        components.add(CategoryLabel(0, 0, "Combat", ACCENT_COLOR))

        // Sea Creature Killer Toggle
        components.add(ToggleButton(
            0, 0, "Sea Creature Killer",
            getValue = { SeaCreatureKiller.isEnabled() },
            setValue = { enabled ->
                // Note: Add proper enable/disable methods to SeaCreatureKiller class
                println("Sea Creature Killer: $enabled")
            }
        ))

        // Category: Fishing
        components.add(CategoryLabel(0, 0, "Fishing", ACCENT_COLOR))

        // Auto Fishing Toggle
        components.add(ToggleButton(
            0, 0, "Auto Fishing",
            getValue = { AutoFishingFeature.isEnabled() },
            setValue = { enabled ->
                // Note: Add proper enable/disable methods to AutoFishingFeature class
                println("Auto Fishing: $enabled")
            }
        ))

        // Auto Fishing Keybind
        components.add(KeybindButton(
            0, 0, "Auto Fish Keybind",
            getValue = { -1 }, // Default to no keybind for now
            setValue = { keyCode ->
                // Store keybind logic here - for now just print
                println("Auto Fish keybind set to: $keyCode")
            }
        ))

        // Category: Safety
        components.add(CategoryLabel(0, 0, "Failsafes", rgba(255, 200, 100, 255)))

        // Anti-AFK Failsafe
        components.add(ToggleButton(
            0, 0, "Anti-AFK Failsafe",
            getValue = { false }, // Default to false for now
            setValue = { enabled ->
                println("Anti-AFK Failsafe: $enabled")
            }
        ))

        // Water Check Failsafe
        components.add(ToggleButton(
            0, 0, "Water Check",
            getValue = { true }, // Default to true for now
            setValue = { enabled ->
                println("Water Check: $enabled")
            }
        ))

        // Inventory Full Failsafe
        components.add(ToggleButton(
            0, 0, "Inventory Full Stop",
            getValue = { true }, // Default to true for now
            setValue = { enabled ->
                println("Inventory Full Stop: $enabled")
            }
        ))

        // Category: Other
        components.add(CategoryLabel(0, 0, "Other", rgba(150, 150, 150, 255)))

        // Webhook Toggle
        components.add(ToggleButton(
            0, 0, "Discord Webhook",
            getValue = { FishMasterConfig.isWebhookEnabled() },
            setValue = { enabled -> FishMasterConfig.setWebhookEnabled(enabled) }
        ))
    }

    private fun updateComponentPositions() {
        var yOffset = TITLE_HEIGHT + MARGIN
        components.forEach { component ->
            component.x = panelX + MARGIN
            component.y = panelY + yOffset
            component.width = PANEL_WIDTH - (MARGIN * 2)
            yOffset += COMPONENT_HEIGHT + if (component is CategoryLabel) 0 else 5
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Call parent render first to ensure proper setup
        super.render(context, mouseX, mouseY, delta)

        // Use simple solid colors instead of ARGB function
        // Dark background overlay
        context.fill(0, 0, width, height, 0x80000000.toInt()) // Semi-transparent black

        // Center the panel on screen
        panelX = (width - PANEL_WIDTH) / 2
        panelY = (height - PANEL_HEIGHT) / 2

        // Draw a simple test rectangle - dark gray
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF404040.toInt())

        // Simple white border
        // Top border
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 2, 0xFFFFFFFF.toInt())
        // Bottom border
        context.fill(panelX, panelY + PANEL_HEIGHT - 2, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFFFFF.toInt())
        // Left border
        context.fill(panelX, panelY, panelX + 2, panelY + PANEL_HEIGHT, 0xFFFFFFFF.toInt())
        // Right border
        context.fill(panelX + PANEL_WIDTH - 2, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFFFFF.toInt())

        // Simple title text
        val title = "FishMaster GUI Test"
        val titleX = panelX + (PANEL_WIDTH - textRenderer.getWidth(title)) / 2
        val titleY = panelY + 20

        context.drawText(textRenderer, title, titleX, titleY, 0xFFFFFF, false)

        // Simple instruction text
        val instruction = "Press ESC to close"
        val instructionX = panelX + (PANEL_WIDTH - textRenderer.getWidth(instruction)) / 2
        val instructionY = panelY + 40

        context.drawText(textRenderer, instruction, instructionX, instructionY, 0xAAAAAA, false)
    }

    private fun renderPanelShadow(context: DrawContext) {
        val shadowOffset = 4
        context.fill(
            panelX + shadowOffset, panelY + shadowOffset,
            panelX + PANEL_WIDTH + shadowOffset, panelY + PANEL_HEIGHT + shadowOffset,
            rgba(0, 0, 0, 100)
        )
    }

    private fun renderPanel(context: DrawContext) {
        // Main panel background
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, PANEL_COLOR)

        // Panel border with animation
        val animatedBorder = rgba(
            100 + (sin(animationTime) * 20).toInt(),
            100 + (cos(animationTime * 1.5f) * 20).toInt(),
            150 + (sin(animationTime * 2f) * 30).toInt(),
            255
        )
        drawBorder(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, animatedBorder)
    }

    private fun renderTitleBar(context: DrawContext) {
        // Title bar background
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + TITLE_HEIGHT, ACCENT_COLOR)

        // Title text
        val title = "FishMaster v1.0"
        val titleX = panelX + (PANEL_WIDTH - textRenderer.getWidth(title)) / 2
        val titleY = panelY + (TITLE_HEIGHT - 8) / 2

        context.drawText(textRenderer, title, titleX, titleY, TEXT_COLOR, true)

        // Close button
        val closeX = panelX + PANEL_WIDTH - 20
        val closeY = panelY + 5
        context.fill(closeX, closeY, closeX + 15, closeY + 15, rgba(170, 85, 85, 255))
        context.drawText(textRenderer, "×", closeX + 4, closeY + 3, TEXT_COLOR, false)
    }

    private fun renderKeybindOverlay(context: DrawContext) {
        // Semi-transparent overlay
        context.fill(0, 0, width, height, rgba(0, 0, 0, 128))

        // Center message
        val message = "Press a key to bind (ESC to cancel)"
        val messageWidth = textRenderer.getWidth(message)
        val messageX = (width - messageWidth) / 2
        val messageY = height / 2

        // Message background
        context.fill(
            messageX - 10, messageY - 10,
            messageX + messageWidth + 10, messageY + 20,
            PANEL_COLOR
        )

        context.drawText(textRenderer, message, messageX, messageY, TEXT_COLOR, true)
    }

    private fun drawBorder(context: DrawContext, x: Int, y: Int, width: Int, height: Int, color: Int) {
        context.fill(x, y, x + width, y + 1, color) // Top
        context.fill(x, y + height - 1, x + width, y + height, color) // Bottom
        context.fill(x, y, x + 1, y + height, color) // Left
        context.fill(x + width - 1, y, x + width, y + height, color) // Right
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // Check close button
            val closeX = panelX + PANEL_WIDTH - 20
            val closeY = panelY + 5
            if (mx >= closeX && mx <= closeX + 15 && my >= closeY && my <= closeY + 15) {
                close()
                return true
            }

            // Check title bar for dragging
            if (mx >= panelX && mx <= panelX + PANEL_WIDTH && my >= panelY && my <= panelY + TITLE_HEIGHT) {
                isDragging = true
                dragOffsetX = mx - panelX
                dragOffsetY = my - panelY
                return true
            }

            // Check component clicks
            components.forEach { component ->
                if (component.isMouseOver(mx, my)) {
                    component.onClick(mx, my, button)
                    return true
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isDragging = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (isDragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            panelX = (mouseX - dragOffsetX).toInt().coerceIn(0, width - PANEL_WIDTH)
            panelY = (mouseY - dragOffsetY).toInt().coerceIn(0, height - PANEL_HEIGHT)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (waitingForKeybind != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKeybind = null
            } else {
                waitingForKeybind!!.setKeybind(keyCode)
                waitingForKeybind = null
            }
            return true
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close()
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false

    // Abstract GUI Component
    abstract class GuiComponent(
        var x: Int,
        var y: Int,
        var width: Int,
        var height: Int
    ) {
        abstract fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float)
        open fun onClick(mouseX: Int, mouseY: Int, button: Int) {}

        fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
        }
    }

    // Category Label Component
    class CategoryLabel(
        x: Int, y: Int,
        private val text: String,
        private val color: Int
    ) : GuiComponent(x, y, 0, COMPONENT_HEIGHT) {

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            val client = MinecraftClient.getInstance()
            context.drawText(client.textRenderer, text, x, y + 8, color, true)

            // Underline
            val textWidth = client.textRenderer.getWidth(text)
            context.fill(x, y + 18, x + textWidth, y + 19, color)
        }
    }

    // Toggle Button Component
    class ToggleButton(
        x: Int, y: Int,
        private val text: String,
        private val getValue: () -> Boolean,
        private val setValue: (Boolean) -> Unit
    ) : GuiComponent(x, y, 0, COMPONENT_HEIGHT) {

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            val isEnabled = getValue()
            val isHovered = isMouseOver(mouseX, mouseY)

            // Button background
            val bgColor = when {
                isEnabled -> ENABLED_COLOR
                isHovered -> BUTTON_HOVER
                else -> BUTTON_COLOR
            }

            context.fill(x, y, x + width, y + height, bgColor)

            // Button border
            val borderColor = if (isHovered) rgba(150, 150, 150, 255) else BORDER_COLOR
            drawBorder(context, x, y, width, height, borderColor)

            // Button text
            val client = MinecraftClient.getInstance()
            val textY = y + (height - 8) / 2
            context.drawText(client.textRenderer, text, x + 8, textY, TEXT_COLOR, false)

            // Status indicator
            val statusText = if (isEnabled) "ON" else "OFF"
            val statusColor = if (isEnabled) rgba(100, 255, 100, 255) else rgba(255, 100, 100, 255)
            val statusX = x + width - client.textRenderer.getWidth(statusText) - 8

            context.drawText(client.textRenderer, statusText, statusX, textY, statusColor, true)
        }

        override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setValue(!getValue())
            }
        }

        private fun drawBorder(context: DrawContext, x: Int, y: Int, width: Int, height: Int, color: Int) {
            context.fill(x, y, x + width, y + 1, color) // Top
            context.fill(x, y + height - 1, x + width, y + height, color) // Bottom
            context.fill(x, y, x + 1, y + height, color) // Left
            context.fill(x + width - 1, y, x + width, y + height, color) // Right
        }
    }

    // Keybind Button Component
    inner class KeybindButton(
        x: Int, y: Int,
        private val text: String,
        private val getValue: () -> Int,
        private val setValue: (Int) -> Unit
    ) : GuiComponent(x, y, 0, COMPONENT_HEIGHT) {

        // Public method to set the keybind value
        fun setKeybind(keyCode: Int) {
            setValue(keyCode)
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            val isHovered = isMouseOver(mouseX, mouseY)
            val isWaiting = waitingForKeybind == this

            // Button background
            val bgColor = when {
                isWaiting -> rgba(255, 255, 100, 255)
                isHovered -> BUTTON_HOVER
                else -> BUTTON_COLOR
            }

            context.fill(x, y, x + width, y + height, bgColor)

            // Button border
            val borderColor = when {
                isWaiting -> rgba(255, 255, 0, 255)
                isHovered -> rgba(150, 150, 150, 255)
                else -> BORDER_COLOR
            }
            drawBorder(context, x, y, width, height, borderColor)

            // Button text
            val client = MinecraftClient.getInstance()
            val textY = y + (height - 8) / 2
            context.drawText(client.textRenderer, text, x + 8, textY, TEXT_COLOR, false)

            // Keybind display
            val keyName = when {
                isWaiting -> "..."
                getValue() == -1 -> "NONE"
                else -> GLFW.glfwGetKeyName(getValue(), 0)?.uppercase() ?: "KEY${getValue()}"
            }

            val keyColor = if (isWaiting) rgba(0, 0, 0, 255) else rgba(200, 200, 255, 255)
            val keyX = x + width - client.textRenderer.getWidth(keyName) - 8

            context.drawText(client.textRenderer, keyName, keyX, textY, keyColor, true)
        }

        override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                waitingForKeybind = this
            }
        }

        private fun drawBorder(context: DrawContext, x: Int, y: Int, width: Int, height: Int, color: Int) {
            context.fill(x, y, x + width, y + 1, color) // Top
            context.fill(x, y + height - 1, x + width, y + height, color) // Bottom
            context.fill(x, y, x + 1, y + height, color) // Left
            context.fill(x + width - 1, y, x + width, y + height, color) // Right
        }
    }
}
