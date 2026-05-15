package me.thatonedevil.nbt

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.BuildConfig
import me.thatonedevil.YoinkGUIClient
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils
import me.thatonedevil.utils.Utils.toClickCopy
import me.thatonedevil.utils.Utils.toClickOpenFile
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.utils.api.UpdateChecker
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NBTParser {

    private val gson = Gson()
    private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH-mm-ss")

    private const val MINECRAFT_CUSTOM_NAME = "minecraft:custom_name"
    private const val MINECRAFT_ITEM_NAME = "minecraft:item_name"
    private const val MINECRAFT_LORE = "minecraft:lore"
    private const val COMPONENTS_KEY = "components"

    private val LEGACY_COLOR_CODES = mapOf(
        "black" to "&0", "dark_blue" to "&1", "dark_green" to "&2", "dark_aqua" to "&3",
        "dark_red" to "&4", "dark_purple" to "&5", "gold" to "&6", "gray" to "&7",
        "dark_gray" to "&8", "blue" to "&9", "green" to "&a", "aqua" to "&b",
        "red" to "&c", "light_purple" to "&d", "yellow" to "&e", "white" to "&f"
    )

    private data class ParsedItem(
        val raw: String,
        val formatted: String,
        val slotIndex: Int?
    )

    // -------------------------------------------------------------------------
    // Formatted text parsing
    // -------------------------------------------------------------------------

    private fun parseTextComponent(obj: JsonObject): String = buildString {
        val result = ComponentValueRegistry.process(obj)
        append(result.text)

        if (result.stopPropagation) return@buildString

        obj.get("extra")?.asJsonArray?.forEach { element ->
            if (element.isJsonObject) {
                append(parseTextComponent(element.asJsonObject))
            }
        }
    }

    private fun parseJsonStringAsTextComponent(jsonString: String?): String {
        if (jsonString == null) return ""

        return try {
            val element = gson.fromJson(jsonString, com.google.gson.JsonElement::class.java)

            when {
                element == null -> ""
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> parseTextComponent(element.asJsonObject)
                else -> jsonString
            }
        } catch (e: JsonSyntaxException) {
            LatestErrorLog.record(e, "Failed to parse JSON string as text component")
            YoinkGUIClient.logger.debug("Failed to parse JSON string as text component: $jsonString", e)
            jsonString
        }
    }

    // -------------------------------------------------------------------------
    // Raw lore extraction
    // -------------------------------------------------------------------------

    private fun segmentToRaw(seg: JsonObject): String {
        val text = seg.get("text")?.asString ?: return ""
        if (text.isEmpty()) return ""

        val colorPart: String = seg.get("color")?.asString?.let { color ->
            when {
                color.startsWith("#") -> color.uppercase()
                else -> LEGACY_COLOR_CODES[color.lowercase()] ?: ""
            }
        } ?: ""

        val stylePart = buildString {
            if (ComponentValueRegistry.getBooleanValue(seg.get("bold")))          append("&l")
            if (ComponentValueRegistry.getBooleanValue(seg.get("italic")))        append("&o")
            if (ComponentValueRegistry.getBooleanValue(seg.get("underlined")))    append("&n")
            if (ComponentValueRegistry.getBooleanValue(seg.get("strikethrough"))) append("&m")
            if (ComponentValueRegistry.getBooleanValue(seg.get("obfuscated")))    append("&k")
        }

        return if (colorPart.isEmpty() && stylePart.isEmpty()) text
        else "$colorPart$stylePart$text"
    }

    private fun extractRawLoreLine(lineElement: com.google.gson.JsonElement): String? {
        if (lineElement.isJsonPrimitive) {
            val s = lineElement.asString
            return if (s.isBlank()) null else s
        }
        if (!lineElement.isJsonObject) return null
        val raw = lineElement.toString()
        return if (raw.isBlank()) null else raw
    }

    // -------------------------------------------------------------------------
    // Item extraction
    // -------------------------------------------------------------------------

    private fun extractItemName(components: JsonObject): String? {
        val nameElement = components.get(MINECRAFT_CUSTOM_NAME)
            ?: components.get(MINECRAFT_ITEM_NAME)
            ?: return "Unknown"

        return when {
            nameElement.isJsonObject -> parseTextComponent(nameElement.asJsonObject)
            nameElement.isJsonPrimitive -> parseJsonStringAsTextComponent(nameElement.asString)
            else -> "Unknown format"
        }
    }

    private fun extractItemLore(
        components: JsonObject,
        showRaw: Boolean,
        rawMode: LoreRawMode
    ): String? {
        val loreArray = components.getAsJsonArray(MINECRAFT_LORE) ?: return null

        return buildString {
            append("Lore:\n")
            loreArray.forEachIndexed { index, line ->
                val formatted = when {
                    line.isJsonPrimitive && line.asString.isBlank() -> ""
                    line.isJsonObject -> parseTextComponent(line.asJsonObject)
                    line.isJsonPrimitive -> parseJsonStringAsTextComponent(line.asString)
                    else -> ""
                }
                val raw: String? = if (showRaw) extractRawLoreLine(line) else null

                when {
                    showRaw && rawMode == LoreRawMode.RAW_ONLY -> {
                        append("Line $index: ${raw ?: formatted}\n")
                    }
                    showRaw && rawMode == LoreRawMode.BOTH -> {
                        append("Line $index: $formatted\n")
                        if (raw != null) append("Raw  $index: $raw\n")
                    }
                    else -> {
                        append("Line $index: $formatted\n")
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // NBT format parsing
    // -------------------------------------------------------------------------

    private fun parseNewNBTFormat(raw: String, showRaw: Boolean, rawMode: LoreRawMode): String? {
        return try {
            val element = gson.fromJson(raw, com.google.gson.JsonElement::class.java)
            if (!element.isJsonObject) return null
            val json = element.asJsonObject
            val components = json.getAsJsonObject(COMPONENTS_KEY) ?: return null

            val hasName = components.has(MINECRAFT_CUSTOM_NAME) || components.has(MINECRAFT_ITEM_NAME)
            val hasLore = components.has(MINECRAFT_LORE)

            if (!hasName && !hasLore) return null

            buildString {
                extractItemName(components)?.let { append("Name: $it\n\n") }
                extractItemLore(components, showRaw, rawMode)?.let { append(it) }
            }
        } catch (e: JsonSyntaxException) {
            LatestErrorLog.record(e, "Failed to parse NBT format")
            YoinkGUIClient.logger.debug("Failed to parse NBT format: $raw", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // Item list parsing
    // -------------------------------------------------------------------------

    private fun parseItems(rawItems: List<Pair<Int, String>>, showRaw: Boolean, rawMode: LoreRawMode): List<ParsedItem> {
        return rawItems.mapNotNull { (slotIndex, raw) ->
            parseNewNBTFormat(raw, showRaw, rawMode)?.let { ParsedItem(raw, it, slotIndex) }
        }
    }

    private fun parseSingleItem(raw: String, showRaw: Boolean, rawMode: LoreRawMode): ParsedItem? {
        return parseNewNBTFormat(raw, showRaw, rawMode)?.let { ParsedItem(raw, it, null) }
    }

    // -------------------------------------------------------------------------
    // File writing
    // -------------------------------------------------------------------------

    private fun writeFileHeader(writer: BufferedWriter, formattedTime: String, itemCount: Int, totalCount: Int) {
        writer.write("=== Formatted NBT Data ===\n")
        writer.write("Generated: $formattedTime\n")
        writer.write("Items with content: $itemCount / $totalCount\n\n")
        writer.write("=== Details ===\n")
        writer.write("Mod Version: ${BuildConfig.VERSION}\n")
        writer.write("Minecraft Version: ${BuildConfig.MC_VERSION}\n\n")
    }

    private fun buildItemHeader(itemNumber: Int, slotIndex: Int?, slotFormat: SlotFormat, showLabels: Boolean): String {
        if (slotIndex == null || slotIndex == -1) return "=== ITEM $itemNumber ===\n"
        val slotStr = SlotLabel.format(slotIndex, showLabels)
        return when (slotFormat) {
            SlotFormat.ITEM_AND_SLOT    -> "=== ITEM $itemNumber ===\nSlot: $slotStr\n"
            SlotFormat.SLOT_ONLY        -> "=== SLOT $slotStr ===\n"
            SlotFormat.ITEM_SLOT_HEADER -> "=== ITEM $itemNumber / SLOT $slotStr ===\n"
            SlotFormat.ITEM_ONLY        -> "=== ITEM $itemNumber ===\n"
        }
    }

    private fun writeItem(
        writer: BufferedWriter,
        index: Int,
        item: ParsedItem,
        includeRawNbt: Boolean,
        isLastItem: Boolean,
        slotFormat: SlotFormat,
        showLabels: Boolean
    ) {
        writer.write(buildItemHeader(index + 1, item.slotIndex, slotFormat, showLabels))
        if (includeRawNbt) writer.write("Raw NBT: ${item.raw}\n")
        writer.write("\n${item.formatted}\n")
        if (!isLastItem) writer.write("\n${"=".repeat(50)}\n\n")
    }

    private fun ensureDirectoryExists(dirPath: String): File {
        return File(dirPath).apply {
            if (!exists() && !mkdirs()) throw IOException("Failed to create directory: $absolutePath")
        }
    }

    private fun generateFilename(formattedTime: String): String = "${UpdateChecker.serverName}-${formattedTime}.txt"

    private fun sendSuccessMessage(file: File, duration: Long) {
        val clickOpensFile = YoinkGuiSettings.clickOpensFile.get()
        val pathDisplay = file.relativeTo(File(".").canonicalFile).path
        val clickable = try {
            if (clickOpensFile) {
                "  <color:#8968CD>$pathDisplay &7&o(Click to open)\n".toClickOpenFile(file.canonicalPath)
            } else {
                "  <color:#8968CD>${file.canonicalPath} &7&o(Click to copy)\n".toClickCopy(file.canonicalPath)
            }
        } catch (e: Exception) {
            "  <color:#8968CD>${file.canonicalPath} &7&o(Click to copy)\n".toClickCopy(file.canonicalPath)
        }
        Utils.sendChat(
            "\n<color:#FFA6CA>Formatted NBT data saved to:".toComponent(),
            " <color:#FFA6CA>Parse time: <color:#8968CD>${duration}ms".toComponent(),
            clickable
        )
    }

    private fun resolveSlotFormat(): SlotFormat {
        return try { SlotFormat.valueOf(YoinkGuiSettings.slotFormat.get()) }
        catch (_: Exception) { SlotFormat.ITEM_AND_SLOT }
    }

    private fun resolveLoreRawMode(): LoreRawMode {
        return try { LoreRawMode.valueOf(YoinkGuiSettings.loreRawMode.get()) }
        catch (_: Exception) { LoreRawMode.RAW_ONLY }
    }

    // -------------------------------------------------------------------------
    // Save entry points
    // -------------------------------------------------------------------------

    private suspend fun saveNbtFile(
        configDir: String,
        rawItems: List<Pair<Int, String>>
    ): Result<File> = withContext(Dispatchers.IO) {
        val start = LocalDateTime.now()
        val formattedTime = start.format(timeFormatter)

        try {
            val yoinkDir = ensureDirectoryExists(configDir)
            val file = File(yoinkDir, generateFilename(formattedTime))

            val showRaw = YoinkGuiSettings.loreShowRaw.get()
            val rawMode = resolveLoreRawMode()
            val items = parseItems(rawItems, showRaw, rawMode)
            val slotFormat = resolveSlotFormat()
            val showLabels = YoinkGuiSettings.slotShowLabels.get()

            file.bufferedWriter().use { writer ->
                writeFileHeader(writer, formattedTime, items.size, rawItems.size)
                items.forEachIndexed { index, item ->
                    writeItem(
                        writer = writer,
                        index = index,
                        item = item,
                        includeRawNbt = YoinkGuiSettings.includeRawNbt.get(),
                        isLastItem = index == items.lastIndex,
                        slotFormat = slotFormat,
                        showLabels = showLabels
                    )
                }
            }

            val duration = Duration.between(start, LocalDateTime.now()).toMillis()
            sendSuccessMessage(file, duration)
            Result.success(file)
        } catch (e: Exception) {
            LatestErrorLog.record(e, "Error saving NBT file to $configDir")
            YoinkGUIClient.logger.error("Error saving NBT file: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun saveSingleNbtFile(
        configDir: String,
        rawNbt: String
    ): Result<File> = withContext(Dispatchers.IO) {
        val start = LocalDateTime.now()
        val formattedTime = start.format(timeFormatter)

        try {
            val yoinkDir = ensureDirectoryExists(configDir)
            val file = File(yoinkDir, generateFilename(formattedTime))

            val showRaw = YoinkGuiSettings.loreShowRaw.get()
            val rawMode = resolveLoreRawMode()
            val item = parseSingleItem(rawNbt, showRaw, rawMode)
            val slotFormat = resolveSlotFormat()
            val showLabels = YoinkGuiSettings.slotShowLabels.get()

            file.bufferedWriter().use { writer ->
                writeFileHeader(writer, formattedTime, if (item != null) 1 else 0, 1)
                if (item != null) {
                    writeItem(
                        writer = writer,
                        index = 0,
                        item = item,
                        includeRawNbt = YoinkGuiSettings.includeRawNbt.get(),
                        isLastItem = true,
                        slotFormat = slotFormat,
                        showLabels = showLabels
                    )
                }
            }

            val duration = Duration.between(start, LocalDateTime.now()).toMillis()
            sendSuccessMessage(file, duration)
            Result.success(file)
        } catch (e: Exception) {
            LatestErrorLog.record(e, "Error saving NBT file to $configDir")
            YoinkGUIClient.logger.error("Error saving NBT file: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveFormattedNBTToFile(
        nbtList: List<Pair<Int, String>>,
        configDir: String
    ): Result<File> = saveNbtFile("$configDir/yoinkgui", nbtList)

    suspend fun saveSingleItem(
        rawNbt: String,
        configDir: String
    ): Result<File> = saveSingleNbtFile("$configDir/yoinkgui/items", rawNbt)
}
