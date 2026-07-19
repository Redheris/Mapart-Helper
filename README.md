# <img src="src/main/resources/assets/maparthelper/icon.png" width="96" alt="icon"> Mapart Helper

> If you run into any bugs or rough edges, please report them on the
> [Issues](https://github.com/Redheris/Mapart-Helper/issues) page — your feedback will be a huge help

---

**Mapart Helper** aims to make working with maparts as convenient and enjoyable as possible.

The mod generates blocks palette directly from the client, giving you a complete list of blocks
available for each specific color. The mod has some default filters to exclude most of the unsuitable or problematic
blocks, but it can be configured in the config screen.

It also includes other map-related features, such as saving maps from the world as PNG files or
displaying beams at the north-western positions of map areas.

<details>
<summary>Supported versions</summary>

| Version     | Status                   |
|-------------|--------------------------|
| 1.21.8      | ✔️ Active                |
| ~~1.21.10~~ | ⚠️ Last version `0.12.0` |
| 1.21.11     | ✔️ Active                |
| 26.1        | ✔️ Active                |
| 26.2        | ✔️ Active                |
| 1.21.4-5    | ✔️ Backport* `0.13.1`    |

*Backport versions won't be getting every new update.
They will be updated only for some major versions (with a delay) and critical fixes

</details>

---

## How to:
### Create a Mapart:
1. Launch the game with the mod installed
2. Join a world or server
3. Open the **Mapart editor** screen with the `Y` key *(this can be changed in the "Miscellaneous" key binds settings)*
4. Use the user-friendly GUI to create some cool maparts
5. Save your mapart as NBT file(s)
6. Use other tools (such as Litematica) to build or place your NBT in the world
7. After you correctly placed the schematic and built it, you can use map item to get the image

(If you haven't built maparts yet, I strongly recommend to watch some video guides about maparts)

![Showcase of the Mapart Editor screen](https://raw.githubusercontent.com/Redheris/Repo-for-readme-assets/refs/heads/main/Mapart-Helper/Mapart-Editor.gif)

### Edit Mapart image:
After you completed image preprocessing, the image can still have some dirty places.
The best solution to fix them is to use any image editor.
And Mapart Helper provides such an editor - **Mapart Painter**. You can open it from the Mapart Editor screen.

**Mapart Painter** is a simple image editor to edit your image or to paint a completely new one. It has a fairly intuitive
user interface and doesn't have too complicated features.

![Mapart-Painter.gif](https://raw.githubusercontent.com/Redheris/Repo-for-readme-assets/refs/heads/main/Mapart-Helper/Mapart-Painter.gif)

### Use palette presets:
Palette presets let you quickly switch between different sets of blocks and colors used to create a mapart.
You can manage them in the **Presets Editor** screen, accessible from the Mapart Editor screen.

Each preset is stored as a separate `.json` file, and they are very easy to transfer: just add a new preset file into
presets folder and update presets in the game. You can find "Open presets folder" and "Update presets list from files"
in the Presets Editor screen.

![Presets-Editor.png](https://raw.githubusercontent.com/Redheris/Repo-for-readme-assets/refs/heads/main/Mapart-Helper/Presets-Editor.png)

### Save maps from the world as PNG:
Mapart Helper provides `/mart save` commands to save an image from:
- a map you are holding;
- a map from the item frame you are looking at;
- a map area to combine multiple maps into one image.

After you use it, you will get a clickable message in the chat, and the image will be saved to the `saved_maps` folder.

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

There are also a few miscellaneous singleplayer-only commands for some unique cases:
- `/mart-creative palette give-map-palette` — Gives you the map with every available map color
- `/mart-creative palette place <height>` — Places all blocks from the generated blocks palette into the world
  from the current map area's starting coordinates and at the given y-coordinate

---

## Official links to download the mod
- [Modrinth](https://modrinth.com/project/qRKpqkGI)
- [Curseforge](https://www.curseforge.com/minecraft/mc-mods/mapart-helper)
- [GitHub](https://github.com/Redheris/Mapart-Helper/)

## Contacts
- GitHub: https://github.com/Redheris/Mapart-Helper/issues
- Discord: [Server](https://discord.gg/QTykTF8D5p)

---

### Rebane's MapartCraft
As someone who builds maparts in survival, I have always used this great website for that purpose.
So, of course, the main idea for this
mod was inspired by [MapartCraft](https://rebane2001.com/mapartcraft/).
As a novice modder, I decided to create a similar tool, but integrated directly into the game.

Mapart Helper is an alternative that provides in-game GUI for the same purposes, with some differences
and its own features.

MapartCraft and Mapart Helper use different programming languages,
algorithms, and logic. For this reason, the results <u>may and will differ</u>.
Please keep this in mind if you want to use both tools for the same mapart.