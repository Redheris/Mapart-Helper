package rh.maparthelper.config.palette;

import net.minecraft.block.Block;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CompletePalette {
    public final Map<Integer, List<Block>> palette = new TreeMap<>();

    CompletePalette() {
        PaletteGenerator.initColors(palette);
    }
}
