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
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float contrastFactor = (259 * (contrastLevel + 255)) / (255f * (259 - contrastLevel));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int newArgb = getNewArgb(argb, brightnessFactor, contrastFactor);
                result.setRGB(x, y, newArgb);
            }
        }

        if (Float.compare(saturationFactor, 1.0f) != 0) {
            result = changeSaturation(result, saturationFactor);
        }

        return result;
    }

    private static int getNewArgb(int argb, float brightnessFactor, float contrastFactor) {
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        red = clamp((int) (red * brightnessFactor), 0, 255);
        green = clamp((int) (green * brightnessFactor), 0, 255);
        blue = clamp((int) (blue * brightnessFactor), 0, 255);

        red = clamp((int) (contrastFactor * (red - 128) + 128), 0, 255);
        green = clamp((int) (contrastFactor * (green - 128) + 128), 0, 255);
        blue = clamp((int) (contrastFactor * (blue - 128) + 128), 0, 255);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static BufferedImage changeSaturation(BufferedImage image, float saturationFactor) {
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
