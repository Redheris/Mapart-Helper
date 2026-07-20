package rh.maparthelper.painter.history.action;

import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class LayerAddHistoryAction<T extends PixelSurface, E extends Layer<T>> implements HistoryAction {
    private final LayerManager<T, E> layerManager;
    private final E prevSelectedLayer;
    private final E addedLayer;
    private final int layerIndex;

    public LayerAddHistoryAction(LayerManager<T, E> layerManager, E prevSelectedLayer, E addedLayer, int layerIndex) {
        this.layerManager = layerManager;
        this.prevSelectedLayer = prevSelectedLayer;
        this.addedLayer = addedLayer;
        this.layerIndex = layerIndex;
    }

    @Override
    public HistoryActionType type() {
        return HistoryActionType.LAYERS;
    }

    @Override
    public void undo() {
        layerManager.getLayers().remove(addedLayer);
        layerManager.setSelectedLayer(prevSelectedLayer);
    }

    @Override
    public void redo() {
        layerManager.getLayers().add(layerIndex, addedLayer);
        layerManager.setSelectedLayer(addedLayer);
    }

    @Override
    public void discardedFromRedoHistory() {
        addedLayer.dispose();
    }
}
