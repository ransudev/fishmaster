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
    private var expanded = false
    private val optionsContainer: UIComponent
    private val mainBox: UIComponent

    init {
        constrain {
            width = 120.pixels()
            height = 20.pixels()
        }

        mainBox = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(5f.pixels()) childOf this
        mainBox.effect(OutlineEffect(Color.BLACK, 1f))

        selectedText = UIText(initialSelection).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
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
                height = 20.pixels()
            } childOf optionsContainer

            val optionBackground = UIBlock().constrain {
                width = 100.percent()
                height = 100.percent()
                color = Color(70, 70, 70).toConstraint()
            }.setRadius(5f.pixels()) childOf optionBox
            optionBackground.effect(OutlineEffect(Color.BLACK, 1f))

            val optionText = UIText(option).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
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
                selectedText.setText(option)
                onSelect(option)
                toggleDropdown()
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
            width = 40.pixels()
            height = 20.pixels()
        }

        background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(5f.pixels()) childOf this
        background.effect(OutlineEffect(Color.BLACK, 1f))


        text.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
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
            width = 100.pixels()
            height = 20.pixels()
        }

        background = UIBlock().constrain {
            width = 100.percent()
            height = 100.percent()
            color = Color(70, 70, 70).toConstraint()
        }.setRadius(5f.pixels()) childOf this
        background.effect(OutlineEffect(Color.BLACK, 1f))

        keyText = UIText(getKeyName(initialKey)).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
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
            waitingForKey = false
            onKeySet(keyCode)
            keyText.setText(getKeyName(keyCode))
            updateColor()
        }
    }
}

class FishmasterScreen : WindowScreen(ElementaVersion.V5, drawDefaultBackground = false) {
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
            height = 300.pixels()
        } childOf window

        // Background with gradient and border
        val background = UIBlock().constrain {
            width = FillConstraint()
            height = FillConstraint()
            color = Color(18, 18, 18).toConstraint()
        } childOf mainContainer
        background.effect(OutlineEffect(Color.BLACK, 2f))


        // Title
        UIText("FishMaster").constrain {
            x = CenterConstraint()
            y = 10.pixels()
            textScale = 1.5f.pixels()
            color = Color.WHITE.toConstraint()
        } childOf mainContainer

        // Tabs container
        val tabsContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = 40.pixels()
            width = ChildBasedSizeConstraint(padding = 10f)
            height = 22.pixels()
        } childOf mainContainer

        tabs.forEach { tabName ->
            val tab = UIContainer().constrain {
                x = SiblingConstraint(10f)
                y = CenterConstraint()
                width = ChildBasedSizeConstraint(padding = 5f)
                height = ChildBasedSizeConstraint()
            } childOf tabsContainer

            val tabText = UIText(tabName).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = Color.LIGHT_GRAY.toConstraint()
                textScale = 1.2f.pixels()
            } childOf tab

            tabsComponents[tabName] = tab

            tab.onMouseEnter {
                if (selectedTab != tabName) {
                    tabText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, Color.WHITE.toConstraint())
                    }
                }
            }
            tab.onMouseLeave {
                if (selectedTab != tabName) {
                    tabText.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, Color.LIGHT_GRAY.toConstraint())
                    }
                }
            }
            tab.onMouseClick {
                selectedTab = tabName
                updateTabs()
                updateContent()
            }
        }

        selectedTabUnderline = UIBlock(Color.WHITE).constrain {
            width = 0.pixels()
            height = 2.pixels()
            x = CenterConstraint()
            y = 20.pixels()
        } childOf (tabsComponents[selectedTab] ?: tabsContainer)

        // Content container
        contentContainer = UIBlock().constrain {
            x = 10.pixels()
            y = 70.pixels()
            width = 100.percent() - 20.pixels()
            height = 100.percent() - 80.pixels()
            color = Color(0, 0, 0, 0).toConstraint()
        } childOf mainContainer

        updateTabs()
        updateContent()

        window.onKeyType { _, keyCode ->
            keybindButton?.handleKeyInput(keyCode)
        }
    }

    private fun updateTabs() {
        tabsComponents.forEach { (name, component) ->
            val text = component.children.first() as UIText
            if (name == selectedTab) {
                text.setColor(Color.WHITE.toConstraint())
            } else {
                text.setColor(Color.LIGHT_GRAY.toConstraint())
            }
        }
        val selectedTabComponent = tabsComponents[selectedTab]!!
        selectedTabUnderline.parent.removeChild(selectedTabUnderline)
        selectedTabComponent.addChild(selectedTabUnderline)

        val textWidth = (selectedTabComponent.children.first() as UIText).getWidth()
        val targetWidth = textWidth + 10f // Corresponds to the padding of the tab container

        selectedTabUnderline.animate {
            setXAnimation(Animations.OUT_EXP, 0.5f, CenterConstraint())
            setWidthAnimation(Animations.OUT_EXP, 0.5f, targetWidth.pixels())
        }
    }

    private fun updateContent() {
        contentContainer.clearChildren()
        keybindButton = null // Reset keybind button on tab change

        when (selectedTab) {
            "Main" -> {
                val keybindRow = UIContainer().constrain {
                    y = 10.pixels()
                    x = CenterConstraint()
                    width = 95.percent()
                    height = 30.pixels()
                } childOf contentContainer

                UIBlock().constrain {
                    width = 100.percent()
                    height = 100.percent()
                    color = Color(70, 70, 70).toConstraint()
                }.setRadius(5f.pixels()) childOf keybindRow

                UIText("Auto Fishing Keybind").constrain {
                    x = 20.pixels()
                    y = CenterConstraint()
                    color = Color.WHITE.toConstraint()
                } childOf keybindRow

                keybindButton = SimpleKeybindButton(FishMasterConfig.getAutoFishingKeybind()) {
                    FishMasterConfig.setAutoFishingKeybind(it)
                }.constrain {
                    x = RelativeConstraint(0.95f) - width
                    y = CenterConstraint()
                } childOf keybindRow

                val sckRow = UIContainer().constrain {
                    y = SiblingConstraint(10f)
                    x = CenterConstraint()
                    width = 95.percent()
                    height = 30.pixels()
                } childOf contentContainer

                UIBlock().constrain {
                    width = 100.percent()
                    height = 100.percent()
                    color = Color(70, 70, 70).toConstraint()
                }.setRadius(5f.pixels()) childOf sckRow

                UIText("Sea Creature Killer").constrain {
                    x = 20.pixels()
                    y = CenterConstraint()
                    color = Color.WHITE.toConstraint()
                } childOf sckRow

                SimpleSwitch(FishMasterConfig.isSeaCreatureKillerEnabled()).apply {
                    onStateChange = { newState ->
                        FishMasterConfig.setSeaCreatureKillerEnabled(newState)
                        SeaCreatureKiller.setEnabled(newState)
                    }
                }.constrain {
                    x = RelativeConstraint(0.95f) - width
                    y = CenterConstraint()
                } childOf sckRow

                val attackModeRow = UIContainer().constrain {
                    y = SiblingConstraint(10f)
                    x = CenterConstraint()
                    width = 95.percent()
                    height = 30.pixels()
                } childOf contentContainer

                UIBlock().constrain {
                    width = 100.percent()
                    height = 100.percent()
                    color = Color(70, 70, 70).toConstraint()
                }.setRadius(5f.pixels()) childOf attackModeRow

                UIText("Attack Mode").constrain {
                    x = 20.pixels()
                    y = CenterConstraint()
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
                    x = RelativeConstraint(0.95f) - width
                    y = CenterConstraint()
                } childOf attackModeRow
            }
            else -> {
                // For other tabs, just show a label
                UIText("Content for $selectedTab").constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                    color = Color.WHITE.toConstraint()
                } childOf contentContainer
            }
        }
    }
}
