package rh.maparthelper.conversion.colors;

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


    public static double colorDistanceARGB(int argb1, int argb2) {
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

    public static double colorDistanceLAB(int[] lab1, int[] lab2) {
        int L1 = lab1[0];
        int a1 = lab1[1];
        int b1 = lab1[2];

        int L2 = lab2[0];
        int a2 = lab2[1];
        int b2 = lab2[2];

        int dL = L2 - L1;
        int da = a2 - a1;
        int db = b2 - b1;

        return Math.sqrt(dL * dL + da * da + db * db);
    }

    public static double colorDistance(int argb1, int argb2, boolean useLAB) {
        if (useLAB) {
            int[] lab1 = rgb2LabCache.computeIfAbsent(argb1, ColorUtils::rgb2lab);
            int[] lab2 = rgb2LabCache.computeIfAbsent(argb2, ColorUtils::rgb2lab);
            return colorDistanceLAB(lab1, lab2);
        }
        return colorDistanceARGB(argb1, argb2);
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

        int argb, newArgb, alpha;
        int[] rgb = new int[3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                argb = image.getRGB(x, y);
                alpha = (argb >> 24) & 0xFF;
                rgb[0] = (argb >> 16) & 0xFF;
                rgb[1] = (argb >> 8) & 0xFF;
                rgb[2] = argb & 0xFF;

                if (!neutralBrightness) applyBrightness(rgb, brightnessFactor);
                if (!neutralContrast) applyContrast(rgb, contrastFactor);

                newArgb = (alpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                result.setRGB(x, y, newArgb);
            }
        }
        return neutralSaturation ? result : applySaturation(result, saturationFactor);
    }

    private static void applyBrightness(int[] rgb, float brightnessFactor) {
        rgb[0] = clamp((int) (rgb[0] * brightnessFactor), 0, 255);
        rgb[1] = clamp((int) (rgb[1] * brightnessFactor), 0, 255);
        rgb[2] = clamp((int) (rgb[2] * brightnessFactor), 0, 255);
    }

    private static void applyContrast(int[] rgb, float contrastFactor) {
        rgb[0] = clamp((int) (contrastFactor * (rgb[0] - 128) + 128), 0, 255);
        rgb[1] = clamp((int) (contrastFactor * (rgb[1] - 128) + 128), 0, 255);
        rgb[2] = clamp((int) (contrastFactor * (rgb[2] - 128) + 128), 0, 255);
    }

    private static BufferedImage applySaturation(BufferedImage image, float saturationFactor) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);

                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                float[] hsb = Color.RGBtoHSB(red, green, blue, null);
                float hue = hsb[0];
                float saturation = hsb[1];
                float brightness = hsb[2];

                saturation = Math.max(0f, Math.min(1f, saturation * saturationFactor));

                int rgb = Color.HSBtoRGB(hue, saturation, brightness);
                int newArgb = (alpha << 24) | (rgb & 0x00FFFFFF);
                result.setRGB(x, y, newArgb);
            }
        }

        return result;
    }
}
