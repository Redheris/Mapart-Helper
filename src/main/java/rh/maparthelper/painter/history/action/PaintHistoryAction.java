package rh.maparthelper.painter.history.action;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;

public class PaintHistoryAction<T extends PixelSurface> implements HistoryAction {
    private final Layer<T> layer;
    private final PixelSurface surface;
    private final Rectangle affectedArea;
    private final Int2IntMap before;
    private final Int2IntMap after;

    public PaintHistoryAction(Layer<T> layer, Rectangle affectedArea, Int2IntMap changedPixelsBefore) {
        this.layer = layer;
        this.surface = layer.getSurface();
        this.affectedArea = affectedArea.getBounds();
        this.before = new Int2IntOpenHashMap(changedPixelsBefore);
        this.after = new Int2IntOpenHashMap(changedPixelsBefore.size());

        before.forEach((pixelId, oldColor) -> {
            int pixelX = pixelId % surface.getWidth();
            int pixelY = pixelId / surface.getWidth();
            after.put((int) pixelId, surface.getPixel(pixelX, pixelY));
        });
    }

    @Override
    public void undo() {
        apply(before);
    }

    @Override
    public void redo() {
        apply(after);
    }

    private void apply(Int2IntMap pixels) {
        int width = surface.getWidth();

        pixels.forEach((id, color) -> {
            int pixelId = id;
            int pixelX = pixelId % width;
            int pixelY = pixelId / width;
            surface.setPixel(pixelX, pixelY, color);
        });

        layer.markDirty(affectedArea);
    }
}
