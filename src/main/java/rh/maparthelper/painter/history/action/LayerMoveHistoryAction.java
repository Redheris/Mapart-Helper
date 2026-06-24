package rh.maparthelper.painter.history.action;

import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class LayerMoveHistoryAction<T extends PixelSurface, E extends Layer<T>> implements HistoryAction {
    private final LayerManager<T, E> layerManager;
    private final int oldIndex;
    private final int newIndex;

    public LayerMoveHistoryAction(LayerManager<T, E> layerManager, int oldIndex, int newIndex) {
        this.layerManager = layerManager;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    @Override
    public HistoryActionType type() {
        return HistoryActionType.LAYERS;
    }

    @Override
    public void undo() {
        E layer = layerManager.getLayers().remove(newIndex);
        layerManager.getLayers().add(oldIndex, layer);
    }

    @Override
    public void redo() {
        E layer = layerManager.getLayers().remove(oldIndex);
        layerManager.getLayers().add(newIndex, layer);
    }
}
