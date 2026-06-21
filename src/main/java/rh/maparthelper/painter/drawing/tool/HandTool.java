package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.history.action.HistoryAction;

/**
 * Empty painter tool tht makes no changes to either the surface or the document
 */
public class HandTool implements PainterTool {

    @Override
    public void start(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {}

    @Override
    public void process(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {}

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
