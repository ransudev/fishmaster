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
import rohan.fishmaster.config.FishMasterConfig
import rohan.fishmaster.feature.SeaCreatureKiller
import java.awt.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

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
            width = 80.pixels()
            height = 16.pixels()
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
            textScale = 0.7f.pixels()
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
                height = 16.pixels()
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
                textScale = 0.7f.pixels()
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
            optionBox.onMouseClick {
                selectedOption = option
                selectedText.setText(option)
                onSelect(option)
                optionsContainer.hide(true) // Ensure dropdown closes
                expanded = false // Ensure state is correct
            }
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

class SimpleSwitch(initialState: Boolean) : UIComponent() {
    private var enabled = initialState
    val text = UIText(if (enabled) "ON" else "OFF")
    private val background: UIComponent

    var onStateChange: (Boolean) -> Unit = {}

    init {
        constrain {
            width = 30.pixels()
            height = 15.pixels()
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
            textScale = 0.6f.pixels()
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

class SimpleKeybindButton(initialKey: Int, private val onKeySet: (Int) -> Unit) : UIComponent() {
    private var waitingForKey = false
    private val keyText: UIText
    private val background: UIComponent

    init {
        constrain {
            width = 60.pixels()
            height = 16.pixels()
        }

        background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(3f.pixels()) childOf this
        background.effect(OutlineEffect(Color.BLACK, 1f))

        keyText = UIText(getKeyName(initialKey)).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.7f.pixels()
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

    private fun getKeyName(key: Int) = if (key == GLFW.GLFW_KEY_UNKNOWN) "None" else GLFW.glfwGetKeyName(key, 0) ?: "Unknown"

    private fun updateColor() {
        val textColor = if (waitingForKey) Color(114, 137, 218) else Color(185, 187, 190)
        keyText.setColor(textColor)
    }

    fun handleKeyInput(keyCode: Int) {
        if (waitingForKey) {
            val finalKeyCode = if (keyCode == GLFW.GLFW_KEY_ESCAPE) GLFW.GLFW_KEY_UNKNOWN else keyCode
            waitingForKey = false
            onKeySet(finalKeyCode)
            keyText.setText(getKeyName(finalKeyCode))
            updateColor()
        }
    }

    fun isWaiting() = waitingForKey
}

class FishmasterScreen : WindowScreen(ElementaVersion.V5) {
    private val tabs = listOf("Main", "Misc", "Failsafes", "Webhook")
    private var selectedTab = tabs.first()
    private val contentContainer: UIComponent
    private val tabsComponents = mutableMapOf<String, UIComponent>()
    private var keybindButton: SimpleKeybindButton? = null
    private val selectedTabUnderline: UIComponent

    init {
        val mainContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 500.pixels()
            height = 350.pixels()
        } childOf window

        val background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(45, 45, 45).toConstraint()
        }.setRadius(10f.pixels()) childOf mainContainer
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
        }.setRadius(10f.pixels()) childOf header
        headerBackground.effect(OutlineEffect(Color.BLACK, 1f))

        val title = UIText("FishMaster").constrain {
            x = 20.pixels()
            y = CenterConstraint()
            textScale = 1.5f.pixels()
            color = Color(88, 101, 242).toConstraint()
        } childOf header

        val closeButton = UIContainer().constrain {
            x = RelativeConstraint(1f) - 25.pixels()
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
            y = 100.percent() - 2.pixels()
            width = 70.pixels()
            height = 2.pixels()
            color = Color(88, 101, 242).toConstraint()
        } childOf tabsContainer

        // Create tabs
        tabs.forEachIndexed { index, tab ->
            val tabButton = UIContainer().constrain {
                x = (index * 70).pixels()
                y = 0.pixels()
                width = 70.pixels()
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

        val targetX = (selectedIndex * 70).toFloat()
        selectedTabUnderline.animate {
            setXAnimation(Animations.OUT_EXP, 0.3f, targetX.pixels())
        }
    }

    private fun initializeTabContent() {
        // Initialize content for each tab if needed
    }

    private fun showTab(tab: String) {
        contentContainer.clearChildren()
        keybindButton = null // Reset keybind button on tab change

        when (tab) {
            "Main" -> {
                val keybindRow = UIContainer().constrain {
                    y = 10.pixels()
                    x = CenterConstraint()
                    width = 95.percent()
                    height = 25.pixels()
                } childOf contentContainer

                UIBlock().constrain {
                    width = 100.percent()
                    height = 100.percent()
                    color = Color(70, 70, 70).toConstraint()
                }.setRadius(5f.pixels()) childOf keybindRow

                UIText("Auto Fishing Keybind").constrain {
                    x = 15.pixels()
                    y = CenterConstraint()
                    textScale = 0.8f.pixels()
                    color = Color.WHITE.toConstraint()
                } childOf keybindRow

                keybindButton = SimpleKeybindButton(FishMasterConfig.getAutoFishingKeybind()) {
                    FishMasterConfig.setAutoFishingKeybind(it)
                }.constrain {
                    x = RelativeConstraint(0.97f) - width
                    y = CenterConstraint()
                } childOf keybindRow

                val sckRow = UIContainer().constrain {
                    y = SiblingConstraint(5f)
                    x = CenterConstraint()
                    width = 95.percent()
                    height = 25.pixels()
                } childOf contentContainer

                UIBlock().constrain {
                    width = 100.percent()
                    height = 100.percent()
                    color = Color(70, 70, 70).toConstraint()
                }.setRadius(5f.pixels()) childOf sckRow

                UIText("Sea Creature Killer").constrain {
                    x = 15.pixels()
                    y = CenterConstraint()
                    textScale = 0.8f.pixels()
                    color = Color.WHITE.toConstraint()
                } childOf sckRow

                SimpleSwitch(FishMasterConfig.isSeaCreatureKillerEnabled()).apply {
                    onStateChange = { newState ->
                        FishMasterConfig.setSeaCreatureKillerEnabled(newState)
                        SeaCreatureKiller.setEnabled(newState)
                    }
                }.constrain {
                    x = RelativeConstraint(0.97f) - width
                    y = CenterConstraint()
                } childOf sckRow

                val attackModeRow = UIContainer().constrain {
                    y = SiblingConstraint(5f)
                    x = CenterConstraint()
                    width = 95.percent()
                    height = 25.pixels()
                } childOf contentContainer

                UIBlock().constrain {
                    width = 100.percent()
                    height = 100.percent()
                    color = Color(70, 70, 70).toConstraint()
                }.setRadius(5f.pixels()) childOf attackModeRow

                UIText("Attack Mode").constrain {
                    x = 15.pixels()
                    y = CenterConstraint()
                    textScale = 0.8f.pixels()
                    color = Color.WHITE.toConstraint()
                } childOf attackModeRow

                SimpleDropdown(
                    listOf("RCM", "Melee", "Fire Veil Wand"),
                    FishMasterConfig.getSeaCreatureKillerMode()
                ) {
                    FishMasterConfig.setSeaCreatureKillerMode(it)
                    if (it != "RCM") {
                        MinecraftClient.getInstance().player?.sendMessage(
                            Text.literal("[FishMaster] $it mode is a Work In Progress.").formatted(Formatting.YELLOW),
                            false
                        )
                    }
                }.constrain {
                    x = RelativeConstraint(0.97f) - width
                    y = CenterConstraint()
                } childOf attackModeRow
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
        if (keybindButton?.isWaiting() == true) {
            keybindButton?.handleKeyInput(keyCode)
        } else {
            super.onKeyPressed(keyCode, typedChar, modifiers)
        }
    }
}
