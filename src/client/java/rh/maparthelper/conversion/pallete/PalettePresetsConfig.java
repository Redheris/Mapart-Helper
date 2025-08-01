package rh.maparthelper.conversion.pallete;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import rh.maparthelper.conversion.BlocksPalette;

import java.util.*;

public class PalettePresetsConfig {
    private int currentPreset;
    private List<PalettePreset> presets;

    // Returns String array of palettes names for using as lists in game
    public String[] getPaletteNames() {
        return presets.stream().map(p -> p.name).toArray(String[]::new);
    }

    public PalettePreset getPresetById(int id) {
        for (PalettePreset preset : presets) {
            if (preset.id == id)
                return preset;
        }
        return null;
    }

    public PalettePreset getCurrentPreset() {
        return getPresetById(currentPreset);
    }

    public void createNewPreset() {
        if (presets == null)
            presets = new ArrayList<>();
        Map<MapColor, Block> defaultPalette = BlocksPalette.getDefaultPalette();
        int id = presets.size();
        PalettePreset preset = new PalettePreset("New Preset", id, defaultPalette);
        presets.add(preset);
        currentPreset = id;
    }

    public static class PalettePreset {
        private String name;
        private final int id;
        private final Map<MapColor, Block> colors;

        private PalettePreset(String name, int id, Map<MapColor, Block> colors) {
            this.name = name;
            this.id = id;
            this.colors = colors;
        }

        public List<Block> getBlocks() {
            return new ArrayList<>(colors.values().stream().toList());
        }

        public Block getBlockByMapColor(MapColor color) {
            return colors.get(color);
        }

        public void changeEntry(MapColor color, Block block) {
            colors.put(color, block);
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
