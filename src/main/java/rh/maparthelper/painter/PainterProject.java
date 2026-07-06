package rh.maparthelper.painter;

import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.history.HistoryManager;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerFactory;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class PainterProject<T extends PixelSurface, E extends Layer<T>> implements AutoCloseable {
    private final DrawingEngine<T> drawingEngine;
    private final LayerManager<T, E> layerManager;
    private final HistoryManager historyManager;

    public PainterProject(E mainLayer, LayerFactory<T, E> layerFactory) {
        this.historyManager = new HistoryManager();
        this.layerManager = new LayerManager<>(historyManager, mainLayer, layerFactory);
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

    @Override
    public void close() {
        drawingEngine.cancel();
        layerManager.getLayers().forEach(Layer::dispose);
        historyManager.clear();
    }
}
