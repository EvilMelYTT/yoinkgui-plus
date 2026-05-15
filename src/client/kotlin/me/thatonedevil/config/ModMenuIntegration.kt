package me.thatonedevil.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.config.YaclConfigHelper.booleanOption
import me.thatonedevil.config.YaclConfigHelper.enumOptionString
import me.thatonedevil.nbt.ComponentValueRegistry.refreshHandlers
import me.thatonedevil.nbt.FormatOptions
import me.thatonedevil.nbt.LoreRawMode
import me.thatonedevil.nbt.SlotFormat
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { parentScreen ->
        createScreen(parentScreen)
    }

    fun createScreen(parentScreen: Screen?): Screen {
        val screen = YetAnotherConfigLib.createBuilder()
            .save {
                YoinkGuiSettings.saveToFile()
                refreshHandlers()
            }
            .title(Component.nullToEmpty("YoinkGUI+ Settings"))
            .category(ConfigCategory.createBuilder()
                .name(Component.nullToEmpty("Button settings"))
                .tooltip(Component.nullToEmpty("Button settings"))
                .group(OptionGroup.createBuilder()
                    .name(Component.nullToEmpty("Button Options"))
                    .option(booleanOption(
                        name = "Enable Yoink Button",
                        field = yoinkGuiSettings.enableYoinkButton,
                        defaultValue = true
                    ))
                    .option(booleanOption(
                        name = "Enable Single Item Yoink",
                        field = yoinkGuiSettings.enableSingleItemYoink,
                        defaultValue = true,
                        description = "Allows yoinking single items when pressing Y while hovering."
                    ))
                    .build())
                .build())

            .category(ConfigCategory.createBuilder()
                .name(Component.nullToEmpty("Dev settings"))
                .tooltip(Component.nullToEmpty("Developer settings"))
                .group(OptionGroup.createBuilder()
                    .name(Component.nullToEmpty("Dev Options"))
                    .option(booleanOption(
                        name = "Debug Mode",
                        field = yoinkGuiSettings.debugMode,
                        defaultValue = false,
                        description = "Enables debug logging to help diagnose issues."
                    ))
                    .name(Component.nullToEmpty("Toggle formatting option"))
                    .option(enumOptionString(
                        name = "Default NBT Format",
                        field = yoinkGuiSettings.formatOption,
                        enumClass = FormatOptions::class.java,
                        defaultValue = FormatOptions.LEGACY
                    ))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Component.nullToEmpty("NBT Parser Options"))
                    .option(booleanOption(
                        name = "Include Raw NBT",
                        field = yoinkGuiSettings.includeRawNbt,
                        defaultValue = false,
                        description = "Includes the raw NBT data in the parsed output."
                    ))
                    .option(booleanOption(
                        name = "Color parser",
                        field = yoinkGuiSettings.toggleColorParser,
                        defaultValue = true,
                        description = "Toggles color parsing in NBT text. <red>, <blue>, <green>"
                    ))
                    .option(booleanOption(
                        name = "Style parser",
                        field = yoinkGuiSettings.toggleStyleParser,
                        defaultValue = true,
                        description = "Toggles style parsing in NBT text. <bold>, <italic>, <underlined>"
                    ))
                    .option(booleanOption(
                        name = "Shadow parser",
                        field = yoinkGuiSettings.toggleShadowParser,
                        defaultValue = true,
                        description = "Toggles shadow parsing in NBT text. <shadow:#000000:0.5>"
                    ))
                    .option(booleanOption(
                        name = "Gradient parser",
                        field = yoinkGuiSettings.toggleGradientParser,
                        defaultValue = true,
                        description = "Toggles gradient parsing in NBT text. <gradient:#FF0000:#00FF00:#0000FF>"
                    ))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Component.nullToEmpty("Output Options"))
                    .option(booleanOption(
                        name = "Click Opens File",
                        field = yoinkGuiSettings.clickOpensFile,
                        defaultValue = true,
                        description = "When enabled, clicking the file path in chat opens the file directly. When disabled, clicking copies the path to clipboard."
                    ))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Component.nullToEmpty("Lore Raw Output"))
                    .option(booleanOption(
                        name = "Show Raw Lore",
                        field = yoinkGuiSettings.loreShowRaw,
                        defaultValue = true,
                        description = "Outputs raw per-segment color+style strings for lore lines (e.g. #65EFEB&lS#71F2E3&lt...)."
                    ))
                    .option(enumOptionString(
                        name = "Raw Lore Mode",
                        field = yoinkGuiSettings.loreRawMode,
                        enumClass = LoreRawMode::class.java,
                        defaultValue = LoreRawMode.RAW_ONLY
                    ))
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(Component.nullToEmpty("Slot Display Options"))
                    .option(enumOptionString(
                        name = "Slot Header Format",
                        field = yoinkGuiSettings.slotFormat,
                        enumClass = SlotFormat::class.java,
                        defaultValue = SlotFormat.ITEM_AND_SLOT
                    ))
                    .option(booleanOption(
                        name = "Show Slot Labels",
                        field = yoinkGuiSettings.slotShowLabels,
                        defaultValue = true,
                        description = "Shows human-readable labels for named slots (e.g. Slot: 98 (Mainhand)). Unnamed slots always show only their number."
                    ))
                    .build())
                .build())

            .build()
            .generateScreen(parentScreen)
        return screen
    }
}
