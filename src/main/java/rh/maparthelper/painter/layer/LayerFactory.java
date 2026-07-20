package rh.maparthelper.painter.layer;

import org.jetbrains.annotations.NotNull;
import rh.maparthelper.painter.surface.PixelSurface;

import java.util.List;

public interface LayerFactory<T extends PixelSurface, E extends Layer<T>> {
    E createEmpty(int width, int height, int layerNumber);

    E copy(E origin);

    E merge(E layerAbove, E layerBelow);

    E flattenLayers(@NotNull List<E> layers);
}
