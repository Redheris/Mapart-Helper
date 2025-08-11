package rh.maparthelper.conversion.palette.config;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import rh.maparthelper.conversion.palette.PaletteGenerator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CompletePalette {
    private final Map<Integer, List<Block>> palette = new TreeMap<>();

    CompletePalette() {
        PaletteGenerator.initColors(palette);
    }

    public List<Block> getBlocksOfMapColor(MapColor color) {
        return palette.get(color.id);
    }
}
