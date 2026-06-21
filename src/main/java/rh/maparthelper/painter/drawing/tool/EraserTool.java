package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class EraserTool<T extends PixelSurface> extends BrushTool<T> {
    public EraserTool(BrushToolSettings settings, LayerManager<T, ? extends Layer<T>> layerManager, Selection selection) {
        super(settings, layerManager, selection);
    }

    @Override
    protected void processDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        super.processDrawing(x, y, lineX, lineY, 0, 0);
    }
}
