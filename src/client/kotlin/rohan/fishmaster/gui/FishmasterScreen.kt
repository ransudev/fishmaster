package rohan.fishmaster.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import org.lwjgl.glfw.GLFW
import rohan.fishmaster.feature.SeaCreatureKiller
import java.awt.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// Note: Using real config from rohan.fishmaster.config.FishMasterConfig

// Bridge to access Java config statics without Kotlin compile-time dependency on the client Java task order
private object ConfigBridge {
    private const val CLASS_NAME = "rohan.fishmaster.config.FishMasterConfig"

    private fun clazz(): Class<*>? = try {
        Class.forName(CLASS_NAME)
    } catch (_: Throwable) { null }

    fun getAutoFishingKeybind(): Int = try {
        val c = clazz() ?: return org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN
        val m = c.getMethod("getAutoFishingKeybind")
        (m.invoke(null) as? Int) ?: org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN
    } catch (_: Throwable) { org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN }

    fun setAutoFishingKeybind(key: Int) {
        try {
            val c = clazz() ?: return
            val m = c.getMethod("setAutoFishingKeybind", Int::class.javaPrimitiveType)
            m.invoke(null, key)
        } catch (_: Throwable) { }
    }

    fun isSeaCreatureKillerEnabled(): Boolean = try {
        val c = clazz() ?: return false
        val m = c.getMethod("isSeaCreatureKillerEnabled")
        (m.invoke(null) as? Boolean) ?: false
    } catch (_: Throwable) { false }

    fun getSeaCreatureKillerMode(): String = try {
        val c = clazz() ?: return "RCM"
        val m = c.getMethod("getSeaCreatureKillerMode")
        (m.invoke(null) as? String) ?: "RCM"
    } catch (_: Throwable) { "RCM" }

    fun setSeaCreatureKillerMode(mode: String) {
        try {
            val c = clazz() ?: return
            val m = c.getMethod("setSeaCreatureKillerMode", String::class.java)
            m.invoke(null, mode)
        } catch (_: Throwable) { }
    }
}

class SimpleDropdown(
    private val options: List<String>,
    initialSelection: String,
    private val onSelect: (String) -> Unit
) : UIComponent() {
    private val selectedText: UIText
    private var selectedOption = initialSelection
    private var expanded = false
    private val optionsContainer: UIComponent
    private val mainBox: UIComponent

    init {
        constrain {
            width = 120.pixels()
            height = 24.pixels()
        }

        mainBox = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(3f.pixels()) childOf this
        mainBox.effect(OutlineEffect(Color.BLACK, 1f))

        selectedText = UIText(selectedOption).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.0f.pixels()
            color = Color(185, 187, 190).toConstraint()
        } childOf mainBox

        optionsContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 100.percent() + 2.pixels()
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf this
        optionsContainer.hide(true)

        options.forEach { option ->
            val optionBox = UIContainer().constrain {
                y = SiblingConstraint()
                width = 100.percent()
                height = 24.pixels()
            } childOf optionsContainer

            val optionBackground = UIBlock().constrain {
                width = 100.percent()
                height = 100.percent()
                color = Color(70, 70, 70).toConstraint()
            }.setRadius(3f.pixels()) childOf optionBox
            optionBackground.effect(OutlineEffect(Color.BLACK, 1f))

            val optionText = UIText(option).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                textScale = 1.0f.pixels()
                color = Color(185, 187, 190).toConstraint()
            } childOf optionBox

            optionBox.onMouseEnter {
                optionText.setColor(Color.WHITE)
                optionBackground.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, Color(90, 90, 90).toConstraint())
                }
            }
            optionBox.onMouseLeave {
                optionText.setColor(Color(185, 187, 190))
                optionBackground.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, Color(70, 70, 70).toConstraint())
                }
            }
            fun selectOption() {
                selectedOption = option
                selectedText.setText(option)
                onSelect(option) // Always call onSelect
                optionsContainer.hide(true)
                expanded = false
            }
            optionBox.onMouseClick {
                selectOption()
            }
            optionBackground.onMouseClick { selectOption() }
        }

        mainBox.onMouseClick {
            toggleDropdown()
        }

        mainBox.onMouseEnter {
            mainBox.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(90, 90, 90).toConstraint())
            }
        }

        mainBox.onMouseLeave {
            mainBox.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(70, 70, 70).toConstraint())
            }
        }
    }

    private fun toggleDropdown() {
        expanded = !expanded
        if (expanded) {
            optionsContainer.unhide(true)
        } else {
            optionsContainer.hide(true)
        }
    }
}

// Temporary replacement for dropdown: a single button that cycles through options on click
class SimpleCycleButton(
    private val options: List<String>,
    initialSelection: String,
    private val onChange: (String) -> Unit
) : UIComponent() {
    private var index = options.indexOf(initialSelection).let { if (it == -1) 0 else it }
    private val background: UIComponent
    private val text: UIText

    init {
        constrain {
            width = 120.pixels()
            height = 24.pixels()
        }

        background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(3f.pixels()) childOf this
        background.effect(OutlineEffect(Color.BLACK, 1f))

        text = UIText(options[index]).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.0f.pixels()
            color = Color(185, 187, 190).toConstraint()
        } childOf this

        onMouseClick {
            index = (index + 1) % options.size
            val value = options[index]
            text.setText(value)
            onChange(value)
        }

        onMouseEnter {
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(90, 90, 90).toConstraint())
            }
        }
        onMouseLeave {
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(70, 70, 70).toConstraint())
            }
        }
    }
}

class SimpleSwitch(initialState: Boolean) : UIComponent() {
    private var enabled = initialState
    val text = UIText(if (enabled) "ON" else "OFF")
    private val background: UIComponent

    var onStateChange: (Boolean) -> Unit = {}

    init {
        constrain {
            width = 45.pixels()
            height = 22.5f.pixels()
        }

        background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(3f.pixels()) childOf this
        background.effect(OutlineEffect(Color.BLACK, 1f))

        text.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.9f.pixels()
        } childOf this

        updateColor()

        onMouseClick {
            enabled = !enabled
            text.setText(if (enabled) "ON" else "OFF")
            updateColor()
            onStateChange(enabled)
        }

        onMouseEnter {
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(90, 90, 90).toConstraint())
            }
        }

        onMouseLeave {
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(70, 70, 70).toConstraint())
            }
        }
    }

    private fun updateColor() {
        val textColor = if (enabled) Color(88, 101, 242) else Color(185, 187, 190)
        text.setColor(textColor)
    }
}

class SimpleKeybindButton(private val getKey: () -> Int, private val onKeySet: (Int) -> Unit) : UIComponent() {
    private var waitingForKey = false
    private val keyText: UIText
    private val background: UIComponent

    init {
        constrain {
            width = 90.pixels()
            height = 24.pixels()
        }

        background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(3f.pixels()) childOf this
        background.effect(OutlineEffect(Color.BLACK, 1f))

        keyText = UIText(getKeyName(getKey())).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.0f.pixels()
        } childOf this

        updateColor()

        onMouseClick {
            if (!waitingForKey) {
                waitingForKey = true
                keyText.setText("...")
                updateColor()
            }
        }

        onMouseEnter {
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(90, 90, 90).toConstraint())
            }
        }

        onMouseLeave {
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(70, 70, 70).toConstraint())
            }
        }
    }

    private fun getKeyName(key: Int): String {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "None"
        val name = GLFW.glfwGetKeyName(key, 0)
        if (name != null) return name.uppercase()
        // Fallback for special keys
        return when (key) {
            GLFW.GLFW_KEY_SPACE -> "SPACE"
            GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "SHIFT"
            GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "CTRL"
            GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> "ALT"
            GLFW.GLFW_KEY_TAB -> "TAB"
            GLFW.GLFW_KEY_ENTER -> "ENTER"
            GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE"
            GLFW.GLFW_KEY_DELETE -> "DELETE"
            GLFW.GLFW_KEY_UP -> "UP"
            GLFW.GLFW_KEY_DOWN -> "DOWN"
            GLFW.GLFW_KEY_LEFT -> "LEFT"
            GLFW.GLFW_KEY_RIGHT -> "RIGHT"
            else -> "Unknown"
        }
    }

    private fun updateColor() {
        val textColor = if (waitingForKey) Color(114, 137, 218) else Color(185, 187, 190)
        keyText.setColor(textColor)
    }

    fun handleKeyInput(keyCode: Int) {
        if (waitingForKey) {
            val finalKeyCode = if (keyCode == GLFW.GLFW_KEY_ESCAPE) GLFW.GLFW_KEY_UNKNOWN else keyCode
            waitingForKey = false
            onKeySet(finalKeyCode)
            keyText.setText(getKeyName(getKey())) // Always show the latest key from config
            updateColor()
        }
    }

    fun isWaiting() = waitingForKey
}

class FishmasterScreen : WindowScreen(ElementaVersion.V5) {
    private var previousGuiScale: Int? = null
    private val tabs = listOf("Main", "Misc", "Failsafes", "Extras")
    private var selectedTab = tabs.first()
    private val contentContainer: UIComponent
    private val tabsComponents = mutableMapOf<String, UIComponent>()
    private var keybindButton: SimpleKeybindButton? = null
    private val selectedTabUnderline: UIComponent

    init {
        // Save the previous GUI scale
        previousGuiScale = MinecraftClient.getInstance().options.guiScale.value
        // Force GUI scale to 2x
        MinecraftClient.getInstance().options.guiScale.setValue(2)

        val mainContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 600.pixels()
            height = 400.pixels()
        } childOf window

        val background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(45, 45, 45).toConstraint()
        }.setRadius(10.pixels()) childOf mainContainer
        background.effect(OutlineEffect(Color.BLACK, 2f))

        // Header
        val header = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 50.pixels()
        } childOf mainContainer

        val headerBackground = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(60, 60, 60).toConstraint()
        }.setRadius(10.pixels()) childOf header
        headerBackground.effect(OutlineEffect(Color.BLACK, 1f))

        val title = UIText("FishMaster").constrain {
            x = 20.pixels()
            y = CenterConstraint()
            textScale = 1.5f.pixels()
            color = Color(88, 101, 242).toConstraint()
        } childOf header

        val closeButton = UIContainer().constrain {
            x = RelativeConstraint(1f) - 30.pixels()
            y = CenterConstraint()
            width = 20.pixels()
            height = 20.pixels()
        } childOf header

        val closeButtonText = UIText("X").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = Color.WHITE.toConstraint()
        } childOf closeButton

        closeButton.onMouseClick {
            close()
        }

        closeButton.onMouseEnter {
            closeButtonText.setColor(Color.RED.toConstraint())
        }

        closeButton.onMouseLeave {
            closeButtonText.setColor(Color.WHITE.toConstraint())
        }

        // Tabs
        val tabsContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 50.pixels()
            width = 100.percent()
            height = 35.pixels()
        } childOf mainContainer

        val tabsBackground = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(55, 55, 55).toConstraint()
        } childOf tabsContainer

        // Content area
        contentContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 85.pixels()
            width = 100.percent()
            height = 100.percent() - 85.pixels()
        } childOf mainContainer

        selectedTabUnderline = UIBlock().constrain {
            x = 0.pixels()
            y = 100.percent() - 3.pixels()
            width = 90.pixels()
            height = 3.pixels()
            color = Color(88, 101, 242).toConstraint()
        } childOf tabsContainer

        // Create tabs
        tabs.forEachIndexed { index, tab ->
            val tabButton = UIContainer().constrain {
                x = (index * 90).pixels()
                y = 0.pixels()
                width = 90.pixels()
                height = 100.percent()
            } childOf tabsContainer
            tabsComponents[tab] = tabButton

            val tabText = UIText(tab).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = if (tab == selectedTab) Color.WHITE.toConstraint() else Color(185, 187, 190).toConstraint()
            } childOf tabButton

            tabButton.onMouseClick {
                selectTab(tab, index)
            }

            tabButton.onMouseEnter {
                if (tab != selectedTab) {
                    tabText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.3f, Color.WHITE.toConstraint())
                    }
                }
            }

            tabButton.onMouseLeave {
                if (tab != selectedTab) {
                    tabText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.3f, Color(185, 187, 190).toConstraint())
                    }
                }
            }
        }

        // Initialize tab content
        initializeTabContent()
        showTab(selectedTab)
    }

    private fun selectTab(tab: String, index: Int) {
        selectedTab = tab
        updateTabStyles(index)
        showTab(tab)
    }

    private fun updateTabStyles(selectedIndex: Int) {
        tabs.forEachIndexed { index, tab ->
            val tabButton = tabsComponents[tab] ?: return
            val tabText = (tabButton.children.first() as UIText)
            if (index == selectedIndex) {
                tabText.setColor(Color.WHITE.toConstraint())
            } else {
                tabText.setColor(Color(185, 187, 190).toConstraint())
            }
        }

        val targetX = (selectedIndex * 90)
        selectedTabUnderline.animate {
            setXAnimation(Animations.OUT_EXP, 0.3f, targetX.pixels())
        }
    }

    private fun initializeTabContent() {
        // Initialize content for each tab if needed
    }

    private fun showTab(tab: String) {
        contentContainer.clearChildren()
        // Only reset keybindButton if not on Main tab
        if (tab != "Main") keybindButton = null

        when (tab) {
            "Main" -> {
                val rowWidth = 540f
                val labelWidth = 340f
                val rowHeight = 55f
                val rowSpacing = 10f
                val leftPad = 30f
                val buttonPad = 20f

                fun addFeatureRow(label: String, button: UIComponent, yConstraint: YConstraint, labelScale: Float) {
                    val row = UIContainer().constrain {
                        x = CenterConstraint()
                        y = yConstraint
                        width = rowWidth.pixels()
                        height = rowHeight.pixels()
                    } childOf contentContainer

                    UIBlock().constrain {
                        width = 100.percent()
                        height = 100.percent()
                        color = Color(70, 70, 70).toConstraint()
                    }.setRadius(5.pixels()) childOf row

                    // Improved text positioning: vertically center, left align, and fix scale
                    val labelText = UIText(label).constrain {
                        x = leftPad.pixels()
                        y = CenterConstraint() - (labelScale * 8).pixels() // Nudge up by half text height
                        width = labelWidth.pixels()
                        textScale = labelScale.pixels()
                        color = Color.WHITE.toConstraint()
                    } childOf row

                    button.constrain {
                        x = RelativeConstraint(1f) - FEATURE_BUTTON_WIDTH.pixels() - buttonPad.pixels()
                        y = CenterConstraint()
                    } childOf row
                }

                // Use a persistent keybindButton instance, wired to real config used by GuiKeybindHandler
                if (keybindButton == null) {
                    keybindButton = SimpleKeybindButton({ ConfigBridge.getAutoFishingKeybind() }) { keyCode ->
                        ConfigBridge.setAutoFishingKeybind(keyCode)
                    }
                }

                addFeatureRow(
                    "Auto Fishing Keybind",
                    keybindButton!!,
                    20.pixels(),
                    0.45f // smaller, consistent text
                )

                addFeatureRow(
                    "Sea Creature Killer",
                    SimpleSwitch(ConfigBridge.isSeaCreatureKillerEnabled()).apply {
                        onStateChange = { newState ->
                            SeaCreatureKiller.setEnabled(newState)
                        }
                    },
                    SiblingConstraint(rowSpacing),
                    0.45f // smaller, consistent text
                )

                addFeatureRow(
                    "Attack Mode",
                    run {
                        val options = listOf("RCM", "Melee", "Fire Veil Wand")
                        val initial = ConfigBridge.getSeaCreatureKillerMode().let { if (options.contains(it)) it else "RCM" }
                        SimpleCycleButton(options, initial) { selected ->
                            ConfigBridge.setSeaCreatureKillerMode(selected)
                            // Announce chosen mode
                            MinecraftClient.getInstance().player?.sendMessage(
                                Text.literal("[FishMaster] Sea Creature Killer mode set to: ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal(selected).formatted(Formatting.AQUA)),
                                false
                            )
                            // Only Melee remains WIP; Fire Veil Wand is implemented
                            if (selected == "Melee") {
                                MinecraftClient.getInstance().player?.sendMessage(
                                    Text.literal("[FishMaster] $selected mode is a Work In Progress.").formatted(Formatting.YELLOW),
                                    false
                                )
                            }
                        }
                    },
                    SiblingConstraint(rowSpacing),
                    0.38f // slightly smaller for Attack Mode
                )
            }
            else -> {
                // For other tabs, just show a label
                UIText("Content for $tab").constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                    color = Color.WHITE.toConstraint()
                } childOf contentContainer
            }
        }
    }

    override fun shouldCloseOnEsc() = keybindButton?.isWaiting() != true

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: gg.essential.universal.UKeyboard.Modifiers?) {
        // Always check the persistent keybindButton
        if (keybindButton?.isWaiting() == true) {
            keybindButton?.handleKeyInput(keyCode)
            return
        }
        super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    override fun close() {
        // Restore the previous GUI scale
        previousGuiScale?.let {
            MinecraftClient.getInstance().options.guiScale.setValue(it)
        }
        super.close()
    }
}

const val FEATURE_BUTTON_WIDTH = 120f
