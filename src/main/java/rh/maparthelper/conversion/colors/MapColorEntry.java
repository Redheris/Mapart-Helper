package rh.maparthelper.conversion.colors;

import net.minecraft.block.MapColor;

public record MapColorEntry(MapColor mapColor, MapColor.Brightness brightness) {
    public static final MapColorEntry CLEAR = new MapColorEntry(MapColor.CLEAR, MapColor.Brightness.NORMAL);
}
