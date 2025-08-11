package rh.maparthelper.config.palette;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.adapter.MapColorEntryAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PaletteConfigManager {
    private static final Path PRESETS_PATH = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID).resolve("palette_presets.json");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
            .registerTypeAdapter(new TypeToken<Map<MapColor, Block>>(){}.getType(), new MapColorEntryAdapter())
            .create();

    public static PalettePresetsConfig palettePresetsConfig;
    public static CompletePalette completePalette;

    public static void regenerateCompletePalette() {
        completePalette = new CompletePalette();
        saveCompletePalette();
    }

    // JSON file containing complete palette for setting presets in GUI
    public static void readCompletePalette() {
        Path completePaletepath = PRESETS_PATH.resolveSibling("complete_palette.json");
        if (!Files.exists(completePaletepath)) {
            completePalette = new CompletePalette();
            saveCompletePalette();
            return;
        }
        try (FileReader reader = new FileReader(completePaletepath.toFile())) {
            completePalette = gson.fromJson(reader, CompletePalette.class);
            if (completePalette == null) {
                completePalette = new CompletePalette();
            }
            saveCompletePalette();
        } catch (Exception e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    public static void saveCompletePalette() {
        try (FileWriter writer = new FileWriter(PRESETS_PATH.resolveSibling("complete_palette.json").toFile())) {
            gson.toJson(completePalette, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    // JSON file containing all user presets
    public static void readPresetsFile() {
        if (!Files.exists(PRESETS_PATH)) {
            palettePresetsConfig = new PalettePresetsConfig();
            savePresetsFile();
            return;
        }
        try (FileReader reader = new FileReader(PRESETS_PATH.toFile())) {
            palettePresetsConfig = gson.fromJson(reader, PalettePresetsConfig.class);
            if (palettePresetsConfig == null) {
                palettePresetsConfig = new PalettePresetsConfig();
            }
            palettePresetsConfig.validateConfig();
            savePresetsFile();
        } catch (Exception e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    public static void savePresetsFile() {
        try (FileWriter writer = new FileWriter(PRESETS_PATH.toFile())) {
            gson.toJson(palettePresetsConfig, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    public static void changeCurrentPreset(String name) {
        palettePresetsConfig.changeCurrentPreset(name);
        savePresetsFile();
    }

    public static void createNewPreset() {
        palettePresetsConfig.createNewPreset();
        savePresetsFile();
    }

    public static void renamePreset(String oldName, String newName) {
        palettePresetsConfig.renamePreset(oldName, newName);
        savePresetsFile();
    }

    public static void duplicatePreset(String name) {
        palettePresetsConfig.duplicatePreset(name);
        savePresetsFile();
    }
}
