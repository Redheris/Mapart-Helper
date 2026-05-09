package rh.maparthelper.conversion.dithering.ordered;

import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.DitherEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.dithering.ColorConverter;
import rh.maparthelper.conversion.dithering.ConversionContext;
import rh.maparthelper.mapart.ColorsCounter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.stream.IntStream;

public class OrderedDithering extends ColorConverter {
    protected final int[][] matrix;
    protected final int N;
    protected final int matrixSize;

    public OrderedDithering(ConversionContext context, int[][] matrix) {
        super(context);
        this.matrix = matrix;
        this.N = matrix.length;
        this.matrixSize = N * N;
    }

    @Override
    public BufferedImage convertColors(boolean useUnobtainableColors, boolean useMultithreading) {
        BufferedImage convertedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int width = convertedImage.getWidth();
        int[] originalPixels = ((DataBufferInt) original.getRaster().getDataBuffer()).getData();
        int[] resultPixels = ((DataBufferInt) convertedImage.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / originalPixels.length;

        IntStream xStream;
        if (useMultithreading)
            xStream = IntStream.range(0, width).parallel();
        else
            xStream = IntStream.range(0, width);

        xStream.forEach(x -> {
            for (int y = 0; y < convertedImage.getHeight(); y++) {
                ColorsCounter colorsCounter = mapart.getColorsCounterFor(x / 128, y / 128);
                if (colorsCounter == null || Thread.currentThread().isInterrupted()) {
                    mapart.clearColorCounters();
                    return;
                }
                int argb = originalPixels[x + y * width];
                if (argb == 0 && backgroundColor != 0) {
                    resultPixels[x + y * width] = backgroundColor;
                    colorsCounter.increment(backgroundMapColorId);
                    continue;
                }

                DitherEntry mapColors = useUnobtainableColors ? PaletteColors.getClosestColorUnconditional(argb) : PaletteColors.getClosestColor(argb, use3D);
                byte closestColorByte;
                MapColor closestColor;

                int threshold = matrix[y % N][x % N];
                if (mapColors.distRatio() * matrixSize > threshold) {
                    closestColorByte = mapColors.colorByte1();
                    closestColor = mapColors.getFirstMapColor();
                } else {
                    closestColorByte = mapColors.colorByte2();
                    closestColor = mapColors.getSecondMapColor();
                }

                int newArgb;
                if (mapColors == DitherEntry.CLEAR) {
                    newArgb = backgroundColor;
                } else {
                    if (!useUnobtainableColors && y > 0 && resultPixels[x + (y - 1) * width] == 0)
                        newArgb = PaletteColors.getMapRenderColor(closestColorByte, MapColor.Brightness.HIGH);
                    else newArgb = PaletteColors.getMapRenderColor(closestColorByte);
                    colorsCounter.increment(closestColor.id);
                }
                if (y == mapart.getInsertionY() && x >= mapart.getInsertionX()) {
                    topLineBright[x - mapart.getInsertionX()] = newArgb;
                    topLineCorrect[x - mapart.getInsertionX()] = PaletteColors.getMapRenderColor(closestColorByte);
                }
                resultPixels[x + y * width] = newArgb;
                progress.addAndGet(progressStep);
            }
        });

        return convertedImage;
    }
}
