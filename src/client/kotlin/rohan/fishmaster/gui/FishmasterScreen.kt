package rohan.fishmaster.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.GradientComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

// FishmasterScreen.kt
// This file defines the FishmasterScreen class, which is a custom screen for the Fishmaster mod.
// It uses Elementa for UI rendering and displays a simple black block in the center of the screen.
class FishmasterScreen : WindowScreen() {
    private val pages = mutableMapOf<String, UIBlock>()
    private var activePage: String = "Main"
    private lateinit var tabBar: UIBlock

    init {
        val container = GradientComponent(
            Color(25, 25, 112), // Midnight Blue
            Color(70, 130, 180)  // Steel Blue
        ).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 300.pixels()
            height = 220.pixels() // Increased height for tabs
        } effect OutlineEffect(Color.WHITE, 2f) childOf window

        val titleBar = UIBlock(Color(15, 15, 80)).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        UIText("FishMaster").constrain {
            x = 5.pixels()
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.WHITE)
        } childOf titleBar

        tabBar = UIBlock(Color(0, 0, 0, 50)).constrain {
            x = 0.pixels()
            y = 25.pixels()
            width = 100.percent()
            height = 20.pixels()
        } childOf container

        createTabs(tabBar)
        createPages(container)

        showPage(activePage)
    }

    private fun createTabs(parent: UIBlock) {
        val tabs = listOf("Main", "Misc", "Webhook", "Failsafes")
        var lastTab: UIComponent? = null

        for (tabName in tabs) {
            val tabContainer = UIBlock(Color(0, 0, 0, 0)).constrain {
                x = if (lastTab == null) 5.pixels() else SiblingConstraint(10f) // Increased distance
                y = CenterConstraint()
                width = ChildBasedSizeConstraint() + 10.pixels()
                height = ChildBasedSizeConstraint() + 5.pixels()
            }
            .effect(OutlineEffect(Color.DARK_GRAY, 1f)) // Subtle border
            .onMouseEnter {
                (this.effects.first() as OutlineEffect).color = Color.WHITE
            }
            .onMouseLeave {
                if (activePage != tabName) {
                    (this.effects.first() as OutlineEffect).color = Color.DARK_GRAY
                }
            }
            .onMouseClick {
                showPage(tabName)
            } childOf parent

            UIText(tabName).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = ConstantColorConstraint(Color.LIGHT_GRAY)
            }.onMouseEnter {
                (this as UIText).setColor(ConstantColorConstraint(Color.WHITE))
            }.onMouseLeave {
                if (activePage != tabName) {
                    (this as UIText).setColor(ConstantColorConstraint(Color.LIGHT_GRAY))
                }
            } childOf tabContainer

            lastTab = tabContainer
        }
    }

    private fun createPages(parent: UIBlock) {
        pages["Main"] = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 5.pixels()
            y = 50.pixels()
            width = 100.percent() - 10.pixels()
            height = 100.percent() - 55.pixels()
        }.childOf(parent).also { pageContainer ->
            UIText("Main Page Content").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = ConstantColorConstraint(Color.WHITE)
            } childOf pageContainer
        }

        pages["Misc"] = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 5.pixels()
            y = 50.pixels()
            width = 100.percent() - 10.pixels()
            height = 100.percent() - 55.pixels()
        }.childOf(parent).also { pageContainer ->
            UIText("Misc Page Content").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = ConstantColorConstraint(Color.WHITE)
            } childOf pageContainer
        }

        pages["Webhook"] = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 5.pixels()
            y = 50.pixels()
            width = 100.percent() - 10.pixels()
            height = 100.percent() - 55.pixels()
        }.childOf(parent).also { pageContainer ->
            UIText("Webhook Page Content").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = ConstantColorConstraint(Color.WHITE)
            } childOf pageContainer
        }

        pages["Failsafes"] = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 5.pixels()
            y = 50.pixels()
            width = 100.percent() - 10.pixels()
            height = 100.percent() - 55.pixels()
        }.childOf(parent).also { pageContainer ->
            UIText("Failsafes Page Content").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = ConstantColorConstraint(Color.WHITE)
            } childOf pageContainer
        }
    }

    private fun showPage(name: String) {
        activePage = name
        pages.forEach { (pageName, page) ->
            page.hide(pageName != name)
        }
        // Highlight active tab
        tabBar.children.forEach { tabContainer ->
            val tabText = tabContainer.children.first() as UIText
            val outline = tabContainer.effects.first() as OutlineEffect
            if (tabText.getText() == name) {
                tabText.setColor(ConstantColorConstraint(Color.WHITE))
                outline.color = Color.WHITE
            } else {
                tabText.setColor(ConstantColorConstraint(Color.LIGHT_GRAY))
                outline.color = Color.DARK_GRAY
            }
        }
    }
}
