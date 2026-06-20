package rh.maparthelper.painter.drawing;

import org.jetbrains.annotations.NotNull;
import rh.maparthelper.painter.drawing.tool.BrushTool;
import rh.maparthelper.painter.drawing.tool.PainterTool;
import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.history.HistoryManager;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class DrawingEngine<T extends PixelSurface> {
    protected final LayerManager<T, ? extends Layer<T>> layerManager;
    private final HistoryManager historyManager;
    public final Selection selection;
    private @NotNull PainterTool selectedTool;
    private int mainColor;
    private int secondaryColor;

    private boolean isProcessing;

    public DrawingEngine(LayerManager<T, ? extends Layer<T>> layerManager, HistoryManager historyManager) {
        this.layerManager = layerManager;
        this.historyManager = historyManager;
        this.selection = new Selection();
        this.selectedTool = new BrushTool<>(new BrushToolSettings(), layerManager, selection);
    }

    public void setSelectedTool(@NotNull PainterTool selectedTool) {
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

    public boolean isProcessing() {
        return isProcessing;
    }

    public void start(int x, int y, int lineX, int lineY, boolean inverseColors) {
        if (isProcessing) return;
        isProcessing = true;
        if (inverseColors)
            selectedTool.start(x, y, lineX, lineY, secondaryColor, mainColor);
        else
            selectedTool.start(x, y, lineX, lineY, mainColor, secondaryColor);
    }

    public void process(int x, int y, int lineX, int lineY, boolean inverseColors) {
        if (inverseColors)
            selectedTool.process(x, y, lineX, lineY, secondaryColor, mainColor);
        else
            selectedTool.process(x, y, lineX, lineY, mainColor, secondaryColor);
    }

    public void submit() {
        isProcessing = false;
        historyManager.saveAction(selectedTool.submit());
    }
}
