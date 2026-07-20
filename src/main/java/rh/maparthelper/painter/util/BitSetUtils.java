package rh.maparthelper.painter.util;

import java.util.BitSet;

public class BitSetUtils {
    public static void clearAndFill(BitSet set, int width, int height, int x0, int y0, int x1, int y1) {
        set.clear();

        x0 = Math.clamp(x0, 0, width - 1);
        x1 = Math.clamp(x1, 0, width - 1);
        y0 = Math.clamp(y0, 0, height - 1);
        y1 = Math.clamp(y1, 0, height - 1);

        int x = Math.min(x0, x1);
        int y = Math.min(y0, y1);
        int lineWidth = Math.abs(x1 - x0) + 1;

        for (; y <= Math.max(y0, y1); y++) {
            int id = x + y * width;
            set.set(id, id + lineWidth);
        }
    }
}
