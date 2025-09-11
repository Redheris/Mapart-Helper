package rh.maparthelper.config.palette;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
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

import static rh.maparthelper.MapartHelper.CONFIG_PATH;

public class PaletteConfigManager {
    final static Path PRESETS_PATH = CONFIG_PATH.resolve("presets");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
            .registerTypeAdapter(new TypeToken<Map<MapColor, Block>>(){}.getType(), new MapColorEntryAdapter())
            .create();

    public static @NotNull PalettePresetsConfig presetsConfig = new PalettePresetsConfig();
    public static @NotNull CompletePalette completePalette = new CompletePalette();

    public static void regenerateCompletePalette() {
        completePalette = CompletePalette.generate();
        saveCompletePalette();
    }

    // JSON file containing complete palette for setting presets in GUI
    public static boolean readCompletePalette() {
        Path completePaletepath = CONFIG_PATH.resolve("complete_palette.json");
        if (!Files.exists(completePaletepath)) {
            return false;
        }
        try (FileReader reader = new FileReader(completePaletepath.toFile())) {
            completePalette = gson.fromJson(reader, CompletePalette.class);
            if (completePalette != null) {
                completePalette.palette.replaceAll((color, blocks) ->
                        blocks.stream()
                                .filter(b -> b != Blocks.AIR)
                                .toList()
                );
                PaletteGenerator.initARGBMapColor(completePalette.palette);
                return true;
            }
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Failed to read JSON syntax \"{}\": {}", completePaletepath, e.getMessage(), e);
        }
        return false;
    }

    public static void updateCompletePalette() {
        boolean validPaletteFile = readCompletePalette();
        if (!validPaletteFile) {
            regenerateCompletePalette();
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
        var it = presetsConfig.presetFiles.keySet().iterator();
        while (it.hasNext()) {
            Path presetPath = PRESETS_PATH.resolve(it.next());
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
            MapartHelper.LOGGER.info("No preset files found, generating a default");
            presetsConfig.currentPresetFile = presetsConfig.createDefaultPreset();
            savePresetFiles();
            hasChanges = true;
        } else if (!presetsConfig.presetFiles.containsKey(presetsConfig.currentPresetFile)) {
            MapartHelper.LOGGER.info("Selected preset file \"{}\" is no longer exist, switching current preset", presetsConfig.currentPresetFile);
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
        if (!Files.exists(PRESETS_PATH))
            return false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PRESETS_PATH, "*.json")) {
            for (Path path : stream) {
                try (FileReader reader = new FileReader(path.toFile())) {
                    MapartHelper.LOGGER.info("Reading JSON file: \"{}\"", path);
                    PalettePresetsConfig.PalettePreset preset = gson.fromJson(reader, PalettePresetsConfig.PalettePreset.class);
                    if (preset == null || preset.colors == null) {
                        MapartHelper.LOGGER.info("JSON file \"{}\" is not a preset, ignoring", path);
                        hasChanges |= presetsConfig.presetFiles.remove(path.getFileName().toString()) != null;
                        continue;
                    }
                    String filename = path.getFileName().toString();
                    presetsConfig.presets.put(filename, preset);
                    if (!presetsConfig.presetFiles.containsKey(filename)) {
                        presetsConfig.presetFiles.put(filename, FilenameUtils.getBaseName(filename));
                        hasChanges = true;
                    }
                    MapartHelper.LOGGER.info("Preset file \"{}\" successfully read", path);
                }
                catch (JsonSyntaxException e) {
                    MapartHelper.LOGGER.error("Failed to read JSON syntax \"{}\": {}", path, e.getMessage());
                    hasChanges |= presetsConfig.presetFiles.remove(path.getFileName().toString()) != null;
                } catch (IOException e) {
                    MapartHelper.LOGGER.error("Failed to read preset \"{}\"", path, e);
                    hasChanges |= presetsConfig.presetFiles.remove(path.getFileName().toString()) != null;
                }
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to read presets directory", e);
        }
        return hasChanges;
    }

    private static void savePresetFiles() {
        try {
            if (!Files.exists(PRESETS_PATH)) {
                Files.createDirectory(PRESETS_PATH);
            }
            for (var entry : presetsConfig.presetFiles.entrySet()) {
                savePresetFile(entry.getKey());
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write presets directory", e);
        }
    }

    public static void savePresetFile(String filename) {
        Path presetFilePath = PRESETS_PATH.resolve(filename);
        try (FileWriter writer = new FileWriter(presetFilePath.toFile())) {
            MapartHelper.LOGGER.info("Saving preset \"{}\" JSON file: \"{}\"", presetsConfig.presetFiles.get(filename), presetFilePath);
            PalettePresetsConfig.PalettePreset preset = presetsConfig.presets.get(filename);
            gson.toJson(preset, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write preset \"{}\" to file \"{}\"", filename, presetFilePath, e);
        }
    }

    public static void deletePresetFile(String filename) {
        try {
            Files.delete(PRESETS_PATH.resolve(filename));
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to delete preset \"{}\"", filename, e);
        }
    }

    public static void changeCurrentPreset(String name) {
        presetsConfig.setCurrentPreset(name);
        savePresetsConfigFile();
    }
}
