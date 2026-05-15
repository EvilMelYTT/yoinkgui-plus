package me.thatonedevil

import me.thatonedevil.commands.YoinkGuiClientCommandRegistry
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.handlers.ParseButtonHandler
import me.thatonedevil.keybinds.KeybindManager
import me.thatonedevil.utils.api.UpdateChecker
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.KeyMapping
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//? if >=1.21.4 {
import net.minecraft.resources.Identifier
//?} else {
/*import net.minecraft.resources.ResourceLocation
*/
//?}

object YoinkGUIClient : ClientModInitializer {
    val logger: Logger = LoggerFactory.getLogger(BuildConfig.MOD_ID)

    @JvmStatic
    val yoinkGuiSettings: YoinkGuiSettings = YoinkGuiSettings

    //? if >=1.21.4 {
    val keybindCategory: KeyMapping.Category = KeyMapping.Category.register(Identifier.parse("keybinds"))
    //?} else {
    /*val keybindCategory: KeyMapping.Category = KeyMapping.Category.register(ResourceLocation.parse("keybinds"))
    */
    //?}

    override fun onInitializeClient() {
        UpdateChecker.setupJoinListener()
        YoinkGuiClientCommandRegistry.register()
        KeybindManager().register()
        yoinkGuiSettings
        ParseButtonHandler.register()
    }
}
