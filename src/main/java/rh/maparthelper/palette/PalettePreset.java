package rh.maparthelper.palette;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import java.util.*;

public record PalettePreset(String presetName, Map<MapColor, Block> colors) {

    public PalettePreset(String presetName, Map<MapColor, Block> colors) {
        Map<MapColor, Block> sortedContent = new TreeMap<>(Comparator.comparingInt(o -> o.id));
        sortedContent.putAll(colors);
        sortedContent.values().removeIf(b -> b.equals(Blocks.AIR));

        this.presetName = presetName != null ? presetName : "Unnamed";
        this.colors = Collections.unmodifiableMap(sortedContent);
    }

    public Set<MapColor> getMapColors() {
        return colors.keySet();
    }

    public Block getBlockOfMapColor(MapColor mapColor) {
        return colors.get(mapColor);
    }
}
