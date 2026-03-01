package rh.maparthelper.colors;

import net.minecraft.block.MapColor;

public record MapColorEntry(MapColor mapColor, MapColor.Brightness brightness, int errorRed, int errorGreen,
                            int errorBlue) {
    public static final MapColorEntry CLEAR = new MapColorEntry(MapColor.CLEAR, MapColor.Brightness.NORMAL, 0, 0, 0);

    public MapColorEntry(MapColor mapColor, MapColor.Brightness brightness) {
        this(mapColor, brightness, 0, 0, 0);
    }

    public int getRenderColor() {
        return mapColor.getRenderColor(brightness);
    }
}
