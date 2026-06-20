package rh.maparthelper.painter.surface;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;

public class NativeImageSurface extends AbstractSurface {
    private final NativeImage image;

    public NativeImageSurface(@NotNull NativeImage image, boolean writable) {
        super(writable);
        this.image = image;
    }

    public NativeImageSurface(@NotNull NativeImage image) {
        this(image, true);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public int getPixelUnsafe(int x, int y) {
        return image.getPixel(x, y);
    }

    @Override
    public boolean setPixelUnsafe(int x, int y, int color) {
        if (image.getPixel(x, y) == color) return false;

        image.setPixel(x, y, color);

        return true;
    }
}
