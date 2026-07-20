package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.DrawingContext;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.history.action.PaintHistoryAction;
import rh.maparthelper.painter.history.action.PaintedPixelsState;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;

public abstract class AbstractDrawingTool<T extends PixelSurface> implements PainterTool {
    private final Rectangle affectedArea;
    protected final LayerManager<T, ? extends Layer<T>> layerManager;
    protected final Selection selection;
    protected PaintedPixelsState paintedPixelsState;
    protected final Rectangle changedAreaStepBuffer;
    protected Layer<T> editingLayer;
    private boolean isDrawing;

    public AbstractDrawingTool(LayerManager<T, ? extends Layer<T>> layerManager, Selection selection) {
        this.affectedArea = new Rectangle();
        this.layerManager = layerManager;
        this.selection = selection;
        this.changedAreaStepBuffer = new Rectangle();
    }

    @Override
    public final void start(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        isDrawing = true;
        paintedPixelsState = new PaintedPixelsState();
        editingLayer = layerManager.getSelectedLayer();

        startDrawing(drawingContext, x, y, lineX, lineY, firstColor, secondColor);
        processDrawing(drawingContext, x, y, lineX, lineY, firstColor, secondColor);

        affectedArea.setBounds(changedAreaStepBuffer);
        editingLayer.markDirty(affectedArea);
    }

    @Override
    public final void process(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (!isDrawing) return;

        processDrawing(drawingContext, x, y, lineX, lineY, firstColor, secondColor);

        affectedArea.add(changedAreaStepBuffer);
        editingLayer.markDirty(changedAreaStepBuffer);
    }

    @Override
    public final HistoryAction submit() {
        if (!isDrawing) {
            return HistoryAction.EMPTY;
        }
        isDrawing = false;
        if (affectedArea.isEmpty() || paintedPixelsState.indices().isEmpty()) {
            return HistoryAction.EMPTY;
        }
        return new PaintHistoryAction<>(editingLayer, affectedArea, paintedPixelsState);
    }

    @Override
    public boolean isDrawing() {
        return isDrawing;
    }

    @Override
    public void cancel() {
        isDrawing = false;
        affectedArea.setBounds(0, 0, 0, 0);
        paintedPixelsState = null;
    }

    protected abstract void startDrawing(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor);

    protected abstract void processDrawing(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor);
}
