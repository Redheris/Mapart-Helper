package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.BrushBehavior;
import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class BrushTool<T extends PixelSurface> extends AbstractDrawingTool<T> implements BrushBehavior {
    protected final BrushToolSettings settings;
    protected int lastX;
    protected int lastY;

    public BrushTool(BrushToolSettings settings, LayerManager<T, ? extends Layer<T>> layerManager, Selection selection) {
        super(layerManager, selection);
        this.settings = settings;
    }

    @Override
    public BrushToolSettings brushToolSettings() {
        return settings;
    }

    @Override
    protected void startDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int xCenter = x;
        int yCenter = y;
        if (settings.getThickness() % 2 == 0) {
            xCenter = lineX;
            yCenter = lineY;
        }
        lastX = xCenter;
        lastY = yCenter;
    }

    @Override
    protected void processDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        int xCenter = x;
        int yCenter = y;
        if (settings.getThickness() % 2 == 0) {
            xCenter = lineX;
            yCenter = lineY;
        }
        Rasterizer.drawLine(
                Rasterizer.drawingPixelConsumer(selection, editingLayer.getSurface(), changedPixelsBefore, firstColor),
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
