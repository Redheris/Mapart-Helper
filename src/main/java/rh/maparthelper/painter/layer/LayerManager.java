package rh.maparthelper.painter.layer;

import rh.maparthelper.painter.history.HistoryManager;
import rh.maparthelper.painter.history.action.LayerAddHistoryAction;
import rh.maparthelper.painter.history.action.LayerMergeHistoryAction;
import rh.maparthelper.painter.history.action.LayerMoveHistoryAction;
import rh.maparthelper.painter.history.action.LayerRemoveHistoryAction;
import rh.maparthelper.painter.surface.PixelSurface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @param <T> Specific {@link PixelSurface} implementation
 * @param <E> Specific {@link Layer} implementation also using {@code T} as PixelSurface implementation
 */
public class LayerManager<T extends PixelSurface, E extends Layer<T>> {
    private final HistoryManager historyManager;
    private final List<E> layers;
    private final LayerFactory<T, E> layerFactory;
    private E selectedLayer;

    public LayerManager(HistoryManager historyManager, E mainLayer, LayerFactory<T, E> layerFactory) {
        this.historyManager = historyManager;
        this.layers = new ArrayList<>();
        layers.add(mainLayer);
        this.selectedLayer = mainLayer;
        this.layerFactory = layerFactory;
    }

    public E getSelectedLayer() {
        return selectedLayer;
    }

    public List<E> getLayers() {
        return layers;
    }

    public void setSelectedLayer(E layer) {
        if (layers.contains(layer)) {
            this.selectedLayer = layer;
        }
    }

    public void createEmptyLayer(int width, int height) {
        int selectedIndex = layers.indexOf(selectedLayer);
        E newLayer = layerFactory.createEmpty(width, height, layers.size() + 1);
        this.layers.add(selectedIndex + 1, newLayer);

        historyManager.saveAction(new LayerAddHistoryAction<>(
                this, this.selectedLayer, newLayer, selectedIndex + 1
        ));
        this.selectedLayer = newLayer;
    }

    public void copyLayer(E layer) {
        int index = layers.indexOf(layer);
        if (index < 0) return;
        E newLayer = layerFactory.copy(selectedLayer);
        this.layers.add(index + 1, newLayer);

        historyManager.saveAction(new LayerAddHistoryAction<>(
                this, this.selectedLayer, newLayer, index + 1
        ));
        this.selectedLayer = newLayer;
    }

    private int removeLayerWithoutHistory(E layer) {
        int index = layers.indexOf(layer);
        if (index < 0 || layers.size() == 1) return -1;
        layers.remove(layer);
        selectedLayer = layers.get(Math.max(0, index - 1));
        return index;
    }

    public void removeLayer(E layer) {
        int index = removeLayerWithoutHistory(layer);
        if (index < 0) return;

        historyManager.saveAction(new LayerRemoveHistoryAction<>(
                this, this.selectedLayer, layer, index
        ));
    }

    public void moveLayerUp(E layer) {
        int index = layers.indexOf(layer);
        if (index < 0 || index == layers.size() - 1) return;
        E layerAbove = layers.get(index + 1);
        layers.set(index, layerAbove);
        layers.set(index + 1, layer);

        historyManager.saveAction(new LayerMoveHistoryAction<>(this, index, index + 1));
    }

    public void moveLayerDown(E layer) {
        int index = layers.indexOf(layer);
        if (index < 0 || index == 0) return;
        E layerBelow = layers.get(index - 1);
        layers.set(index, layerBelow);
        layers.set(index - 1, layer);

        historyManager.saveAction(new LayerMoveHistoryAction<>(this, index, index - 1));
    }

    public void mergeLayerWithBelow(E layerAbove) {
        int index = layers.indexOf(layerAbove);
        if (index <= 0) return;
        E layerBelow = layers.get(index - 1);
        E layerMerged = layerFactory.merge(layerAbove, layerBelow);

        layers.remove(layerAbove);
        layers.set(index - 1, layerMerged);
        if (selectedLayer == layerAbove || selectedLayer == layerBelow) {
            this.selectedLayer = layerMerged;
        }

        historyManager.saveAction(new LayerMergeHistoryAction<>(
                this, layerAbove, layerBelow, layerMerged, index - 1
        ));
    }

    public boolean isTopLayer(E layer) {
        return Objects.equals(layers.getLast(), layer);
    }

    public boolean isBottomLayer(E layer) {
        return Objects.equals(layers.getFirst(), layer);
    }
}
