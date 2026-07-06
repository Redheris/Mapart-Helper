package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.BrushBehavior;
import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;

public class SelectionBrushTool extends AbstractSelectionTool implements BrushBehavior {
    private final BrushToolSettings brushSettings;
    private int lastX;
    private int lastY;

    public SelectionBrushTool(BrushToolSettings brushSettings, SelectionToolSettings settings, Selection selection) {
        super(settings, selection);
        this.brushSettings = brushSettings;
    }

    @Override
    public BrushToolSettings brushToolSettings() {
        return brushSettings;
    }

    @Override
    protected void startSelecting(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int xCenter = x;
        int yCenter = y;
        if (brushSettings.getThickness() % 2 == 0) {
            xCenter = lineX;
            yCenter = lineY;
        }
        lastX = xCenter;
        lastY = yCenter;
        process(x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    protected void processSelection(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int xCenter = x;
        int yCenter = y;
        if (brushSettings.getThickness() % 2 == 0) {
            xCenter = lineX;
            yCenter = lineY;
        }
        Rasterizer.drawLine(
                (x0, y0) -> {
                    int id = x0 + y0 * maxWidth;
                    if (x0 >= 0 && y0 >= 0 && x0 < maxWidth && y0 < maxHeight) {
                        newPart.set(id);
                    }
                },
                null,
                brushSettings.isCircleShape(),
                brushSettings.getThickness(),
                lastX, lastY,
                xCenter, yCenter
        );
        lastX = xCenter;
        lastY = yCenter;
    }
}
