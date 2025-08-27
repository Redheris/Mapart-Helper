package rh.maparthelper.config.palette;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;

import java.util.*;

public class PalettePresetsConfig {
    String currentPresetFile;
    public Map<String, String> presetFiles = new HashMap<>();
    transient Map<String, PalettePreset> presets = new HashMap<>();

    static PalettePresetsConfig createDefaultConfig() {
        PalettePresetsConfig config = new PalettePresetsConfig();
        config.createNewPreset();
        return config;
    }

    public String getCurrentPresetFilename() {
        return currentPresetFile;
    }

    public String getCurrentPresetName() {
        return presetFiles.get(currentPresetFile);
    }

    public Set<String> getPresetKeys() {
        return presetFiles.keySet();
    }

    public Set<MapColor> getPresetColors(String filename) {
        return presets.get(filename).colors.keySet();
    }

    public Set<MapColor> getCurrentPresetColors() {
        return getPresetColors(currentPresetFile);
    }

    public List<Block> getPresetBlocks(String preset) {
        return presets.get(preset).getBlocks();
    }

    public List<Block> getCurrentPresetBlocks() {
        return getPresetBlocks(currentPresetFile);
    }

    public Block getPresetBlockOfMapColor(String preset, MapColor color) {
        return presets.get(preset).getBlockOfMapColor(color);
    }

    public Block getBlockOfMapColor(MapColor color) {
        return getPresetBlockOfMapColor(currentPresetFile, color);
    }

    // Returns a clone of the preset for editing
    public PalettePreset copyPreset(String key) {
        return new PalettePreset(presets.get(key));
    }

    // Updates preset after in-game editing
    void updatePreset(String key, PalettePreset preset) {
        presets.replace(key, preset);
    }

    void changeCurrentPreset(String key) {
        currentPresetFile = key;
    }

    void createNewPreset() {
        String presetName = "new_preset.json";
        PalettePreset preset = PaletteGenerator.getDefaultPreset();
        presetFiles.put(presetName, "New Preset");
        presets.put(presetName, preset);
        currentPresetFile = presetName;
    }

    void deletePreset(String filename) {
        presetFiles.remove(filename);
        presets.remove(filename);
        currentPresetFile = presetFiles.keySet().iterator().next();
    }

    String duplicatePreset(String filename) {
        PalettePreset preset = new PalettePreset(presets.get(filename));
        String newFilename = presetFiles.get(filename) + " (Copy).json";
        presets.put(newFilename, preset);
        presetFiles.put(newFilename, presetFiles.get(filename) + " (Copy)");
        currentPresetFile = newFilename;
        return newFilename;
    }

    void renamePreset(String filename, String newName) {
        presetFiles.replace(filename, newName);
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

        List<Block> getBlocks() {
            return new ArrayList<>(colors.values().stream().toList());
        }

        Block getBlockOfMapColor(MapColor color) {
            return colors.get(color);
        }
    }
}
