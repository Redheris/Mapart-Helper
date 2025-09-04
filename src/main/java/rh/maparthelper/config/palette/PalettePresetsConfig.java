package rh.maparthelper.config.palette;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PalettePresetsConfig {
    String currentPresetFile;
    public Map<String, String> presetFiles = new TreeMap<>();
    transient Map<String, @NotNull PalettePreset> presets = new HashMap<>();

    static PalettePresetsConfig createDefaultConfig() {
        PalettePresetsConfig config = new PalettePresetsConfig();
        config.currentPresetFile = config.createDefaultPreset();
        return config;
    }

    public Editable getEditable() {
        return new Editable(this);
    }

    public String getCurrentPresetFilename() {
        return currentPresetFile;
    }

    public String getCurrentPresetName() {
        return presetFiles.get(currentPresetFile);
    }

    public Set<MapColor> getPresetColors(String filename) {
        return presets.get(filename).colors.keySet();
    }

    public Set<MapColor> getCurrentPresetColors() {
        return getPresetColors(currentPresetFile);
    }

    public Block getPresetBlockOfMapColor(String preset, MapColor color) {
        return presets.get(preset).getBlockOfMapColor(color);
    }

    public Block getBlockOfMapColor(MapColor color) {
        return getPresetBlockOfMapColor(currentPresetFile, color);
    }

    void setCurrentPreset(String presetFilename) {
        if (presetFiles.containsKey(presetFilename))
            this.currentPresetFile = presetFilename;
    }

    void addPreset(String filename, PalettePreset preset) {
        String presetName = Utils.makeUniqueName(presetFiles.values()::contains, "New preset", null, "%s (%d)");
        presetFiles.put(filename, presetName);
        presets.put(filename, preset);
    }

    String createDefaultPreset() {
        Path presetsDir = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID).resolve("presets");
        String presetFilename = Utils.makeUniqueFilename(presetsDir, "new_preset", "json", "%s_%d");
        this.addPreset(presetFilename, PaletteGenerator.getDefaultPreset());
        return presetFilename;
    }

    public static class Editable extends PalettePresetsConfig {
        public Editable(PalettePresetsConfig config) {
            this.currentPresetFile = config.currentPresetFile;
            this.presetFiles = new TreeMap<>(config.presetFiles);
            this.presets = new TreeMap<>(config.presets);
        }

        public void setCurrentPreset(String presetFilename) {
            if (presetFiles.containsKey(presetFilename))
                this.currentPresetFile = presetFilename;
        }

        public PalettePreset getPreset(String presetFilename) {
            return this.presets.get(presetFilename);
        }

        public String createNewPreset(boolean createDefault, Set<String> updatedPresets, Set<String> deletedPresets) {
            Path presetsDir = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID).resolve("presets");
            String presetFilename = Utils.makeUniqueName(filename ->
                    (updatedPresets.contains(filename) || Files.exists(presetsDir.resolve(filename))) && !deletedPresets.contains(filename),
                    "new_preset", "json", "%s_%d"
            );
            PalettePreset preset;
            if (createDefault)
                preset = PaletteGenerator.getDefaultPreset();
            else
                preset = new PalettePreset();
            this.addPreset(presetFilename, preset);
            return presetFilename;
        }

        public Editable deletePreset(String filename, Set<String> updatedPresets, Set<String> deletedPresets) {
            if (presetFiles.size() == 1) {
                Editable newConfig = new Editable(new PalettePresetsConfig());
                newConfig.currentPresetFile = newConfig.createNewPreset(true, updatedPresets, deletedPresets);
                return newConfig;
            }
            this.presetFiles.remove(filename);
            this.presets.remove(filename);
            this.currentPresetFile = presetFiles.keySet().iterator().next();
            return this;
        }

        public String duplicatePreset(String filename, Set<String> updatedPresets, Set<String> deletedPresets) {
            PalettePreset preset = new PalettePreset(presets.get(filename));
            String newFilename = FilenameUtils.getBaseName(filename) + " (Copy)";
            newFilename = Utils.makeUniqueName(presetFiles::containsKey, newFilename, "json", "%s_%d");
            Path presetsDir = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID).resolve("presets");
            newFilename = Utils.makeUniqueName(fName ->
                            (updatedPresets.contains(fName) || Files.exists(presetsDir.resolve(fName))) && !deletedPresets.contains(fName),
                    newFilename, "json", "%s_%d"
            );
            presets.put(newFilename, preset);
            presetFiles.put(newFilename, presetFiles.get(filename) + " (Copy)");
            return newFilename;
        }
    }

    public static class PalettePreset {
        public final Map<MapColor, Block> colors;

        {
            this.colors = new TreeMap<>(Comparator.comparingInt(o -> o.id));
        }

        PalettePreset() {
        }

        PalettePreset(Map<MapColor, Block> colors) {
            this.colors.putAll(colors);
        }

        PalettePreset(PalettePreset origin) {
            this.colors.putAll(origin.colors);
        }

        Block getBlockOfMapColor(MapColor color) {
            return colors.get(color);
        }

        public void updateColor(MapColor mapColor, Block block) {
            this.colors.put(mapColor, block);
        }

        public void removeColor(MapColor mapColor) {
            this.colors.remove(mapColor);
        }
    }
}
