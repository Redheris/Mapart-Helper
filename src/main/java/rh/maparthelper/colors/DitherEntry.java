package rh.maparthelper.colors;

import net.minecraft.world.level.material.MapColor;

public record DitherEntry(byte colorByte1, byte colorByte2, float distRatio, int errorRed, int errorGreen,
                          int errorBlue) {
    public static DitherEntry CLEAR = new DitherEntry(
            (byte) 0, (byte) 0, 0, 0, 0, 0
    );

    public DitherEntry(byte colorByte) {
        this(colorByte, colorByte, 0, 0, 0, 0);
    }

    public MapColor getFirstMapColor() {
        return MapColor.byId((colorByte1 & 0xFF) >> 2);
    }

    public MapColor getSecondMapColor() {
        return MapColor.byId((colorByte2 & 0xFF) >> 2);
    }
}
