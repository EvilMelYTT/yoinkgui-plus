package me.thatonedevil.handlers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.thatonedevil.nbt.NBTParser
import me.thatonedevil.YoinkGUIClient
import me.thatonedevil.inventory.TopInventory
import me.thatonedevil.inventory.YoinkInventory
import me.thatonedevil.inventory.YoinkInventory.Companion.yoinkSingleItem
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils.sendChat
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack

object ItemParseHandler {

    fun handleSingleItemParse(itemStack: ItemStack?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = Minecraft.getInstance()
                val configDir = client.gameDirectory.resolve("config")

                if (itemStack == null) {
                    sendChat("<color:#FF6961>No item hovered!")
                    return@launch
                }

                val nbtString = yoinkSingleItem(client.player!!, itemStack)
                if (nbtString == null) {
                    sendChat("<color:#FF6961>Failed to parse item NBT!")
                    YoinkGUIClient.logger.error("Error during NBT parsing: NBT string is null")
                    return@launch
                }

                NBTParser.saveSingleItem(nbtString, configDir.path)

            } catch (e: Exception) {
                LatestErrorLog.record(e, "Error during single item NBT parsing")
                YoinkGUIClient.logger.error("Error during single item NBT parsing: ${e.message}", e)
            }
        }
    }

    fun handleParseButton(client: Minecraft) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val player = client.player ?: return@launch
                val configDir = client.gameDirectory.resolve("config").path
                val yoinkInventory = YoinkInventory(player, TopInventory(client))
                // Preserve slot index as Pair<Int, String> for plus slot tracking feature
                val yoinkedItems = yoinkInventory.apply { yoinkItems() }.getYoinkedItems()

                if (yoinkedItems.isEmpty()) {
                    sendChat("<color:#FF6961>Inventory is empty!")
                    return@launch
                }

                NBTParser.saveFormattedNBTToFile(yoinkedItems, configDir)

            } catch (e: Exception) {
                LatestErrorLog.record(e, "Error during NBT parsing")
                YoinkGUIClient.logger.error("Error during NBT parsing: ${e.message}", e)
            }
        }
    }
}
