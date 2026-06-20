package rh.maparthelper.painter;

import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.history.HistoryManager;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class ImageDocument<T extends PixelSurface, E extends Layer<T>> {
    private final DrawingEngine<T> drawingEngine;
    private final LayerManager<T, E> layerManager;
    private final HistoryManager historyManager;

    public ImageDocument(E mainLayer) {
        this.layerManager = new LayerManager<>(mainLayer);
        this.historyManager = new HistoryManager();
        this.drawingEngine = new DrawingEngine<>(layerManager, historyManager);
    }

    public LayerManager<T, E> getLayerManager() {
        return layerManager;
    }

    public DrawingEngine<T> getDrawingEngine() {
        return drawingEngine;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }
}
