package rh.maparthelper.config.palette;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import org.apache.commons.io.FilenameUtils;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.adapter.MapColorEntryAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PaletteConfigManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID);
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
            .registerTypeAdapter(new TypeToken<Map<MapColor, Block>>(){}.getType(), new MapColorEntryAdapter())
            .create();

    public static PalettePresetsConfig presetsConfig;
    public static CompletePalette completePalette;

    public static void regenerateCompletePalette() {
        completePalette = new CompletePalette();
        saveCompletePalette();
    }

    // JSON file containing complete palette for setting presets in GUI
    public static void readCompletePalette() {
        Path completePaletepath = CONFIG_PATH.resolve("complete_palette.json");
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
        try (FileWriter writer = new FileWriter(CONFIG_PATH.resolve("complete_palette.json").toFile())) {
            gson.toJson(completePalette, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    // JSON file containing all user presets
    public static void readPresetsConfigFile() {
        Path presetsConfigPath = CONFIG_PATH.resolve("palette_presets.json");
        if (!Files.exists(presetsConfigPath)) {
            presetsConfig = PalettePresetsConfig.createDefaultConfig();
            savePresetsConfigFile();
            savePresetFiles();
            return;
        }
        try (FileReader reader = new FileReader(presetsConfigPath.toFile())) {
            presetsConfig = gson.fromJson(reader, PalettePresetsConfig.class);
            if (presetsConfig == null) {
                presetsConfig = PalettePresetsConfig.createDefaultConfig();
                savePresetsConfigFile();
                savePresetFiles();
                return;
            }
            if (removeUnexist() | readPresetsFiles() | validatePresetsConfig())
                savePresetsConfigFile();
        } catch (Exception e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    private static boolean removeUnexist() {
        boolean hasChanges = false;
        Path presetsPath = CONFIG_PATH.resolve("presets");
        var it = presetsConfig.presetFiles.keySet().iterator();
        while (it.hasNext()) {
            Path presetPath = presetsPath.resolve(it.next());
            if (!Files.exists(presetPath)) {
                it.remove();
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    private static boolean validatePresetsConfig() {
        boolean hasChanges = false;
        if (presetsConfig.presetFiles.isEmpty()) {
            presetsConfig.createNewPreset();
            savePresetFiles();
            hasChanges = true;
        } else if (!presetsConfig.presetFiles.containsKey(presetsConfig.currentPresetFile)) {
            presetsConfig.currentPresetFile = presetsConfig.presetFiles.keySet().iterator().next();
            hasChanges = true;
        }
        return hasChanges;
    }

    public static void savePresetsConfigFile() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.resolve("palette_presets.json").toFile())) {
            gson.toJson(presetsConfig, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    private static boolean readPresetsFiles() {
        boolean hasChanges = false;
        Path presetsPath = CONFIG_PATH.resolve("presets");
        if (!Files.exists(presetsPath))
            return false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(presetsPath, "*.json")) {
            for (Path path : stream) {
                try (FileReader reader = new FileReader(path.toFile())) {
                    PalettePresetsConfig.PalettePreset preset = gson.fromJson(reader, PalettePresetsConfig.PalettePreset.class);
                    if (preset == null || preset.colors == null || preset.colors.isEmpty()) {
                        MapartHelper.LOGGER.info("JSON file \"{}\" is not a preset or empty, ignoring", path);
                        continue;
                    }
                    String filename = path.getFileName().toString();
                    if (presetsConfig.presetFiles.containsKey(filename)) {
                        presetsConfig.presets.put(filename, preset);
                    } else {
                        presetsConfig.presetFiles.put(filename, FilenameUtils.getBaseName(filename));
                        presetsConfig.presets.put(filename, preset);
                        hasChanges = true;
                    }
                    MapartHelper.LOGGER.info("Preset file \"{}\" successfully read", path);
                } catch (IOException e) {
                    MapartHelper.LOGGER.error("Failed to read preset \"{}\"", path, e);
                }
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to read presets directory", e);
        }
        return hasChanges;
    }

    private static void savePresetFiles() {
        try {
            Path presetsPath = CONFIG_PATH.resolve("presets");
            if (!Files.exists(presetsPath)) {
                Files.createDirectory(presetsPath);
            }
            for (var entry : presetsConfig.presetFiles.entrySet()) {
                try (FileWriter writer = new FileWriter(presetsPath.resolve(entry.getKey()).toFile())) {
                    PalettePresetsConfig.PalettePreset preset = presetsConfig.presets.get(entry.getKey());
                    gson.toJson(preset, writer);
                } catch (IOException e) {
                    MapartHelper.LOGGER.error("Failed to write preset \"{}\"", entry.getKey(), e);
                }
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write presets directory", e);
        }
    }

    public static void changeCurrentPreset(String name) {
        presetsConfig.changeCurrentPreset(name);
        savePresetsConfigFile();
    }

    public static void updatePreset(String key, PalettePresetsConfig.PalettePreset preset) {
        presetsConfig.updatePreset(key, preset);
        // TODO: replace by saving a single preset instead of every one
        savePresetFiles();
    }

    public static void createNewPreset() {
        presetsConfig.createNewPreset();
        savePresetsConfigFile();
    }

    public static void renamePreset(String oldName, String newName) {
        presetsConfig.renamePreset(oldName, newName);
        savePresetsConfigFile();
    }

    public static void duplicatePreset(String name) {
        presetsConfig.duplicatePreset(name);
        savePresetsConfigFile();
    }
}
