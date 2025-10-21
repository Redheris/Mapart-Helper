package rh.maparthelper.colors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.clamp;

public class ColorUtils {

    private final static Map<Integer, int[]> rgb2LabCache = new HashMap<>();

    public static void clearRgb2LabCache() {
        rgb2LabCache.clear();
    }

    // rgb2lab conversion based on the one from the redstonehelper's program
    // https://github.com/redstonehelper/MapConverter/blob/main/MapConverter.java
    public static int[] rgb2lab(int argb) {
        double r = (argb >> 16) & 0xFF;
        double g = (argb >> 8) & 0xFF;
        double b = argb & 0xFF;

        r = (r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
        g = (g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
        b = (b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;

        double[] XYZ = new double[3];
        XYZ[0] = 0.4360747 * r + 0.3850649 * g + 0.1430804 * b;
        XYZ[1] = 0.2225045 * r + 0.7168786 * g + 0.0606169 * b;
        XYZ[2] = 0.0139322 * r + 0.0971045 * g + 0.7141733 * b;

        XYZ[0] = XYZ[0] / 0.96422;
        XYZ[2] = XYZ[2] / 0.82521;
        double[] fVals = new double[3];

        for (int i = 0; i < 3; i++) {
            double f;
            double val = XYZ[i];
            if (val > (216.0 / 24389.0)) {
                f = Math.pow(val, 1.0 / 3.0);
            } else {
                f = ((24389.0 / 27.0) * val + 16.0) / 116.0;
            }
            fVals[i] = f;
        }

        int[] lab = new int[3];
        lab[0] = (int) (2.55 * (116 * fVals[1] - 16));
        lab[1] = 128 + (int) (500 * (fVals[0] - fVals[1]));
        lab[2] = 128 + (int) (200 * (fVals[1] - fVals[2]));
        return lab;
    }


    public static double colorDistanceARGB_noSqrt(int argb1, int argb2) {
        int[] rgb1 = getRGB(argb1);
        int[] rgb2 = getRGB(argb2);

        int dr = rgb2[0] - rgb1[0];
        int dg = rgb2[1] - rgb1[1];
        int db = rgb2[2] - rgb1[2];

        return dr * dr + dg * dg + db * db;
    }

    public static double colorDistanceLAB_noSqrt(int[] lab1, int[] lab2) {
        int L1 = lab1[0];
        int a1 = lab1[1];
        int b1 = lab1[2];

        int L2 = lab2[0];
        int a2 = lab2[1];
        int b2 = lab2[2];

        int dL = L2 - L1;
        int da = a2 - a1;
        int db = b2 - b1;

        return dL * dL + da * da + db * db;
    }

    public static double colorDistance(int argb1, int argb2, boolean useLAB) {
        if (useLAB) {
            int[] lab1 = rgb2LabCache.computeIfAbsent(argb1, ColorUtils::rgb2lab);
            int[] lab2 = rgb2LabCache.computeIfAbsent(argb2, ColorUtils::rgb2lab);
            return colorDistanceLAB_noSqrt(lab1, lab2);
        }
        return colorDistanceARGB_noSqrt(argb1, argb2);
    }

    public static BufferedImage preprocessImage(BufferedImage image, float brightnessFactor, float contrastLevel, float saturationFactor) {
        float contrastFactor = (259 * (contrastLevel + 255)) / (255f * (259 - contrastLevel));
        boolean neutralBrightness = Float.compare(brightnessFactor, 1.0f) == 0;
        boolean neutralContrast = Float.compare(contrastFactor, 1.0f) == 0;
        boolean neutralSaturation = Float.compare(saturationFactor, 1.0f) == 0;

        if (neutralBrightness && neutralContrast)
            return neutralSaturation ? image : applySaturation(image, saturationFactor);

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argbInt = image.getRGB(x, y);
                if (argbInt == 0) continue;
                int[] argb = getARGB(argbInt);

                if (!neutralBrightness) applyBrightness(argb, brightnessFactor);
                if (!neutralContrast) applyContrast(argb, contrastFactor);

                result.setRGB(x, y, getARGB(argb));
            }
        }
        return neutralSaturation ? result : applySaturation(result, saturationFactor);
    }

    private static void applyBrightness(int[] argb, float brightnessFactor) {
        argb[1] = clamp((int) (argb[1] * brightnessFactor), 0, 255);
        argb[2] = clamp((int) (argb[2] * brightnessFactor), 0, 255);
        argb[3] = clamp((int) (argb[3] * brightnessFactor), 0, 255);
    }

    private static void applyContrast(int[] argb, float contrastFactor) {
        argb[1] = clamp((int) (contrastFactor * (argb[1] - 128) + 128), 0, 255);
        argb[2] = clamp((int) (contrastFactor * (argb[2] - 128) + 128), 0, 255);
        argb[3] = clamp((int) (contrastFactor * (argb[3] - 128) + 128), 0, 255);
    }

    private static BufferedImage applySaturation(BufferedImage image, float saturationFactor) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argbInt = image.getRGB(x, y);
                if (argbInt == 0) continue;
                int[] argb = getARGB(argbInt);

                float[] hsb = Color.RGBtoHSB(argb[1], argb[2], argb[3], null);
                float hue = hsb[0];
                float saturation = hsb[1];
                float brightness = hsb[2];

                saturation = Math.max(0f, Math.min(1f, saturation * saturationFactor));

                int rgb = Color.HSBtoRGB(hue, saturation, brightness);
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    public static int[] getRGB(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return new int[]{r, g, b};
    }

    public static int[] getARGB(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return new int[]{a, r, g, b};
    }

    public static int getARGB(int[] argb) {
        return (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8) | argb[3];
    }
}
