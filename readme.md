<div align="center">

Built with `.\gradlew stonecutterSwitchTo1.21.11` and `.\gradlew :1.21.11:build --no-daemon`

<img src="https://i.imgur.com/VnSg7iz.png" width="300"/>

*Art creds to @notfakesusan on Discord*

[![Download on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/yoinkgui)
[![Download on CurseForge](https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/available/curseforge_vector.svg)](https://www.curseforge.com/minecraft/mc-mods/yoinkgui)
[![fapi-badge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg)](https://modrinth.com/mod/fabric-api)
[![kotlin-badge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-language-kotlin_vector.svg)](https://modrinth.com/mod/fabric-language-kotlin)
[![modmenu-badge](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/requires/mod-menu_vector.svg)](https://modrinth.com/mod/modmenu)
[![sinytra-connector](https://raw.githubusercontent.com/Sinytra/.github/refs/heads/main/badges/connector/cozy.svg)](https://modrinth.com/mod/connector)

[![Build Status](https://github.com/ThatOneDevil/yoinkgui/actions/workflows/build.yml/badge.svg)](https://github.com/ThatOneDevil/yoinkgui)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/yoinkgui?color=00AF5C&label=downloads&logo=modrinth)](https://modrinth.com/mod/yoinkgui)
[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1323988_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/yoinkgui)
[![Discord](https://img.shields.io/discord/1405856687851704420?color=blue&logo=discord&label=Discord)](https://discord.gg/kcegGvZvpC)

**A Fabric mod that allows users to easily copy the name and lore of items from any GUI**  
Intended strictly for convenience and development purposes.

This is an unofficial port and extension of YoinkGUI to 1.21.11, based on the 26.1 and 1.21.9 sources, with additional features added on top.

</div>

---

> [!CAUTION]
> If you encounter any bugs or issues:
> 1. Use the command `/yoinkguiclient debug`
> 2. Click the message to copy the error details
> 3. Submit an issue on GitHub: [https://github.com/ThatOneDevil/yoinkgui/issues](https://github.com/ThatOneDevil/yoinkgui/issues)

---

> [!IMPORTANT]
> Make sure you have all required dependencies installed:
> - **Fabric API**
> - **Fabric Language Kotlin**
> - **Yet Another Config Lib**
> - **ModMenu**

---

> [!NOTE]
> - Press **M** to open the configuration menu and customize your parsing options
>
> - **Full Minecraft formatting code support**:
>    - **Color codes, &c &e**
>    - **Bold**, *Italic*, __Underline__, ~~Strikethrough~~, and obfuscated text
>
> - **Hex and gradient support** (MiniMessage format):
>    - Hex colors: `<color:#FF00AA>`
>    - Gradients: `<gradient:#8968CD:#FFA6CA>`
>    - Shadow: `<shadow:hex:alpha>`

---

> [!TIP]
> 1. Install the mod with the correct dependencies.
> 2. Open any GUI in-game.
> 3. Click the **"Yoink and Parse NBT into file"** button in the top-left corner.
> 4. The output `.txt` file will appear in your `config` folder.
> 5. The file path is also displayed in chat, as shown below.

---

## What's New in Plus

### `LoreRawMode.kt` + `SlotFormat.kt` + `SlotLabel.kt`
New enums powering plus-exclusive output options. Controls lore output mode (formatted, raw JSON, or both), slot header format (item name, slot number, or both), and named labels for special slots like Mainhand and Offhand.

### `NBTParser.kt`
Extended to support all new settings. Lore can now output as raw JSON alongside or instead of formatted text. Item headers now show the slot they came from with optional named labels.

### `YoinkGuiSettings.kt`
Adds settings for `slotFormat`, `slotShowLabels`, `loreShowRaw`, `loreRawMode`, and `clickOpensFile`.

### `ModMenuIntegration.kt`
Config screen updated with Lore Raw Output, Slot Display Options, and Output Options groups.

### `YoinkInventory.kt`
Slot index is now tracked alongside each item's NBT throughout the parsing pipeline.
