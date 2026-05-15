package me.thatonedevil.keybinds

import me.thatonedevil.YoinkGUIClient.keybindCategory
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper

interface Key {
    fun keyName(): String
    fun key(): Int
    fun whenPressed()
    fun keyType(): InputConstants.Type = InputConstants.Type.KEYSYM

    fun registerScreenEvents() {}

    fun register(): KeyMapping {
        val keyBinding = KeyMapping(keyName(), keyType(), key(), keybindCategory)
        return KeyBindingHelper.registerKeyBinding(keyBinding)
    }
}
