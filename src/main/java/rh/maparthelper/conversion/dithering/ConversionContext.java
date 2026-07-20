package rh.maparthelper.conversion.dithering;

import com.google.common.util.concurrent.AtomicDouble;
import rh.maparthelper.mapart.AbstractMapart;

import java.awt.image.BufferedImage;

public record ConversionContext(
        AbstractMapart mapart,
        BufferedImage original,
        boolean use3D,
        int backgroundColor,
        int backgroundMapColorId,
        int[] topLineBright,
        int[] topLineCorrect,
        AtomicDouble progress,
        float redPropagationWeight,
        float greenPropagationWeight,
        float bluePropagationWeight
) {

}
