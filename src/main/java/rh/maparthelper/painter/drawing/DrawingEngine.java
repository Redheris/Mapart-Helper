package rh.maparthelper.painter.drawing;

import org.jetbrains.annotations.NotNull;
import rh.maparthelper.painter.drawing.tool.HandTool;
import rh.maparthelper.painter.drawing.tool.PainterTool;
import rh.maparthelper.painter.history.HistoryManager;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class DrawingEngine<T extends PixelSurface> {
    protected final LayerManager<T, ? extends Layer<T>> layerManager;
    private final HistoryManager historyManager;
    public final Selection selection;
    public final DrawingContext drawingContext;
    private @NotNull PainterTool selectedTool;
    private int mainColor;
    private int secondaryColor;

    private boolean isProcessing;
    private boolean isInversedColors;

    public DrawingEngine(LayerManager<T, ? extends Layer<T>> layerManager, HistoryManager historyManager) {
        this.layerManager = layerManager;
        this.historyManager = historyManager;
        this.selection = new Selection();
        selection.setSize(layerManager.getSelectedLayer().getSurface());
        this.drawingContext = new DrawingContext(layerManager.getWidth(), layerManager.getHeight());
        this.selectedTool = new HandTool();
    }

    public void setSelectedTool(@NotNull PainterTool selectedTool) {
        this.submit();
        this.selectedTool = selectedTool;
    }

    public @NotNull PainterTool getSelectedTool() {
        return selectedTool;
    }

    public void setMainColor(int mainColor) {
        this.mainColor = mainColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public int getMainColor() {
        return mainColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public boolean isInversedColors() {
        return isInversedColors;
    }

    public void start(int x, int y, int lineX, int lineY, boolean inverseColors) {
        if (isProcessing) return;
        drawingContext.beginStamp();
        isProcessing = true;
        isInversedColors = inverseColors;
        if (inverseColors)
            selectedTool.start(drawingContext, x, y, lineX, lineY, secondaryColor, mainColor);
        else
            selectedTool.start(drawingContext, x, y, lineX, lineY, mainColor, secondaryColor);
    }

    public void process(int x, int y, int lineX, int lineY, boolean inverseColors) {
        if (!isProcessing) return;
        if (inverseColors)
            selectedTool.process(drawingContext,x, y, lineX, lineY, secondaryColor, mainColor);
        else
            selectedTool.process(drawingContext,x, y, lineX, lineY, mainColor, secondaryColor);
    }

    public void submit() {
        if (!isProcessing) return;
        isProcessing = false;
        isInversedColors = false;
        historyManager.saveAction(selectedTool.submit());
    }

    public void cancel() {
        selectedTool.cancel();
        isProcessing = false;
        isInversedColors = false;
    }
}
