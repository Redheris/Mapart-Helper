package rh.maparthelper.conversion.colors;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.clamp;

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
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue  = argb & 0xFF;

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
