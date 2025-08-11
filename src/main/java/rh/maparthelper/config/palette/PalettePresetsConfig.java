package rh.maparthelper.config.palette;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;

import java.util.*;

public class PalettePresetsConfig {
    private String currentPreset;
    private Map<String, PalettePreset> presets;

    PalettePresetsConfig() {
        createNewPreset();
    }

    // Returns String array of palettes names for using as lists in game
    public String[] getPaletteNames() {
        return presets.keySet().toArray(String[]::new);
    }

    public Set<MapColor> getCurrentPresetColors() {
        return presets.get(currentPreset).colors.keySet();
    }

    public List<Block> getPresetBlocks(String presetName) {
        return presets.get(presetName).getBlocks();
    }
    public List<Block> getCurrentPresetBlocks() {
        return getPresetBlocks(currentPreset);
    }

    public Block getPresetBlockOfMapColor(String presetName, MapColor color) {
        return presets.get(presetName).getBlockOfMapColor(color);
    }
    public Block getBlockOfMapColor(MapColor color) {
        return getPresetBlockOfMapColor(currentPreset, color);
    }

    // Returns a clone of the preset for editing
    public PalettePreset copyPreset(String name) {
        return new PalettePreset(presets.get(name));
    }

    // Updates preset after in-game editing
    public void updatePreset(String name, PalettePreset preset) {
        presets.replace(name, preset);
    }

    void changeCurrentPreset(String name) {
        currentPreset = name;
    }

    void createNewPreset() {
        if (presets == null)
            presets = new HashMap<>();
        String presetName = "New Preset";
        PalettePreset preset = new PalettePreset(PaletteGenerator.getDefaultPreset());
        presets.put(presetName, preset);
        currentPreset = presetName;
    }

    void duplicatePreset(String name) {
        presets.put(name + " (Copy)", new PalettePreset(presets.get(name)));
    }

    void validateConfig() {
        if (presets.isEmpty()) {
            createNewPreset();
        } else if (!presets.containsKey(currentPreset)) {
            currentPreset = presets.keySet().iterator().next();
        }
    }

    void renamePreset(String oldName, String newName) {
        presets.put(newName, presets.get(oldName));
        presets.remove(oldName);
    }

    public static class PalettePreset {
        public final Map<MapColor, Block> colors = new TreeMap<>(Comparator.comparingInt(o -> o.id));

        private PalettePreset(Map<MapColor, Block> colors) {
            this.colors.putAll(colors);
        }

        public PalettePreset(PalettePreset origin) {
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
