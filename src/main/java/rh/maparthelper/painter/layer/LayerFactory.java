package rh.maparthelper.painter.layer;

import rh.maparthelper.painter.surface.PixelSurface;

public interface LayerFactory<T extends PixelSurface, E extends Layer<T>> {
    E createEmpty(int width, int height, int layerNumber);

    E copy(E origin);

    E merge(E layerAbove, E layerBelow);
}
