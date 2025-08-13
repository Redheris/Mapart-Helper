package rh.maparthelper.conversion.colors;

import net.minecraft.block.MapColor;

public record MapColorEntry(MapColor mapColor, MapColor.Brightness brightness, int[] distError) {
    public MapColorEntry(MapColor mapColor, MapColor.Brightness brightness) {
        this(mapColor, brightness, new int[0]);
    }
    public static final MapColorEntry CLEAR = new MapColorEntry(MapColor.CLEAR, MapColor.Brightness.NORMAL, new int[0]);
}
