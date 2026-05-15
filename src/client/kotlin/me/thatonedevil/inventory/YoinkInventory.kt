package me.thatonedevil.inventory

import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class YoinkInventory(private val player: Player, private val topInventory: TopInventory) {

    private val encodedItems: MutableList<Pair<Int, JsonElement>> = mutableListOf()

    fun yoinkItems() {
        encodedItems.clear()

        val inv = player.inventory
        val ops = player.registryAccess().createSerializationContext(JsonOps.INSTANCE)

        // Main inventory + hotbar: slots 0-35
        for (slot in 0 until 36) {
            saveStack(inv.getItem(slot), slot, ops)
        }

        // Armor: feet=100, legs=101, chest=102, head=103 (inventory slots 36-39)
        for (i in 0 until 4) {
            saveStack(inv.getItem(36 + i), 100 + i, ops)
        }

        // Offhand: inventory slot 40, mapped to 99
        saveStack(inv.getItem(40), 99, ops)

        // Container contents
        topInventory.inventoryItems().forEachIndexed { index, stack ->
            saveStack(stack, index, ops)
        }
    }

    private fun saveStack(stack: ItemStack, slotIndex: Int, ops: com.mojang.serialization.DynamicOps<JsonElement>) {
        if (stack.isEmpty) return
        ItemStack.CODEC.encodeStart(ops, stack).result().ifPresent { json ->
            encodedItems.add(Pair(slotIndex, json))
        }
    }

    fun getYoinkedItems(): List<Pair<Int, String>> {
        return encodedItems.map { (slot, json) -> Pair(slot, json.toString()) }
    }

    companion object {
        fun yoinkSingleItem(player: Player, itemStack: ItemStack): String? {
            val ops = player.registryAccess().createSerializationContext(JsonOps.INSTANCE)
            return ItemStack.CODEC.encodeStart(ops, itemStack).result().orElse(null)?.toString()
        }
      }
    }
