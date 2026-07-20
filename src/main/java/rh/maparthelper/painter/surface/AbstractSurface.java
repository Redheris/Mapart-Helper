package rh.maparthelper.painter.surface;

/**
 * Base abstract implementation of PixelSurface with index-safe operations and read-write or read-only modes
 */
public abstract class AbstractSurface implements PixelSurface {
    private final boolean writable;

    public AbstractSurface(boolean writable) {
        this.writable = writable;
    }

    @Override
    public final boolean setPixel(int x, int y, int color) {
        if (!writable || !containsPixel(x, y)) return false;
        return setPixelUnsafe(x, y, color);
    }

    @Override
    public final int getPixel(int x, int y) {
        if (!containsPixel(x, y)) return 0;
        return getPixelUnsafe(x, y);
    }

    protected abstract boolean setPixelUnsafe(int x, int y, int color);

    protected abstract int getPixelUnsafe(int x, int y);

    @Override
    public boolean containsPixel(int x, int y) {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    @Override
    public boolean isWritable() {
        return writable;
    }
}
