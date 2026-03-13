package rh.maparthelper.conversion.dithering.ordered;

import net.minecraft.block.MapColor;
import rh.maparthelper.colors.DitherEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.dithering.ColorConverter;
import rh.maparthelper.conversion.dithering.ConversionContext;
import rh.maparthelper.mapart.ColorsCounter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class OrderedDithering extends ColorConverter {
    protected final float[][] matrix;
    protected final int N;

    public OrderedDithering(ConversionContext context, float[][] matrix) {
        super(context);
        this.matrix = matrix;
        this.N = matrix.length;
    }

    @Override
    public BufferedImage convertColors() {
        BufferedImage convertedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int width = convertedImage.getWidth();
        int[] originalPixels = ((DataBufferInt) original.getRaster().getDataBuffer()).getData();
        int[] resultPixels = ((DataBufferInt) convertedImage.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / originalPixels.length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < convertedImage.getHeight(); y++) {
                ColorsCounter colorsCounter = mapart.getColorsCounterFor(x / 128, y / 128);
                if (colorsCounter == null || Thread.currentThread().isInterrupted()) {
                    mapart.clearColorCounters();
                    return null;
                }
                int argb = originalPixels[x + y * width];
                if (argb == 0 && backgroundColor != 0) {
                    resultPixels[x + y * width] = backgroundColor;
                    colorsCounter.increment(backgroundMapColorId);
                    continue;
                }

                int r = (argb >> 16) & 255;
                int g = (argb >> 8) & 255;
                int b = argb & 255;
                int offset = (int) (((matrix[y % N][x % N] - 0.5) * 24));
                r = Math.clamp(r + offset, 0, 255);
                g = Math.clamp(g + offset, 0, 255);
                b = Math.clamp(b + offset, 0, 255);
                argb = (255 << 24) | (r << 16) | (g << 8) | b;

                DitherEntry mapColors = PaletteColors.getClosestColor(argb, use3D);
                byte closestColorByte = mapColors.colorByte1() /* WIP */;
                MapColor closestColor = mapColors.getFirstMapColor();
                int newArgb;
                if (mapColors == DitherEntry.CLEAR) {
                    newArgb = backgroundColor;
                } else {
                    if (y > 0 && resultPixels[x + (y - 1) * width] == 0)
                        newArgb = PaletteColors.getMapRenderColor(closestColorByte, MapColor.Brightness.HIGH);
                    else newArgb = PaletteColors.getMapRenderColor(closestColorByte);
                    colorsCounter.increment(closestColor.id);
                }
                if (y == mapart.getInsertionY() && x >= mapart.getInsertionX()) {
                    topLineBright[x - mapart.getInsertionX()] = newArgb;
                    topLineCorrect[x - mapart.getInsertionX()] = MapColor.getRenderColor(mapColors.colorByte1());
                }
                resultPixels[x + y * width] = newArgb;
                progress.addAndGet(progressStep);
            }
        }

        return convertedImage;
    }
}
