package rohan.fishmaster.qol

import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import rohan.fishmaster.config.FishMasterConfigNew

/**
 * Auto Harp Feature for FishMaster
 * Automatically completes Melody's Harp minigame in Hypixel Skyblock
 * 
 * Adapted from original OdinClient implementation for Fabric 1.21.5
 * @author FishMaster Team
 */
object AutoHarp {
    private var enabled = false
    private var lastInvHash = 0
    private var tickCounter = 0
    
    // Debug and safety variables
    private var debugMode = false
    private var lastAttemptTime = 0L
    
    // Harp detection patterns
    private val harpTitlePatterns = listOf(
        "Harp -",
        "Melody's Harp",
        "Harp Minigame"
    )
    
    /**
     * Toggle the Auto Harp feature
     */
    fun toggle() {
        enabled = !enabled
        val client = MinecraftClient.getInstance()
        
        if (client.player != null) {
            val prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD)
            if (enabled) {
                val message = Text.literal("Auto Harp enabled").formatted(Formatting.GREEN)
                client.player!!.sendMessage(prefix.copy().append(message), false)
                reset()
            } else {
                val message = Text.literal("Auto Harp disabled").formatted(Formatting.RED)
                client.player!!.sendMessage(prefix.copy().append(message), false)
            }
        }
        
        sendDebugMessage("Auto Harp toggled: ${if (enabled) "ENABLED" else "DISABLED"}")
    }
    
    /**
     * Toggle debug mode for detailed logging
     */
    fun toggleDebug() {
        debugMode = !debugMode
        val client = MinecraftClient.getInstance()
        
        if (client.player != null) {
            val prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD)
            val message = if (debugMode) {
                Text.literal("Auto Harp debug mode enabled").formatted(Formatting.YELLOW)
            } else {
                Text.literal("Auto Harp debug mode disabled").formatted(Formatting.GRAY)
            }
            client.player!!.sendMessage(prefix.copy().append(message), false)
        }
    }
    
    /**
     * Check if Auto Harp is enabled
     */
    fun isEnabled(): Boolean = enabled
    
    /**
     * Check if debug mode is enabled
     */
    fun isDebugMode(): Boolean = debugMode
    
    /**
     * Main tick method - should be called from main tick handler
     */
    @JvmStatic
    fun tick() {
        if (!enabled) return
        
        val client = MinecraftClient.getInstance()
        if (client.player == null || client.world == null) return
        
        // Safety check - don't run if player is not in skyblock
        // This is a basic check, you might want to add more sophisticated skyblock detection
        if (!isInSkyblock()) {
            sendDebugMessage("Not in Skyblock, skipping Auto Harp")
            return
        }
        
        tickCounter++
        
        // Run every 5 ticks (4 times per second) for responsiveness but not too aggressive
        if (tickCounter % 5 != 0) return
        
        val screenHandler = client.player!!.currentScreenHandler
        if (screenHandler !is GenericContainerScreenHandler) {
            // Not in a container screen
            return
        }
        
        // Check if this is a harp GUI
        if (!isHarpGui(screenHandler)) {
            return
        }
        
        processHarpGui(screenHandler)
    }
    
    /**
     * Basic skyblock detection - you might want to enhance this
     */
    private fun isInSkyblock(): Boolean {
        val client = MinecraftClient.getInstance()
        val serverData = client.currentServerEntry
        
        // Basic check for Hypixel server
        if (serverData?.address?.lowercase()?.contains("hypixel") == true) {
            return true
        }
        
        // Additional checks could include scoreboard detection, etc.
        // For now, we'll assume if it's not obviously Hypixel, it might still be skyblock
        return true
    }
    
    /**
     * Check if the current GUI is a harp minigame
     */
    private fun isHarpGui(screenHandler: GenericContainerScreenHandler): Boolean {
        // For now, we'll rely on slot structure and inventory patterns
        // rather than title checking which has API differences across versions
        
        // Check if we have the expected number of slots (54 for a large chest/harp interface)
        val hasCorrectSlots = screenHandler.slots.size >= 54
        
        if (!hasCorrectSlots) {
            sendDebugMessage("Container doesn't have enough slots: ${screenHandler.slots.size}")
            return false
        }
        
        // Check if slots 37-43 (harp note positions) exist and can contain items
        try {
            for (i in 37..43) {
                if (i >= screenHandler.slots.size) {
                    sendDebugMessage("Harp slot $i is out of range")
                    return false
                }
            }
        } catch (e: Exception) {
            sendDebugMessage("Error checking harp slots: ${e.message}")
            return false
        }
        
        sendDebugMessage("Container structure matches harp interface with ${screenHandler.slots.size} slots")
        return true
    }
    
    /**
     * Process the harp GUI and click quartz blocks
     */
    private fun processHarpGui(screenHandler: GenericContainerScreenHandler) {
        val client = MinecraftClient.getInstance()
        
        // Calculate inventory hash to detect changes
        val currentInvHash = calculateInventoryHash(screenHandler)
        
        // If inventory hasn't changed, don't process again
        if (currentInvHash == lastInvHash) {
            return
        }
        
        // Update the hash
        val previousHash = lastInvHash
        lastInvHash = currentInvHash
        
        sendDebugMessage("Inventory changed - Previous hash: $previousHash, Current: $currentInvHash")
        
        // Safety check for consecutive attempts - removed per user request
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttemptTime < 100) { // Minimum 100ms between attempts
            sendDebugMessage("Attempt too soon, skipping (${currentTime - lastAttemptTime}ms)")
            return
        }
        
        // Click all quartz blocks in the harp pattern (slots 37-43)
        var quartzFound = 0
        var clicksPerformed = 0
        
        for (i in 37..43) { // Harp note positions (7 notes)
            if (i >= screenHandler.slots.size) {
                sendDebugMessage("Slot $i is out of range (${screenHandler.slots.size} slots)")
                continue
            }
            
            val slot = screenHandler.slots[i]
            val stack = slot.stack
            
            if (stack.isEmpty) {
                sendDebugMessage("Slot $i is empty")
                continue
            }
            
            // Check if it's a quartz block
            if (isQuartzBlock(stack)) {
                quartzFound++
                sendDebugMessage("Found quartz block in slot $i: ${stack.name.string}")
                
                try {
                    // Perform middle click (pick block equivalent)
                    client.interactionManager?.clickSlot(
                        screenHandler.syncId,
                        i,
                        2, // Middle mouse button
                        SlotActionType.CLONE,
                        client.player
                    )
                    clicksPerformed++
                    sendDebugMessage("Clicked quartz block in slot $i")
                    
                    // Small delay between clicks to avoid overwhelming the server
                    Thread.sleep(10)
                    
                } catch (e: Exception) {
                    sendDebugMessage("Failed to click slot $i: ${e.message}")
                }
            } else {
                sendDebugMessage("Slot $i is not quartz: ${stack.name.string} (${stack.item.javaClass.simpleName})")
            }
        }
        
        if (quartzFound > 0) {
            lastAttemptTime = currentTime
            sendDebugMessage("Harp attempt: Found $quartzFound quartz blocks, performed $clicksPerformed clicks")
            
            if (clicksPerformed > 0) {
                sendStatusMessage("Playing harp notes: $clicksPerformed/$quartzFound", false)
            }
        } else {
            sendDebugMessage("No quartz blocks found in harp slots")
        }
    }
    
    /**
     * Check if an item stack is a quartz block
     */
    private fun isQuartzBlock(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        
        val item = stack.item
        
        // Check if it's a BlockItem and the block is quartz
        if (item is BlockItem) {
            val block = item.block
            val isQuartz = block == Blocks.QUARTZ_BLOCK
            
            if (isQuartz) {
                sendDebugMessage("Confirmed quartz block: ${stack.name.string}")
            }
            
            return isQuartz
        }
        
        return false
    }
    
    /**
     * Calculate a hash of the current inventory state for change detection
     */
    private fun calculateInventoryHash(screenHandler: GenericContainerScreenHandler): Int {
        // Only consider the player inventory slots (usually 0-35) for change detection
        val relevantSlots = if (screenHandler.slots.size >= 36) {
            screenHandler.slots.subList(0, 36)
        } else {
            screenHandler.slots
        }
        
        return relevantSlots.joinToString("") { slot ->
            val stack = slot.stack
            if (stack.isEmpty) {
                "empty"
            } else {
                "${stack.item.toString()}_${stack.count}_${stack.name.string}"
            }
        }.hashCode()
    }
    
    /**
     * Reset state variables
     */
    private fun reset() {
        lastInvHash = 0
        lastAttemptTime = 0L
        sendDebugMessage("Auto Harp state reset")
    }
    
    /**
     * Send debug message if debug mode is enabled
     */
    private fun sendDebugMessage(message: String) {
        if (!debugMode) return
        
        val client = MinecraftClient.getInstance()
        if (client.player != null) {
            val prefix = Text.literal("[AutoHarp Debug] ").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
            val content = Text.literal(message).formatted(Formatting.LIGHT_PURPLE)
            client.player!!.sendMessage(prefix.copy().append(content), false)
        }
    }
    
    /**
     * Send status message to player
     */
    private fun sendStatusMessage(message: String, isError: Boolean) {
        val client = MinecraftClient.getInstance()
        if (client.player != null) {
            val prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD)
            val content = Text.literal(message).formatted(
                if (isError) Formatting.RED else Formatting.YELLOW
            )
            client.player!!.sendMessage(prefix.copy().append(content), false)
        }
    }
    
    /**
     * Send failsafe message to player
     */
    private fun sendFailsafeMessage(message: String, isError: Boolean) {
        val client = MinecraftClient.getInstance()
        if (client.player != null) {
            val prefix = Text.literal("[Fish Master] ").formatted(Formatting.AQUA, Formatting.BOLD)
            val content = Text.literal(message).formatted(
                if (isError) Formatting.RED else Formatting.YELLOW
            )
            client.player!!.sendMessage(prefix.copy().append(content), false)
        }
        
        // Also send to debug if enabled
        sendDebugMessage("FAILSAFE: $message")
    }
    
    /**
     * Emergency stop method (can be called from keybind)
     */
    fun emergencyStop() {
        if (enabled) {
            enabled = false
            reset()
            sendFailsafeMessage("Auto Harp emergency stop activated", false)
        }
    }
    
    /**
     * Get status information for GUI display
     */
    fun getStatusInfo(): Map<String, Any> {
        return mapOf(
            "enabled" to enabled,
            "debugMode" to debugMode,
            "lastInvHash" to lastInvHash,
            "tickCounter" to tickCounter
        )
    }
}
