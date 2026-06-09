package rh.maparthelper.palette;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.adapter.MapColorBlockAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static rh.maparthelper.MapartHelper.CONFIG_PATH;

public class PaletteDataManager {
    public static final Path PRESETS_PATH = CONFIG_PATH.resolve("presets");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
            .registerTypeAdapter(
                    new TypeToken<Map<MapColor, Block>>() {}.getType(), new MapColorBlockAdapter()
            )
            .create();
    private static final PaletteDataManager INSTANCE = new PaletteDataManager();

    private boolean outdatedPalette;
    private @NotNull PalettePresetsHandler presetsHandler = new PalettePresetsHandler();
    private @NotNull CompletePalette completePalette = new CompletePalette();

    public static PaletteDataManager getInstance() {
        return INSTANCE;
    }

    public @NotNull PalettePresetsHandler getPresetsHandler() {
        return presetsHandler;
    }

    public @NotNull CompletePalette getCompletePalette() {
        return completePalette;
    }

    public boolean isPaletteOutdated() {
        return outdatedPalette;
    }

    public void updatePaletteGameVersion(boolean regeneratePalette) {
        if (regeneratePalette) {
            MapartHelper.LOGGER.info("Regenerating palette...");
            completePalette = CompletePalette.generate();
        } else
            completePalette.bumpGameVersion();
        outdatedPalette = false;
        saveCompletePalette();
    }

    public void updatePaletteAndPresets() {
        if (!readCompletePalette()) {
            updatePaletteGameVersion(true);
        }
        readPresetsConfig();
    }

    private void saveCompletePalette() {
        Path completePalettePath = CONFIG_PATH.resolve("complete_palette.json");
        try (FileWriter writer = new FileWriter(completePalettePath.toFile())) {
            GSON.toJson(completePalette, writer);
            MapartHelper.LOGGER.info("The updated palette file was successfully saved");
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to save file \"{}\"", completePalettePath, e);
        }
    }

    /// @return {@code True} if the palette was read successfully, {@code False} if the palette should be regenerated
    public boolean readCompletePalette() {
        Path completePalettePath = CONFIG_PATH.resolve("complete_palette.json");
        if (Files.notExists(completePalettePath)) {
            return false;
        }
        try (FileReader reader = new FileReader(completePalettePath.toFile())) {
            completePalette = GSON.fromJson(reader, CompletePalette.class);
            if (completePalette != null) {
                completePalette.palette.replaceAll((color, blocks) -> blocks.stream()
                        .filter(b -> b != Blocks.AIR)
                        .toList()
                );
                outdatedPalette = !SharedConstants.getCurrentVersion().name().equals(completePalette.getGameVersion());
                return true;
            }
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Failed to read or parse file \"{}\"", completePalettePath, e);
        }
        return false;
    }

    public void changeSelectedPreset(UUID presetUUID) {
        presetsHandler.setSelectedPreset(presetUUID);
        savePresetsConfig();
    }

    private void savePresetsConfig() {
        Path presetsConfigPath = CONFIG_PATH.resolve("palette_presets.json");
        try (FileWriter writer = new FileWriter(presetsConfigPath.toFile())) {
            GSON.toJson(presetsHandler, writer);
            MapartHelper.LOGGER.info("Palette presets config was successfully saved");
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to save file \"{}\"", presetsConfigPath, e);
        }
    }

    public void readPresetsConfig() {
        Path presetsConfigPath = CONFIG_PATH.resolve("palette_presets.json");

        if (Files.notExists(presetsConfigPath)) {
            presetsHandler = new PalettePresetsHandler();
            readPresets();
            validatePresetsConfig();
            savePresetsConfig();
            return;
        }
        try (FileReader reader = new FileReader(presetsConfigPath.toFile())) {
            presetsHandler = GSON.fromJson(reader, PalettePresetsHandler.class);
            if (presetsHandler == null) {
                presetsHandler = new PalettePresetsHandler();
                readPresets();
                validatePresetsConfig();
                savePresetsConfig();
                return;
            }
            if (removeNonexistent() | readPresets() | validatePresetsConfig())
                savePresetsConfig();

        } catch (Exception e) {
            MapartHelper.LOGGER.error("Failed to read and parse file \"{}\"", presetsConfigPath, e);
        }
    }

    private boolean removeNonexistent() {
        return presetsHandler.removeNonexistent();
    }

    private boolean validatePresetsConfig() {
        int configFileChanged = presetsHandler.validateConfigState(completePalette);
        if (configFileChanged == -1) {
            MapartHelper.LOGGER.info("No preset files were found. A default preset will be generated");
            savePresets();
        } else if (configFileChanged == -2) {
            MapartHelper.LOGGER.warn("Selected preset file is no longer exist, switching to another preset");
        }
        return configFileChanged != 0;
    }

    private void savePresets() {
        try {
            if (!Files.exists(PRESETS_PATH)) {
                Files.createDirectory(PRESETS_PATH);
            }
            MapartHelper.LOGGER.info("Saving palette preset files into \"{}\"...", PRESETS_PATH);
            for (RegisteredPalettePreset presetRegistry : presetsHandler.getPresets().values()) {
                Path presetPath = PRESETS_PATH.resolve(presetRegistry.filename());
                try (FileWriter writer = new FileWriter(presetPath.toFile())) {
                    GSON.toJson(presetRegistry.preset(), writer);
                } catch (IOException e) {
                    MapartHelper.LOGGER.error(
                            "Failed to write preset \"{}\"(\"{}\") to the file \"{}\"",
                            presetRegistry.presetName(), presetRegistry.uuid(), presetRegistry, e
                    );
                }
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write presets directory", e);
        }
    }

    private boolean readPresets() {
        Set<String> corruptedPresetFiles = new HashSet<>();
        Map<String, PalettePreset> presetsToAddToConfig = new HashMap<>();

        if (!Files.exists(PRESETS_PATH))
            return false;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PRESETS_PATH, "*.json")) {
            for (Path path : stream) {
                try (FileReader reader = new FileReader(path.toFile())) {
                    PalettePreset preset = GSON.fromJson(reader, PalettePreset.class);
                    if (preset == null || preset.colors() == null) {
                        MapartHelper.LOGGER.warn("JSON file \"{}\" is not a preset, ignoring", path);
                        corruptedPresetFiles.add(path.getFileName().toString());
                        continue;
                    }
                    presetsToAddToConfig.put(path.getFileName().toString(), preset);
                } catch (JsonSyntaxException e) {
                    MapartHelper.LOGGER.error("Failed to read JSON syntax of preset file \"{}\": {}", path, e.getMessage());
                    corruptedPresetFiles.add(path.getFileName().toString());
                } catch (Exception e) {
                    MapartHelper.LOGGER.error("Failed to read or parse preset file \"{}\": {}", path, e.getMessage());
                    corruptedPresetFiles.add(path.getFileName().toString());
                }
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to read presets directory", e);
        }

        boolean configFileChanged = presetsHandler.unregisterPresetsByFilenames(corruptedPresetFiles);
        configFileChanged |= presetsHandler.setPresets(presetsToAddToConfig);

        return configFileChanged;
    }

    /**
     * Applies set of preset changes
     *
     * @param selectedPreset Preset to select after applying patches
     * @param presetPatches  Set of patches containing changes to presets: deletions, creations and content changes
     */
    public void applyPresetPatches(UUID selectedPreset, Collection<RegisteredPresetPatch> presetPatches) {
        MapartHelper.LOGGER.info("Applying changes to palette presets...");
        DataPatchRequest dataPatchRequest = presetsHandler.applyPresetPatches(completePalette, presetPatches);

        int deleted = 0;
        int created = 0;
        int updated = 0;

        for (String filename : dataPatchRequest.filesToRemove) {
            Path presetFilepath = PRESETS_PATH.resolve(filename);
            try {
                Files.delete(presetFilepath);
                deleted++;
            } catch (IOException e) {
                MapartHelper.LOGGER.error("Failed to delete preset file \"{}\"", presetFilepath, e);
            }
        }

        Iterator<RegisteredPalettePreset> presetsToWrite = Stream.concat(
                dataPatchRequest.presetsToCreate.stream(), dataPatchRequest.presetsToUpdate.stream()
        ).iterator();

        while (presetsToWrite.hasNext()) {
            RegisteredPalettePreset preset = presetsToWrite.next();
            Path presetFilepath = PRESETS_PATH.resolve(preset.filename());
            try (FileWriter writer = new FileWriter(presetFilepath.toFile())) {
                GSON.toJson(preset.preset(), writer);
                if (dataPatchRequest.presetsToCreate.contains(preset))
                    created++;
                else
                    updated++;
            } catch (Exception e) {
                MapartHelper.LOGGER.error(
                        "Failed to write preset \"{}\"(\"{}\") to the file \"{}\"",
                        preset.presetName(), preset.uuid(), preset, e
                );
            }
        }

        if (deleted + created > 0 || !selectedPreset.equals(presetsHandler.getSelectedPreset().uuid())) {
            presetsHandler.setSelectedPreset(selectedPreset);
            savePresetsConfig();
        }

        MapartHelper.LOGGER.info(
                "Presets updating finished with: {} deleted; {} created; {} updated",
                deleted, created, updated
        );
    }

    public record DataPatchRequest(
            Set<RegisteredPalettePreset> presetsToCreate,
            Set<RegisteredPalettePreset> presetsToUpdate,
            Set<String> filesToRemove
    ) {}
}
