package rh.maparthelper.conversion.dithering;

import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.colors.DitherEntry;
import rh.maparthelper.mapart.ColorsCounter;
import rh.maparthelper.palette.PaletteColors;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.stream.IntStream;

public class SimpleColorConverter extends ColorConverter {
    public SimpleColorConverter(ConversionContext context) {
        super(context);
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
                int newArgb;

                DitherEntry mapColors = useUnobtainableColors ? PaletteColors.getClosestColorUnconditional(argb) : PaletteColors.getClosestColor(argb, use3D);
                MapColor closestColor = mapColors.getFirstMapColor();

                if (mapColors == DitherEntry.CLEAR) {
                    newArgb = backgroundColor;
                } else {
                    if (!useUnobtainableColors && y > 0 && resultPixels[x + (y - 1) * width] == 0)
                        newArgb = PaletteColors.getMapRenderColor(mapColors.colorByte1(), MapColor.Brightness.HIGH);
                    else
                        newArgb = PaletteColors.getMapRenderColor(mapColors.colorByte1());
                    colorsCounter.increment(closestColor.id);
                }
                if (y == mapart.getInsertionY() && x >= mapart.getInsertionX()) {
                    topLineBright[x - mapart.getInsertionX()] = newArgb;
                    topLineCorrect[x - mapart.getInsertionX()] = PaletteColors.getMapRenderColor(mapColors.colorByte1());
                }
                resultPixels[x + y * width] = newArgb;
                progress.addAndGet(progressStep);
            }
        });

        return convertedImage;
    }
}
