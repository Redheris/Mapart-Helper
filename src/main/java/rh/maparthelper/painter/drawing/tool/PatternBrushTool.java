package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.drawing.tool.settings.PatternSettings;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class PatternBrushTool<T extends PixelSurface> extends BrushTool<T> {
    private final PatternSettings pattern;
    private int startX;
    private int startY;

    public PatternBrushTool(PatternSettings pattern, BrushToolSettings brushSettings,
                            LayerManager<T, ? extends Layer<T>> layerManager, Selection selection
    ) {
        super(brushSettings, layerManager, selection);
        this.pattern = pattern;
    }

    @Override
    protected void startDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        this.startX = x;
        this.startY = y;
        super.startDrawing(x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    protected void processDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int xCenter;
        int yCenter;
        if (settings.getThickness() % 2 == 0) {
            xCenter = lineX;
            yCenter = lineY;
        } else {
            xCenter = x;
            yCenter = y;
        }
        Rasterizer.drawLine(
                (x0, y0) -> {
                    int color = pattern.getPatternPixel(x0 + startX, y0 + startY);
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
