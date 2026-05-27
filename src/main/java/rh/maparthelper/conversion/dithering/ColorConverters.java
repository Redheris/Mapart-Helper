package rh.maparthelper.conversion.dithering;

import com.google.common.util.concurrent.AtomicDouble;
import rh.maparthelper.conversion.dithering.diffusion.*;
import rh.maparthelper.conversion.dithering.ordered.DitherMatrices;
import rh.maparthelper.conversion.dithering.ordered.OrderedDithering;
import rh.maparthelper.mapart.AbstractMapart;

import java.awt.image.BufferedImage;

import static rh.maparthelper.conversion.dithering.DitheringTypes.*;

public enum ColorConverters {
    SIMPLE(NONE, SimpleColorConverter::new),
    FLOYD_STEINBERG(ERROR_DIFFUSION, FloydSteinbergDithering::new),
    ATKINSON(ERROR_DIFFUSION, AtkinsonDithering::new),
    JJN(ERROR_DIFFUSION, JarvisJudiceNinkeDithering::new),
    STUCKI(ERROR_DIFFUSION, StuckiDithering::new),
    BURKES(ERROR_DIFFUSION, BurkesDithering::new),
    SIERRA(ERROR_DIFFUSION, SierraDithering::new),
    SIERRA_LITE(ERROR_DIFFUSION, SierraLiteDithering::new),
    SIERRA_2ROW(ERROR_DIFFUSION, Sierra2RowDithering::new),
    BAYER_2x2(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.BAYER_2x2)),
    BAYER_3x3(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.BAYER_3x3)),
    BAYER_4x4(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.BAYER_4x4)),
    BLUE_NOISE_14x14(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.BLUE_NOISE_14x14)),
    BLUE_NOISE_16x16(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.BLUE_NOISE_16x16)),
    HALFTONE_8x8(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.HALFTONE_8x8)),
    CLUSTER_DOT_4x4(ORDERED, ctx -> new OrderedDithering(ctx, DitherMatrices.CLUSTER_DOT_4x4));

    private final DitheringTypes ditheringType;
    private final ColorConverterFactory factory;

    ColorConverters(DitheringTypes ditheringType, ColorConverterFactory factory) {
        this.ditheringType = ditheringType;
        this.factory = factory;
    }

    public DitheringTypes ditheringType() {
        return this.ditheringType;
    }

    public ColorConverter createColorConverter(AbstractMapart mapart, BufferedImage original, boolean use3D,
                                               int backgroundColor, int backgroundMapColorId, int[] topLineBright,
                                               int[] topLineCorrect, AtomicDouble progress,
                                               float redPropagation, float greenPropagation, float bluePropagation) {
        ConversionContext context = new ConversionContext(
                mapart,
                original,
                use3D,
                backgroundColor,
                backgroundMapColorId,
                topLineBright,
                topLineCorrect,
                progress,
                redPropagation,
                greenPropagation,
                bluePropagation
        );
        return factory.create(context);
    }

    @FunctionalInterface
    private interface ColorConverterFactory {
        ColorConverter create(ConversionContext ctx);
    }
}
