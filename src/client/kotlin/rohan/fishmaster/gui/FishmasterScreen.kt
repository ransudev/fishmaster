package rohan.fishmaster.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
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

    fun getRecastDelay(): Float = try {
        val c = clazz() ?: return 20f // Default 20 ticks (1 second)
        val m = c.getMethod("getRecastDelay")
        (m.invoke(null) as? Float) ?: 20f
    } catch (_: Throwable) { 20f }

    fun setRecastDelay(delay: Float) {
        try {
            val c = clazz() ?: return
            val m = c.getMethod("setRecastDelay", Float::class.javaPrimitiveType)
            m.invoke(null, delay)
        } catch (_: Throwable) { }
    }
}

class AnimatedDropdown(
    private val options: List<String>,
    initialSelection: String,
    private val onSelect: (String) -> Unit
) : UIComponent() {
    private val selectedText: UIText
    private var selectedOption = initialSelection
    private var expanded = false
    private val optionsContainer: UIComponent
    private val mainBox: UIComponent
    private val dropdownIcon: UIText
    private var isAnimating = false

    init {
        constrain {
            width = 140.pixels()
            height = 28.pixels()
        }

        mainBox = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(45, 45, 48).toConstraint()
        } childOf this
        
        selectedText = UIText(selectedOption).constrain {
            x = 12.pixels()
            y = CenterConstraint()
            textScale = 0.9f.pixels()
            color = Color(200, 200, 205).toConstraint()
        } childOf mainBox

        dropdownIcon = UIText("▼").constrain {
            x = RelativeConstraint(1f) - 15.pixels()
            y = CenterConstraint()
            textScale = 0.8f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf mainBox

        optionsContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 100.percent() + 4.pixels()
            width = 100.percent()
            height = 0.pixels()
        } childOf this
        
        val optionsBackground = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(35, 35, 38).toConstraint()
        } childOf optionsContainer

        val optionsContent = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf optionsContainer
        optionsContent.effect(ScissorEffect())

        options.forEach { option ->
            val optionBox = UIContainer().constrain {
                y = SiblingConstraint()
                width = 100.percent()
                height = 28.pixels()
            } childOf optionsContent

            val optionText = UIText(option).constrain {
                x = 12.pixels()
                y = CenterConstraint()
                textScale = 0.9f.pixels()
                color = Color(185, 187, 190).toConstraint()
            } childOf optionBox

            optionBox.onMouseEnter {
                if (!isAnimating) {
                    optionText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.15f, Color.WHITE.toConstraint())
                    }
                    optionBox.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.15f, Color(55, 55, 58).toConstraint())
                    }
                }
            }

            optionBox.onMouseLeave {
                if (!isAnimating) {
                    optionText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.15f, Color(185, 187, 190).toConstraint())
                    }
                    optionBox.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.15f, Color(55, 55, 58).toConstraint())
                    }
                }
            }

            optionBox.onMouseClick {
                selectOption(option)
            }
        }

        mainBox.onMouseClick {
            toggleDropdown()
        }

        mainBox.onMouseEnter {
            if (!isAnimating) {
                mainBox.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(55, 55, 58).toConstraint())
                }
                selectedText.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color.WHITE.toConstraint())
                }
            }
        }

        mainBox.onMouseLeave {
            if (!expanded && !isAnimating) {
                mainBox.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(45, 45, 48).toConstraint())
                }
                selectedText.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(200, 200, 205).toConstraint())
                }
            }
        }
    }

    private fun selectOption(option: String) {
        selectedOption = option
        selectedText.setText(option)
        onSelect(option)
        closeDropdown()
    }

    private fun toggleDropdown() {
        if (isAnimating) return
        
        if (expanded) {
            closeDropdown()
        } else {
            openDropdown()
        }
    }

    private fun openDropdown() {
        if (isAnimating || expanded) return
        
        isAnimating = true
        expanded = true
        
        val targetHeight = (options.size * 28).pixels()
        
        dropdownIcon.setText("▲")
        dropdownIcon.animate {
            setColorAnimation(Animations.OUT_EXP, 0.2f, Color(88, 101, 242).toConstraint())
        }
        
        optionsContainer.animate {
            setHeightAnimation(Animations.OUT_EXP, 0.25f, targetHeight)
        }
        
        // Complete animation after delay
        isAnimating = false
    }

    private fun closeDropdown() {
        if (isAnimating || !expanded) return
        
        isAnimating = true
        expanded = false
        
        dropdownIcon.setText("▼")
        dropdownIcon.animate {
            setColorAnimation(Animations.OUT_EXP, 0.2f, Color(140, 140, 145).toConstraint())
        }
        
        optionsContainer.animate {
            setHeightAnimation(Animations.OUT_EXP, 0.25f, 0.pixels())
        }
        
        // Complete animation after delay  
        isAnimating = false
        mainBox.animate {
            setColorAnimation(Animations.OUT_EXP, 0.2f, Color(45, 45, 48).toConstraint())
        }
        selectedText.animate {
            setColorAnimation(Animations.OUT_EXP, 0.2f, Color(200, 200, 205).toConstraint())
        }
    }
}

class AnimatedCycleButton(
    private val options: List<String>,
    initialSelection: String,
    private val onChange: (String) -> Unit
) : UIComponent() {
    private var index = options.indexOf(initialSelection).let { if (it == -1) 0 else it }
    private val background: UIComponent
    private val text: UIText
    private val leftArrow: UIText
    private val rightArrow: UIText
    private var isAnimating = false

    init {
        constrain {
            width = 140.pixels()
            height = 28.pixels()
        }

        background = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(45, 45, 48).toConstraint()
        } childOf this

        leftArrow = UIText("◀").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 0.8f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf this

        text = UIText(options[index]).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.9f.pixels()
            color = Color(200, 200, 205).toConstraint()
        } childOf this

        rightArrow = UIText("▶").constrain {
            x = RelativeConstraint(1f) - 15.pixels()
            y = CenterConstraint()
            textScale = 0.8f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf this

        // Left arrow click
        leftArrow.onMouseClick {
            cycleBackward()
        }

        leftArrow.onMouseEnter {
            if (!isAnimating) {
                leftArrow.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.15f, Color(88, 101, 242).toConstraint())
                }
            }
        }

        leftArrow.onMouseLeave {
            if (!isAnimating) {
                leftArrow.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.15f, Color(140, 140, 145).toConstraint())
                }
            }
        }

        // Right arrow click
        rightArrow.onMouseClick {
            cycleForward()
        }

        rightArrow.onMouseEnter {
            if (!isAnimating) {
                rightArrow.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.15f, Color(88, 101, 242).toConstraint())
                }
            }
        }

        rightArrow.onMouseLeave {
            if (!isAnimating) {
                rightArrow.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.15f, Color(140, 140, 145).toConstraint())
                }
            }
        }

        // Center text click
        text.onMouseClick {
            cycleForward()
        }

        background.onMouseClick {
            cycleForward()
        }

        background.onMouseEnter {
            if (!isAnimating) {
                background.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(55, 55, 58).toConstraint())
                }
                text.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color.WHITE.toConstraint())
                }
            }
        }

        background.onMouseLeave {
            if (!isAnimating) {
                background.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(45, 45, 48).toConstraint())
                }
                text.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(200, 200, 205).toConstraint())
                }
            }
        }
    }

    private fun cycleForward() {
        if (isAnimating) return
        animateChange((index + 1) % options.size)
    }

    private fun cycleBackward() {
        if (isAnimating) return
        animateChange((index - 1 + options.size) % options.size)
    }

    private fun animateChange(newIndex: Int) {
        if (newIndex == index) return
        
        isAnimating = true
        
        // Slide out current text
        text.animate {
            setXAnimation(Animations.OUT_EXP, 0.15f, (-50).pixels())
        }
        
        // Update text and slide in after a short delay
        index = newIndex
        val newValue = options[index]
        text.setText(newValue)
        text.setX(RelativeConstraint(1f) + 50.pixels())
        
        text.animate {
            setXAnimation(Animations.OUT_EXP, 0.15f, CenterConstraint())
        }
        
        isAnimating = false
        onChange(newValue)
    }
}

class AnimatedToggleSwitch(initialState: Boolean) : UIContainer() {
    private var enabled = initialState
    private val background: UIComponent
    private val slider: UIComponent
    private val enabledText: UIText
    private val disabledText: UIText
    private var isAnimating = false

    var onStateChange: (Boolean) -> Unit = {}

    init {
        constrain {
            width = 64.pixels()
            height = 28.pixels()
        }

        background = UIRoundedRectangle(14f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = if (enabled) Color(88, 101, 242).toConstraint() else Color(45, 45, 48).toConstraint()
        } childOf this

        disabledText = UIText("OFF").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 0.7f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf this

        enabledText = UIText("ON").constrain {
            x = RelativeConstraint(1f) - 22.pixels()
            y = CenterConstraint()
            textScale = 0.7f.pixels()
            color = Color.WHITE.toConstraint()
        } childOf this

        slider = UIRoundedRectangle(11f).constrain {
            x = if (enabled) RelativeConstraint(1f) - 24.pixels() else 2.pixels()
            y = CenterConstraint()
            width = 22.pixels()
            height = 22.pixels()
            color = Color.WHITE.toConstraint()
        } childOf this

        updateVisibility()

        onMouseClick {
            toggle()
        }

        onMouseEnter {
            if (!isAnimating) {
                slider.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.15f, Color(240, 240, 245).toConstraint())
                }
            }
        }

        onMouseLeave {
            if (!isAnimating) {
                slider.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.15f, Color.WHITE.toConstraint())
                }
            }
        }
    }

    private fun toggle() {
        if (isAnimating) return
        
        isAnimating = true
        enabled = !enabled
        
        val targetX = if (enabled) RelativeConstraint(1f) - 24.pixels() else 2.pixels()
        val targetColor = if (enabled) Color(88, 101, 242).toConstraint() else Color(45, 45, 48).toConstraint()
        
        slider.animate {
            setXAnimation(Animations.OUT_EXP, 0.25f, targetX)
        }
        
        background.animate {
            setColorAnimation(Animations.OUT_EXP, 0.25f, targetColor)
        }
        
        // Complete animation after delay
        updateVisibility()
        isAnimating = false
        onStateChange(enabled)
    }

    private fun updateVisibility() {
        if (enabled) {
            enabledText.unhide()
            disabledText.hide()
        } else {
            enabledText.hide()
            disabledText.unhide()
        }
    }
}

class KeybindButton(private val getKey: () -> Int, private val onKeySet: (Int) -> Unit) : UIContainer() {
    private var waitingForKey = false
    private val keyText: UIText
    private val background: UIComponent

    init {
        constrain {
            width = 100.pixels()
            height = 28.pixels()
        }

        background = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(45, 45, 48).toConstraint()
        } childOf this

        keyText = UIText(getKeyName(getKey())).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.9f.pixels()
            color = Color(200, 200, 205).toConstraint()
        } childOf this

        onMouseClick {
            if (!waitingForKey) {
                startWaitingForKey()
            }
        }

        onMouseEnter {
            if (!waitingForKey) {
                background.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(55, 55, 58).toConstraint())
                }
            }
        }

        onMouseLeave {
            if (!waitingForKey) {
                background.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(45, 45, 48).toConstraint())
                }
            }
        }
    }

    private fun startWaitingForKey() {
        waitingForKey = true
        keyText.setText("Press a key...")
        background.animate {
            setColorAnimation(Animations.OUT_EXP, 0.3f, Color(88, 101, 242).toConstraint())
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

    fun handleKeyInput(keyCode: Int) {
        if (waitingForKey) {
            val finalKeyCode = if (keyCode == GLFW.GLFW_KEY_ESCAPE) GLFW.GLFW_KEY_UNKNOWN else keyCode
            waitingForKey = false
            
            onKeySet(finalKeyCode)
            keyText.setText(getKeyName(getKey()))
            
            background.animate {
                setColorAnimation(Animations.OUT_EXP, 0.3f, Color(45, 45, 48).toConstraint())
            }
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
    private var keybindButton: KeybindButton? = null
    private val selectedTabUnderline: UIComponent
    private val mainContainer: UIComponent
    private var isClosing = false
    
    // Page navigation variables
    private var currentPage = "main" // "main" or "autofish-settings"

    init {
        // Save the previous GUI scale
        previousGuiScale = MinecraftClient.getInstance().options.guiScale.value
        // Force GUI scale to 2x
        MinecraftClient.getInstance().options.guiScale.setValue(2)

        mainContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 650.pixels()
            height = 450.pixels()
        } childOf window

        // Entrance animation
        mainContainer.setY(RelativeConstraint(0.5f) + 50.pixels())
        mainContainer.animate {
            setYAnimation(Animations.OUT_EXP, 0.4f, CenterConstraint())
        }

        val background = UIRoundedRectangle(12f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(25, 25, 28).toConstraint()
        } childOf mainContainer

        // Header with gradient effect
        val header = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 60.pixels()
        } childOf mainContainer

        val headerBackground = UIRoundedRectangle(12f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(35, 35, 38).toConstraint()
        } childOf header

        // Logo/Icon placeholder
        val logoContainer = UIContainer().constrain {
            x = 20.pixels()
            y = CenterConstraint()
            width = 32.pixels()
            height = 32.pixels()
        } childOf header

        val logo = UIRoundedRectangle(8f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(88, 101, 242).toConstraint()
        } childOf logoContainer

        val logoText = UIText("F").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.2f.pixels()
            color = Color.WHITE.toConstraint()
        } childOf logo

        val title = UIText("FishMaster").constrain {
            x = 60.pixels()
            y = CenterConstraint() - 5.pixels()
            textScale = 1.8f.pixels()
            color = Color(88, 101, 242).toConstraint()
        } childOf header

        val subtitle = UIText("Advanced Fishing Automation").constrain {
            x = 60.pixels()
            y = CenterConstraint() + 8.pixels()
            textScale = 0.8f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf header

        // Animated close button
        val closeButton = UIContainer().constrain {
            x = RelativeConstraint(1f) - 45.pixels()
            y = CenterConstraint()
            width = 30.pixels()
            height = 30.pixels()
        } childOf header

        val closeButtonBg = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(0, 0, 0, 0).toConstraint()
        } childOf closeButton

        val closeButtonText = UIText("✕").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.1f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf closeButton

        closeButton.onMouseClick {
            closeWithAnimation()
        }

        closeButton.onMouseEnter {
            closeButtonBg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(220, 53, 69, 20).toConstraint())
            }
            closeButtonText.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(220, 53, 69).toConstraint())
            }
        }

        closeButton.onMouseLeave {
            closeButtonBg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(0, 0, 0, 0).toConstraint())
            }
            closeButtonText.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(140, 140, 145).toConstraint())
            }
        }

        // Tabs with improved styling
        val tabsContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 60.pixels()
            width = 100.percent()
            height = 45.pixels()
        } childOf mainContainer

        val tabsBackground = UIRoundedRectangle(12f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(30, 30, 33).toConstraint()
        } childOf tabsContainer

        // Content area with shadow effect
        contentContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 105.pixels()
            width = 100.percent()
            height = 100.percent() - 105.pixels()
        } childOf mainContainer
        contentContainer.effect(ScissorEffect())

        selectedTabUnderline = UIRoundedRectangle(2f).constrain {
            x = 15.pixels()
            y = 100.percent() - 3.pixels()
            width = 100.pixels()
            height = 3.pixels()
            color = Color(88, 101, 242).toConstraint()
        } childOf tabsContainer

        // Create tabs with animations
        tabs.forEachIndexed { index, tab ->
            val tabButton = UIContainer().constrain {
                x = (15 + index * 120).pixels()
                y = 0.pixels()
                width = 100.pixels()
                height = 100.percent()
            } childOf tabsContainer
            tabsComponents[tab] = tabButton

            val tabText = UIText(tab).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                textScale = 1.0f.pixels()
                color = if (tab == selectedTab) Color.WHITE.toConstraint() else Color(140, 140, 145).toConstraint()
            } childOf tabButton

            tabButton.onMouseClick {
                selectTab(tab, index)
            }

            tabButton.onMouseEnter {
                if (tab != selectedTab) {
                    tabText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.2f, Color(200, 200, 205).toConstraint())
                    }
                }
            }

            tabButton.onMouseLeave {
                if (tab != selectedTab) {
                    tabText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.2f, Color(140, 140, 145).toConstraint())
                    }
                }
            }
        }

        // Initialize tab content
        initializeTabContent()
        showTab(selectedTab)
    }

    private fun closeWithAnimation() {
        if (isClosing) return
        isClosing = true
        
        mainContainer.animate {
            setYAnimation(Animations.OUT_EXP, 0.3f, RelativeConstraint(0.5f) + 50.pixels())
        }
        
        // Close after animation
        close()
    }

    private fun selectTab(tab: String, index: Int) {
        if (selectedTab == tab) return
        
        selectedTab = tab
        updateTabStyles(index)
        showTabWithAnimation(tab)
    }

    private fun updateTabStyles(selectedIndex: Int) {
        tabs.forEachIndexed { index, tab ->
            val tabButton = tabsComponents[tab] ?: return
            val tabText = (tabButton.children.first() as UIText)
            if (index == selectedIndex) {
                tabText.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color.WHITE.toConstraint())
                }
            } else {
                tabText.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, Color(140, 140, 145).toConstraint())
                }
            }
        }

        val targetX = (15 + selectedIndex * 120).pixels()
        selectedTabUnderline.animate {
            setXAnimation(Animations.OUT_EXP, 0.3f, targetX)
        }
    }

    private fun initializeTabContent() {
        // Initialize content for each tab if needed
    }

    private fun showTabWithAnimation(tab: String) {
        // Simple fade effect without onComplete chaining (not available in current Elementa build)
        contentContainer.clearChildren()
        showTab(tab)
        // Optionally do a quick flash/animation on new content container children if needed later
    }

    private fun showTab(tab: String) {
        contentContainer.clearChildren()
        
        // Check if we're in a sub-page
        if (currentPage == "autofish-settings") {
            createAutoFishSettingsPage()
            return
        }
        
        // Only reset keybindButton if not on Main tab
        if (tab != "Main") keybindButton = null

        when (tab) {
            "Main" -> createMainTabContent()
            "Misc" -> createMiscTabContent()
            "Failsafes" -> createFailsafesTabContent()
            "Extras" -> createExtrasTabContent()
        }
    }

    private fun createMainTabContent() {
        val contentWrapper = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        } childOf contentContainer

        val scrollContainer = UIContainer().constrain {
            x = 30.pixels()
            y = 20.pixels()
            width = 100.percent() - 60.pixels()
            height = 100.percent() - 40.pixels()
        } childOf contentWrapper

        val autoFishCard = createFeatureCard(
            scrollContainer,
            "Auto Fishing",
            "Configure your automatic fishing keybind",
            0.pixels(),
            createKeybindControl()
        )

        // Add a small settings button next to the keybind button
        val settingsButton = UIContainer().constrain {
            x = RelativeConstraint(1f) - 240.pixels() // Positioned just left of the keybind button
            y = CenterConstraint()
            width = 28.pixels()
            height = 28.pixels()
        } childOf autoFishCard

        val settingsButtonBg = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(55, 55, 58).toConstraint()
        } childOf settingsButton

        val settingsIcon = UIText("⚙").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.0f.pixels()
            color = Color(180, 180, 185).toConstraint()
        } childOf settingsButton

        settingsButton.onMouseClick {
            openAutoFishSettings()
        }

        settingsButton.onMouseEnter {
            settingsButtonBg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(70, 70, 73).toConstraint())
            }
            settingsIcon.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color.WHITE.toConstraint())
            }
        }

        settingsButton.onMouseLeave {
            settingsButtonBg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(55, 55, 58).toConstraint())
            }
            settingsIcon.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(180, 180, 185).toConstraint())
            }
        }

        val toggle = createToggleControl(ConfigBridge.isSeaCreatureKillerEnabled()) { }
        
        createFeatureCard(
            scrollContainer,
            "Sea Creature Killer",
            "Automatically eliminate sea creatures when caught",
            SiblingConstraint(15f),
            toggle
        )

        val attackModeCard = createFeatureCard(
            scrollContainer,
            "Attack Mode",
            "Choose how to attack sea creatures",
            SiblingConstraint(15f),
            createModeSelector()
        )

        (toggle as AnimatedToggleSwitch).onStateChange = { newState ->
            SeaCreatureKiller.setEnabled(newState)
            if (newState) {
                attackModeCard.unhide(true)
            } else {
                attackModeCard.hide(true)
            }
        }

        if (!ConfigBridge.isSeaCreatureKillerEnabled()) {
            attackModeCard.hide()
        }
    }

    private fun createMiscTabContent() {
        val label = UIText("Miscellaneous Settings").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.2f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf contentContainer
    }

    private fun createFailsafesTabContent() {
        val label = UIText("Failsafe Configuration").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.2f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf contentContainer
    }

    private fun createExtrasTabContent() {
        val contentWrapper = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        } childOf contentContainer

        val scrollContainer = UIContainer().constrain {
            x = 30.pixels()
            y = 20.pixels()
            width = 100.percent() - 60.pixels()
            height = 100.percent() - 40.pixels()
        } childOf contentWrapper

        // Auto Harp Feature
        val autoHarpToggle = createToggleControl(
            rohan.fishmaster.qol.AutoHarp.isEnabled()
        ) { newState ->
            if (newState != rohan.fishmaster.qol.AutoHarp.isEnabled()) {
                rohan.fishmaster.qol.AutoHarp.toggle()
            }
        }

        createFeatureCard(
            scrollContainer,
            "Auto Harp",
            "Automatically complete Melody's Harp minigame",
            0.pixels(),
            autoHarpToggle
        )
    }

    private fun createFeatureCard(parent: UIComponent, title: String, description: String, yConstraint: YConstraint, control: UIComponent): UIComponent {
        val card = UIContainer().constrain {
            x = 0.pixels()
            y = yConstraint
            width = 100.percent()
            height = 80.pixels()
        } childOf parent

        val cardBackground = UIRoundedRectangle(8f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(35, 35, 38).toConstraint()
        } childOf card

        val titleText = UIText(title).constrain {
            x = 20.pixels()
            y = 15.pixels()
            textScale = 1.1f.pixels()
            color = Color.WHITE.toConstraint()
        } childOf card

        val descText = UIText(description).constrain {
            x = 20.pixels()
            y = 35.pixels()
            textScale = 0.8f.pixels()
            color = Color(140, 140, 145).toConstraint()
        } childOf card

        control.constrain {
            x = RelativeConstraint(1f) - 200.pixels() // Increased margin to prevent overflow
            y = CenterConstraint()
        } childOf card

        // Hover effect
        card.onMouseEnter {
            cardBackground.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(40, 40, 43).toConstraint())
            }
        }

        card.onMouseLeave {
            cardBackground.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(35, 35, 38).toConstraint())
            }
        }
        return card
    }

    private fun createKeybindControl(): UIComponent {
        if (keybindButton == null) {
            keybindButton = KeybindButton({ ConfigBridge.getAutoFishingKeybind() }) { keyCode ->
                ConfigBridge.setAutoFishingKeybind(keyCode)
            }
        }
        return keybindButton!!
    }

    private fun createToggleControl(initialState: Boolean, onStateChange: (Boolean) -> Unit): UIComponent {
        return AnimatedToggleSwitch(initialState).apply {
            this.onStateChange = onStateChange
        }
    }

    private fun openAutoFishSettings() {
        currentPage = "autofish-settings"
        showTab(selectedTab) // This will trigger the settings page creation
    }

    private fun goBackToMainTab() {
        currentPage = "main"
        showTab(selectedTab) // This will show the normal tab content
    }

    private fun createAutoFishSettingsPage() {
        val contentWrapper = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        } childOf contentContainer

        // Back button
        val backButton = UIContainer().constrain {
            x = 20.pixels()
            y = 15.pixels()
            width = 40.pixels()
            height = 30.pixels()
        } childOf contentWrapper

        val backButtonBg = UIRoundedRectangle(6f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(45, 45, 48).toConstraint()
        } childOf backButton

        val backArrow = UIText("←").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.2f.pixels()
            color = Color(200, 200, 205).toConstraint()
        } childOf backButton

        backButton.onMouseClick {
            goBackToMainTab()
        }

        backButton.onMouseEnter {
            backButtonBg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(55, 55, 58).toConstraint())
            }
            backArrow.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color.WHITE.toConstraint())
            }
        }

        backButton.onMouseLeave {
            backButtonBg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(45, 45, 48).toConstraint())
            }
            backArrow.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, Color(200, 200, 205).toConstraint())
            }
        }

        // Page title
        val title = UIText("Auto Fishing Settings").constrain {
            x = 80.pixels()
            y = 20.pixels()
            textScale = 1.4f.pixels()
            color = Color.WHITE.toConstraint()
        } childOf contentWrapper

        // Settings container
        val settingsContainer = UIContainer().constrain {
            x = 30.pixels()
            y = 70.pixels()
            width = 100.percent() - 60.pixels()
            height = 100.percent() - 100.pixels()
        } childOf contentWrapper

        // Recast delay setting with cycle button
        val delayOptions = (100..1500 step 50).map { "${it}ms" } // Creates: ["100ms", "150ms", "200ms", ..., "1500ms"]
        val currentDelayMs = (ConfigBridge.getRecastDelay() * 50f).toInt()
        val currentDelayText = "${currentDelayMs}ms"
        val initialDelay = if (delayOptions.contains(currentDelayText)) currentDelayText else "250ms"
        
        val recastDelayButton = AnimatedCycleButton(delayOptions, initialDelay) { selected ->
            val delayMs = selected.removeSuffix("ms").toFloat()
            ConfigBridge.setRecastDelay(delayMs / 50f) // Convert milliseconds back to ticks
        }

        createFeatureCard(
            settingsContainer,
            "Recast Delay",
            "Delay between fishing rod casts (lower = faster)",
            0.pixels(),
            recastDelayButton
        )
    }

    private fun createModeSelector(): UIComponent {
        val options = listOf("RCM", "Fire Veil Wand")
        val initial = ConfigBridge.getSeaCreatureKillerMode().let { if (options.contains(it)) it else "RCM" }
        
        return AnimatedCycleButton(options, initial) { selected ->
            ConfigBridge.setSeaCreatureKillerMode(selected)
            // Announce chosen mode
            MinecraftClient.getInstance().player?.sendMessage(
                Text.literal("[FishMaster] Sea Creature Killer mode set to: ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(selected).formatted(Formatting.AQUA)),
                false
            )
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

const val FEATURE_BUTTON_WIDTH = 140f
