package rohan.fishmaster.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class TestGui : Screen(Text.literal("Test GUI")) {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Call parent render first
        super.render(context, mouseX, mouseY, delta)

        // Simple background - semi-transparent black
        context.fill(0, 0, width, height, 0x80000000.toInt())

        // Simple white rectangle in center
        val centerX = width / 2
        val centerY = height / 2
        context.fill(centerX - 100, centerY - 50, centerX + 100, centerY + 50, 0xFFFFFFFF.toInt())

        // Simple text
        val title = "Test GUI Works!"
        val titleX = centerX - textRenderer.getWidth(title) / 2
        val titleY = centerY - 4

        context.drawText(textRenderer, title, titleX, titleY, 0x000000, false) // Black text
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) { // ESC key
            close()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false
}
