package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;
import rh.maparthelper.painter.util.BitSetUtils;

public class RectangleSelectionTool extends AbstractSelectionTool {

    public RectangleSelectionTool(SelectionToolSettings settings, Selection selection) {
        super(settings, selection);
    }

    @Override
    protected void startSelecting(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {}

    @Override
    protected void processSelection(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        BitSetUtils.clearAndFill(newPart, maxWidth, maxHeight, startX, startY, x, y);
    }
}
