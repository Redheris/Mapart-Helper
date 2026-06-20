package rh.maparthelper.painter.surface;

/**
 * Adapter type to interact with different image data formats from the painter methods
 */
public interface PixelSurface {
    int getWidth();

    int getHeight();

    int getPixel(int x, int y);

    boolean setPixel(int x, int y, int color);

    boolean containsPixel(int x, int y);

    boolean isWritable();
}
