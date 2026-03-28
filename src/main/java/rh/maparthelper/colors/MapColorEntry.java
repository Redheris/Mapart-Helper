package rh.maparthelper.colors;

import net.minecraft.world.level.material.MapColor;

public record MapColorEntry(MapColor mapColor, MapColor.Brightness brightness) {
    public static final MapColorEntry CLEAR = new MapColorEntry(MapColor.NONE, MapColor.Brightness.NORMAL);

    public int getRenderColor() {
        return mapColor.calculateARGBColor(brightness);
    }
}
