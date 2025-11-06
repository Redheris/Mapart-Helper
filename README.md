# <img src="src/main/resources/assets/maparthelper/icon.png" width="96" alt="icon"> Mapart Helper

> If you run into any bugs or rough edges, please report them on the
> [Issues](https://github.com/Redheris/Mapart-Helper/issues) page — your feedback will be a huge help

---

**Mapart Helper** aims to make working with maparts as convenient and enjoyable as possible.

The mod generates palette directly from the client, giving you a complete list of blocks
available for each specific color.

It also includes other map-related features, such as saving maps from the world as PNG files or
displaying beams at the north-western positions of map areas.

---

## How to:
### 🖼️ Create a Mapart:
1. Launch the game with the mod installed
2. Join a world or server
3. Open the **Mapart editor** screen with the `Y` key *(this can be changed in the setting)*
4. Use the intuitive, user-friendly GUI to create some cool maparts
### 🎨 Use palette presets:
Palette presets let you quickly switch between different sets of blocks and colors used when creating maparts.
You can manage them in the **Presets Editor** screen, accessible from the Mapart Editor.

Each preset is stored as a separate `.json` file inside
`{game root folder}/config/mapart-helper/presets`.

You can:
* **Create and edit presets** directly from the GUI.
* **Add presets manually** by placing `.json` files into the folder — this is also how
you can share your presets with others.

All presets are also listed in the `config/mapart-helper/palette_presets.json` file, which maps preset filenames
to their display names.
You can use this file to quickly identify a specific preset and to safely change its filename.

If you change any of these files or add new presets, make sure to update the data in-game — either by pressing the
“Update from files” button in the **Presets Editor**, or by running `/mart palette update`.

---

## Commands
The following commands can be used everywhere, including servers:
- `/mart` — Shows a list of commands in the chat
- `/mart save frame [filename]` — Saves the map's image you are looking at to the `saved_maps` folder
- `/mart save hand [filename]` — Saves the map's image you are looking at to the `saved_maps` folder
- `/mart save selection [filename]` — Saves the image from the selection area to the `saved_maps` folder
- `/mart beams` — Toggles displaying the beams at the map areas' north-western points
- `/mart palette regenerate` — Regenerates blocks palette
- `/mart palette update` — Updates palette and presets from config files

There are also several miscellaneous singleplayer-only commands, almost for debugging or for some unique cases:
- `/mart-creative palette give-map-palette` — Fills the holding filled map with the image of the complete palette
- `/mart-creative palette place <height>` — Places all blocks from the generated blocks palette into the world
from the current map area's starting coordinates and at the given y-coordinate

---

### Rebane's MapartCraft
As someone who builds maparts in survival, I have always used this great website for that purpose.
So, of course, the main idea for this
mod was inspired by [MapartCraft](https://rebane2001.com/mapartcraft/).
As a novice modder, I decided to create a similar tool, but integrated directly into the game.

Mapart Helper is an alternative that provides
in-game GUI for the same purposes, with some differences
and its own features.

MapartCraft and Mapart Helper use different programming languages,
algorithms, and logic. For this reason, the results <u>may and will differ</u>,
especially when it comes to image preprocessing.
Please keep this in mind if you want to use both tools for the same mapart.

---

## Official links to download the mod
- [Modrinth](https://modrinth.com/project/qRKpqkGI)
- [Curseforge](https://www.curseforge.com/minecraft/mc-mods/mapart-helper)
- [GitHub](https://github.com/Redheris/Mapart-Helper/)

## Contacts
- GitHub: https://github.com/Redheris/Mapart-Helper/issues
- Discord: [Server](https://discord.gg/QTykTF8D5p)
