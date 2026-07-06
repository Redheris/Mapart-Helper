package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.drawing.tool.settings.PatternBehavior;
import rh.maparthelper.painter.drawing.tool.settings.PatternSettings;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class PatternBrushTool<T extends PixelSurface> extends BrushTool<T> implements PatternBehavior {
    private final PatternSettings pattern;
    private int startX;
    private int startY;
    private int startLineX;
    private int startLineY;

    public PatternBrushTool(PatternSettings pattern, BrushToolSettings brushSettings,
                            LayerManager<T, ? extends Layer<T>> layerManager, Selection selection
    ) {
        super(brushSettings, layerManager, selection);
        this.pattern = pattern;
    }

    @Override
    public PatternSettings patternSettings() {
        return pattern;
    }

    @Override
    protected void startDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        this.startX = x;
        this.startY = y;
        this.startLineX = lineX;
        this.startLineY = lineY;
        super.startDrawing(x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    protected void processDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int xCenter, yCenter;
        int patternPivotX, patternPivotY;
        if (settings.getThickness() % 2 == 0) {
            xCenter = lineX;
            yCenter = lineY;
            patternPivotX = startLineX;
            patternPivotY = startLineY;
        } else {
            xCenter = x;
            yCenter = y;
            patternPivotX = startX;
            patternPivotY = startY;
        }
        Rasterizer.drawLine(
                (x0, y0) -> {
                    int color = pattern.getPatternPixel(x0 - patternPivotX + settings.getThickness() / 2, y0 - patternPivotY + settings.getThickness() / 2);
                    if (!pattern.isPlaceTransparent() && color == 0) return;
                    Rasterizer.setPixel(
                            selection, editingLayer.getSurface(), changedPixelsBefore,
                            x0, y0,
                            color
                    );
                },
                changedAreaStepBuffer,
                settings.isCircleShape(),
                settings.getThickness(),
                lastX, lastY,
                xCenter, yCenter
        );
        lastX = xCenter;
        lastY = yCenter;
    }
}
