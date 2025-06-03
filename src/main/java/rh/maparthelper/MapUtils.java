package rh.maparthelper;

import org.joml.Vector2i;

public class MapUtils {

    public static Vector2i getMapAreaStartPos(int x, int z) {
        int posX = (Math.floorDiv(x + 64, 128) * 128) - 64;
        int posZ = (Math.floorDiv(z + 64, 128) * 128) - 64;
        return new Vector2i(posX, posZ);
    }
}
