package rh.maparthelper.painter.drawing.tool;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.history.action.PaintHistoryAction;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;

public abstract class AbstractDrawingTool<T extends PixelSurface> implements PainterTool {
    private final Rectangle affectedArea;
    private final LayerManager<T, ? extends Layer<T>> layerManager;
    protected final Selection selection;
    protected final Int2IntMap changedPixelsBefore;
    protected final Rectangle changedAreaStepBuffer;
    protected Layer<T> editingLayer;
    private boolean isDrawing;

    public AbstractDrawingTool(LayerManager<T, ? extends Layer<T>> layerManager, Selection selection) {
        this.affectedArea = new Rectangle();
        this.layerManager = layerManager;
        this.selection = selection;
        this.changedPixelsBefore = new Int2IntOpenHashMap();
        this.changedAreaStepBuffer = new Rectangle();
    }

    @Override
    public final void start(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        isDrawing = true;
        changedPixelsBefore.clear();
        editingLayer = layerManager.getSelectedLayer();

        startDrawing(x, y, lineX, lineY, firstColor, secondColor);
        processDrawing(x, y, lineX, lineY, firstColor, secondColor);

        affectedArea.setBounds(changedAreaStepBuffer);
        editingLayer.markDirty(affectedArea);
    }

    @Override
    public final void process(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (!isDrawing) return;

        processDrawing(x, y, lineX, lineY, firstColor, secondColor);

        affectedArea.add(changedAreaStepBuffer);
        editingLayer.markDirty(changedAreaStepBuffer);
    }

    @Override
    public final HistoryAction submit() {
        if (!isDrawing) {
            return HistoryAction.EMPTY;
        }
        isDrawing = false;
        if (affectedArea.isEmpty() || changedPixelsBefore.isEmpty()) {
            return HistoryAction.EMPTY;
        }
        return new PaintHistoryAction<>(editingLayer, affectedArea, changedPixelsBefore);
    }

    @Override
    public boolean isDrawing() {
        return isDrawing;
    }

    @Override
    public void cancel() {
        isDrawing = false;
        affectedArea.setBounds(0, 0, 0, 0);
        changedPixelsBefore.clear();
    }

    protected abstract void startDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor);

    protected abstract void processDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor);
}
