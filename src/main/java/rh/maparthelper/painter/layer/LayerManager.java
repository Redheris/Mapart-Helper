package rh.maparthelper.painter.layer;

import rh.maparthelper.painter.surface.PixelSurface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @param <T> Specific {@link PixelSurface} implementation
 * @param <E> Specific {@link Layer} implementation also using {@code T} as PixelSurface implementation
 */
public class LayerManager<T extends PixelSurface, E extends Layer<T>> {
    private final List<E> layers;
    private E selectedLayer;
    private int selectedLayerIndex;

    public LayerManager(E mainLayer) {
        this.layers = new ArrayList<>();
        layers.add(mainLayer);
        this.selectedLayer = mainLayer;
        this.selectedLayerIndex = 0;
    }

    public E getSelectedLayer() {
        return selectedLayer;
    }

    public List<E> getLayers() {
        return layers;
    }

    public void setSelectedLayer(int index) {
        if (index < 0 || index >= layers.size()) return;

        this.selectedLayer = layers.get(index);
        this.selectedLayerIndex = index;
    }

    public void createLayer(E layer) {
        this.layers.add(selectedLayerIndex + 1, layer);
        this.selectedLayer = layer;
        this.selectedLayerIndex = selectedLayerIndex + 1;
    }

    public boolean removeLayer(int index) {
        if (this.layers.isEmpty()) return false;
        this.layers.remove(index);
        if (selectedLayerIndex == index) {
            this.selectedLayerIndex = Math.max(0, index - 1);
        }
        return true;
    }

    public boolean isTopLayerSelected() {
        return Objects.equals(layers.getFirst(), selectedLayer);
    }

    public boolean isBottomLayerSelected() {
        return Objects.equals(layers.getLast(), selectedLayer);
    }
}
