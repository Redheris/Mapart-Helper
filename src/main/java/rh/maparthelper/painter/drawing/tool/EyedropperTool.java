package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class EyedropperTool implements PainterTool {
    private final LayerManager<?, ?> layerManager;
    private final DrawingEngine<?> drawingEngine;

    public EyedropperTool(LayerManager<?, ?> layerManager, DrawingEngine<?> drawingEngine) {
        this.layerManager = layerManager;
        this.drawingEngine = drawingEngine;
    }

    @Override
    public void start(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        process(x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    public void process(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        PixelSurface surface = layerManager.getSelectedLayer().getSurface();
        if (!surface.containsPixel(x, y)) return;

        int color = surface.getPixel(x, y);
        if (drawingEngine.isInversedColors()) {
            drawingEngine.setSecondaryColor(color);
        } else {
            drawingEngine.setMainColor(color);
        }
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
