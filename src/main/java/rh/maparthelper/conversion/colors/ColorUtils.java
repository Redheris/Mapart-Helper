package rh.maparthelper.conversion.colors;

public class ColorUtils {

    public static double colorDistance(int argb1, int argb2) {
        int r1 = (argb1 >> 16) & 0xFF;
        int g1 = (argb1 >> 8) & 0xFF;
        int b1 = argb1 & 0xFF;

        int r2 = (argb2 >> 16) & 0xFF;
        int g2 = (argb2 >> 8) & 0xFF;
        int b2 = argb2 & 0xFF;

        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
