package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.DrawingContext;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;

public class EyedropperTool implements PainterTool {
    private final LayerManager<?, ?> layerManager;
    private final DrawingEngine<?> drawingEngine;

    public EyedropperTool(LayerManager<?, ?> layerManager, DrawingEngine<?> drawingEngine) {
        this.layerManager = layerManager;
        this.drawingEngine = drawingEngine;
    }

    @Override
    public void start(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        process(drawingContext, x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    public void process(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int color = getTopVisibleColor(x, y);
        if (drawingEngine.isInversedColors()) {
            drawingEngine.setSecondaryColor(color);
        } else {
            drawingEngine.setMainColor(color);
        }
    }

    // TODO: Make a setting option and use also in flood fill tools
    private int getTopVisibleColor(int x, int y) {
        var layers = layerManager.getLayers();
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer<?> layer = layers.get(i);
            if (!layer.isVisible()) continue;
            int color = layer.getSurface().getPixel(x, y);
            if (color != 0) return color;
        }
        return 0;
    }

    @Override
    public HistoryAction submit() {
        return HistoryAction.EMPTY;
    }

    @Override
    public void cancel() {}

    @Override
    public boolean isDrawing() {
        return false;
    }
}
