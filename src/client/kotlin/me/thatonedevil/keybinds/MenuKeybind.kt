package me.thatonedevil.keybinds

import me.thatonedevil.screen.ButtonPositionScreen
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

class MenuKeybind : Key {

    override fun keyName(): String = "key.yoinkgui-plus.position"

    override fun key(): Int = GLFW.GLFW_KEY_M

    override fun whenPressed() {
        val client = Minecraft.getInstance()
        client.setScreen(ButtonPositionScreen(client.screen))
    }
}
