package rh.maparthelper.conversion.dithering;

import com.google.common.util.concurrent.AtomicDouble;
import rh.maparthelper.conversion.dithering.diffusion.*;
import rh.maparthelper.conversion.dithering.ordered.DitherMatrices;
import rh.maparthelper.conversion.dithering.ordered.OrderedDithering;
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
    SIERRA_2ROW(Sierra2RowDithering::new),
    BAYER_2x2(ctx -> new OrderedDithering(ctx, DitherMatrices.BAYER_2x2)),
    BAYER_4x4(ctx -> new OrderedDithering(ctx, DitherMatrices.BAYER_4x4)),
    BLUE_NOISE_14x14(ctx -> new OrderedDithering(ctx, DitherMatrices.BLUE_NOISE_14x14)),
    BLUE_NOISE_16x16(ctx -> new OrderedDithering(ctx, DitherMatrices.BLUE_NOISE_16x16)),
    HALFTONE_8x8(ctx -> new OrderedDithering(ctx, DitherMatrices.HALFTONE_8x8)),
    CLUSTER_DOT_4x4(ctx -> new OrderedDithering(ctx, DitherMatrices.CLUSTER_DOT_4x4));

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
