package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.DrawingEngine;

public class QuickToolSelector {
    private final DrawingEngine<?> drawingEngine;
    private PainterTool mainPainterTool;
    private boolean inUse = false;

    public QuickToolSelector(DrawingEngine<?> drawingEngine) {
        this.drawingEngine = drawingEngine;
    }

    public void use(PainterTool quickTool) {
        if (inUse) return;
        this.inUse = true;
        this.mainPainterTool = drawingEngine.getSelectedTool();
        drawingEngine.setSelectedTool(quickTool);
    }

    public void release() {
        if (!inUse) return;
        this.drawingEngine.setSelectedTool(mainPainterTool);
        this.inUse = false;
    }

    public boolean isInUse() {
        return inUse;
    }
}
