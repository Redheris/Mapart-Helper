package rh.maparthelper.config.palette;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import rh.maparthelper.util.Utils;

import java.util.*;

public class PalettePresetsConfig {
    String currentPresetFile;
    public Map<String, String> presetFiles = new HashMap<>();
    transient Map<String, PalettePreset> presets = new HashMap<>();

    static PalettePresetsConfig createDefaultConfig() {
        PalettePresetsConfig config = new PalettePresetsConfig();
        config.currentPresetFile = config.createNewPreset();
        return config;
    }

    public PalettePresetsConfig copyConfig() {
        PalettePresetsConfig clone = new PalettePresetsConfig();
        clone.currentPresetFile = this.currentPresetFile;
        clone.presetFiles = new HashMap<>(this.presetFiles);
        clone.presets = new HashMap<>(this.presets);
        return clone;
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

    String createNewPreset() {
        String presetName = Utils.makeUniqueFilename(presetFiles::containsKey, "new_preset", "json", "%s_%d");
        PalettePreset preset = PaletteGenerator.getDefaultPreset();
        presetFiles.put(presetName, "New Preset");
        presets.put(presetName, preset);
        return presetName;
    }

    public static class Editable extends PalettePresetsConfig {
        public Editable(PalettePresetsConfig config) {
            this.currentPresetFile = config.currentPresetFile;
            this.presetFiles = new HashMap<>(config.presetFiles);
            this.presets = new HashMap<>(config.presets);
        }

        public void setCurrentPreset(String presetFilename) {
            if (presetFiles.containsKey(presetFilename))
                this.currentPresetFile = presetFilename;
        }

        public PalettePreset getPreset(String presetFilename) {
            return this.presets.get(presetFilename);
        }

        public String createNewPreset() {
            return super.createNewPreset();
        }

        public Editable deletePreset(String filename) {
            if (presetFiles.size() == 1) {
                return new Editable(createDefaultConfig());
            }
            PalettePresetsConfig newConfig = this.copyConfig();
            newConfig.presetFiles.remove(filename);
            newConfig.presets.remove(filename);
            newConfig.currentPresetFile = presetFiles.keySet().iterator().next();
            return new Editable(newConfig);
        }

        public String duplicatePreset(String filename) {
            PalettePreset preset = new PalettePreset(presets.get(filename));
            String newFilename = filename + " (Copy).json";
            presets.put(newFilename, preset);
            presetFiles.put(newFilename, presetFiles.get(filename) + " (Copy)");
            return newFilename;
        }
    }

    public static class PalettePreset {
        public final Map<MapColor, Block> colors;

        PalettePreset() {
            this.colors = new TreeMap<>(Comparator.comparingInt(o -> o.id));
        }

        PalettePreset(Map<MapColor, Block> colors) {
            this();
            this.colors.putAll(colors);
        }

        PalettePreset(PalettePreset origin) {
            this();
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
