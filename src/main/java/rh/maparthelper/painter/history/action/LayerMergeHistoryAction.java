package rh.maparthelper.painter.history.action;

import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class LayerMergeHistoryAction<T extends PixelSurface, E extends Layer<T>> implements HistoryAction {
    private final LayerManager<T, E> layerManager;
    private final E layerAbove;
    private final E layerBelow;
    private final E layerMerged;
    private final int belowIndex;

    public LayerMergeHistoryAction(LayerManager<T, E> layerManager, E layerAbove, E layerBelow, E layerMerged, int belowIndex) {
        this.layerManager = layerManager;
        this.layerAbove = layerAbove;
        this.layerBelow = layerBelow;
        this.layerMerged = layerMerged;
        this.belowIndex = belowIndex;
    }

    @Override
    public HistoryActionType type() {
        return HistoryActionType.LAYERS;
    }

    @Override
    public void undo() {
        var layers = layerManager.getLayers();
        layers.add(belowIndex + 1, layerAbove);
        layers.set(belowIndex, layerBelow);
        layerManager.setSelectedLayer(layerAbove);
    }

    @Override
    public void redo() {
        var layers = layerManager.getLayers();
        layers.remove(layerAbove);
        layers.set(belowIndex, layerMerged);
        layerManager.setSelectedLayer(layerMerged);
    }

    @Override
    public void discardedFromRedoHistory() {
        layerMerged.dispose();
    }

    @Override
    public void discardedFromUndoHistory() {
        layerAbove.dispose();
        layerBelow.dispose();
    }
}
