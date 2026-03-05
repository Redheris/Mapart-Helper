package rh.maparthelper.conversion.dithering;

import com.google.common.util.concurrent.AtomicDouble;
import rh.maparthelper.conversion.dithering.diffusion.*;
import rh.maparthelper.mapart.AbstractMapart;

import java.awt.image.BufferedImage;

public enum ColorConverters {
    SIMPLE(SimpleColorConverter::new),
    FLOYD_STEINBERG(FloydSteinbergDithering::new),
    ATKINSON(AtkinsonDithering::new),
    JJN(JarvisJudiceNinkeDithering::new),
    STUCKI(StuckiDithering::new),
    BURKES(BurkesDithering::new),
    SIERRA(SierraDithering::new),
    SIERRA_LITE(SierraLiteDithering::new),
    SIERRA_2ROW(Sierra2RowDithering::new);

    private final ColorConverterFactory factory;

    ColorConverters(ColorConverterFactory factory) {
        this.factory = factory;
    }

    public ColorConverter createColorConverter(AbstractMapart mapart, BufferedImage original, boolean use3D,
                                               int backgroundColor, int backgroundMapColorId, int[] topLineBright,
                                               int[] topLineCorrect, AtomicDouble progress) {
        ConversionContext context = new ConversionContext(
                mapart,
                original,
                use3D,
                backgroundColor,
                backgroundMapColorId,
                topLineBright,
                topLineCorrect,
                progress
        );
        return factory.create(context);
    }

    @FunctionalInterface
    private interface ColorConverterFactory {
        ColorConverter create(ConversionContext ctx);
    }
}
