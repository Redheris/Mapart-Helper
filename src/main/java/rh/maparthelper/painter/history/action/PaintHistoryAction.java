package rh.maparthelper.painter.history.action;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;

public class PaintHistoryAction<T extends PixelSurface> implements HistoryAction {
    private final Layer<T> layer;
    private final PixelSurface surface;
    private final Rectangle affectedArea;
    private final PaintedPixelsState paintedPixels;

    public PaintHistoryAction(Layer<T> layer, Rectangle affectedArea, PaintedPixelsState paintedPixels) {
        this.layer = layer;
        this.surface = layer.getSurface();
        this.affectedArea = affectedArea.getBounds();
        this.paintedPixels = paintedPixels;
    }

    @Override
    public HistoryActionType type() {
        return HistoryActionType.DRAWING;
    }

    @Override
    public void undo() {
        apply(paintedPixels.before());
    }

    @Override
    public void redo() {
        apply(paintedPixels.after());
    }

    private void apply(IntArrayList pixels) {
        int width = surface.getWidth();

        for (int i = 0; i < paintedPixels.indices().size(); i++) {
            int pixelId = paintedPixels.indices().getInt(i);
            int pixelX = pixelId % width;
            int pixelY = pixelId / width;
            surface.setPixel(pixelX, pixelY, pixels.getInt(i));
        }
        layer.markDirty(affectedArea);
    }
}
