package rh.maparthelper.conversion.dithering;

import com.google.common.util.concurrent.AtomicDouble;
import rh.maparthelper.mapart.AbstractMapart;

import java.awt.image.BufferedImage;

public abstract class ColorConverter {
    protected final AbstractMapart mapart;
    protected final BufferedImage original;

    protected final boolean use3D;
    protected final int backgroundColor;
    protected final int backgroundMapColorId;

    protected final int[] topLineBright;
    protected final int[] topLineCorrect;

    protected final AtomicDouble progress;

    public ColorConverter(ConversionContext context) {
        this.mapart = context.mapart();
        this.original = context.original();
        this.use3D = context.use3D();
        this.backgroundColor = context.backgroundColor();
        this.backgroundMapColorId = context.backgroundMapColorId();
        this.topLineBright = context.topLineBright();
        this.topLineCorrect = context.topLineCorrect();
        this.progress = context.progress();
    }

    public abstract BufferedImage convertColors();
}
