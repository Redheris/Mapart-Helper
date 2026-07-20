package rh.maparthelper.painter.history.action;

import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class LayerRemoveHistoryAction<T extends PixelSurface, E extends Layer<T>> implements HistoryAction {
    private final LayerManager<T, E> layerManager;
    private final E nextSelectedLayer;
    private final E removedLayer;
    private final int layerIndex;

    public LayerRemoveHistoryAction(LayerManager<T, E> layerManager, E nextSelectedLayer, E removedLayer, int layerIndex) {
        this.layerManager = layerManager;
        this.nextSelectedLayer = nextSelectedLayer;
        this.removedLayer = removedLayer;
        this.layerIndex = layerIndex;
    }

    @Override
    public HistoryActionType type() {
        return HistoryActionType.LAYERS;
    }

    @Override
    public void undo() {
        layerManager.getLayers().add(layerIndex, removedLayer);
        layerManager.setSelectedLayer(removedLayer);
    }

    @Override
    public void redo() {
        layerManager.getLayers().remove(removedLayer);
        layerManager.setSelectedLayer(nextSelectedLayer);
    }

    @Override
    public void discardedFromUndoHistory() {
        removedLayer.dispose();
    }
}