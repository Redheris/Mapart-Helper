package rh.maparthelper.painter.drawing.tool;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.painter.drawing.DrawingContext;
import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.FloodFillBehavior;
import rh.maparthelper.painter.drawing.tool.settings.FloodFillSettings;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

public class FloodFillTool<T extends PixelSurface> extends AbstractDrawingTool<T> implements FloodFillBehavior {
    private final FloodFillSettings settings;

    protected final int maxWidth;
    protected final int maxHeight;

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;

    public FloodFillTool(FloodFillSettings settings, LayerManager<T, ? extends Layer<T>> layerManager, Selection selection) {
        super(layerManager, selection);
        this.settings = settings;
        this.maxWidth = selection.getWidth();
        this.maxHeight = selection.getHeight();
    }

    @Override
    public FloodFillSettings floodFillSettings() {
        return settings;
    }

    @Override
    protected void startDrawing(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (x < 0 || y < 0 || x >= maxWidth || y >= maxHeight) {
            cancel();
            return;
        }

        minX = maxWidth - 1;
        maxX = 0;
        minY = maxHeight - 1;
        maxY = 0;

        if (settings.isGlobalFill()) {
            globalFill(drawingContext, x, y, firstColor);
        } else {
            localFill(drawingContext, x, y, firstColor);
        }

        changedAreaStepBuffer.setBounds(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void localFill(DrawingContext drawingContext, int x, int y, int color) {
        PixelSurface surface = layerManager.getSelectedLayer().getSurface();

        int startColor = surface.getPixel(x, y);
        IntArrayFIFOQueue unchecked = new IntArrayFIFOQueue();
        boolean[] checked = new boolean[maxWidth * maxHeight];
        int id = x + y * maxWidth;

        do {
            int x0 = id % maxWidth;
            int y0 = id / maxWidth;

            if (!checked[id]) {
                boolean closeEnough = ColorUtils.matches(
                        surface.getPixel(x0, y0), startColor,
                        settings.getTolerance()
                );
                if (closeEnough) {
                    minX = Math.min(minX, x0);
                    maxX = Math.max(maxX, x0);
                    minY = Math.min(minY, y0);
                    maxY = Math.max(maxY, y0);
                    Rasterizer.setPixel(drawingContext, selection, surface, paintedPixelsState, x0, y0, color);
                    floodFillScanStep(selection, unchecked, id, x0, y0, maxWidth, maxHeight);
                }
            }

            checked[id] = true;
            if (unchecked.isEmpty()) break;
            id = unchecked.dequeueInt();
        } while (!unchecked.isEmpty());
    }

    private void globalFill(DrawingContext drawingContext, int x, int y, int color) {
        PixelSurface surface = layerManager.getSelectedLayer().getSurface();
        int startColor = surface.getPixel(x, y);

        for (int x0 = 0; x0 < surface.getWidth(); x0++) {
            for (int y0 = 0; y0 < surface.getHeight(); y0++) {
                boolean closeEnough = ColorUtils.matches(
                        surface.getPixel(x0, y0), startColor,
                        settings.getTolerance()
                );
                if (closeEnough) {
                    minX = Math.min(minX, x0);
                    maxX = Math.max(maxX, x0);
                    minY = Math.min(minY, y0);
                    maxY = Math.max(maxY, y0);
                    Rasterizer.setPixel(drawingContext, selection, surface, paintedPixelsState, x0, y0, color);
                }
            }
        }
    }

    static void floodFillScanStep(@Nullable Selection selection, IntArrayFIFOQueue unchecked, int id, int x0, int y0, int maxWidth, int maxHeight) {
        if (selection != null && !selection.allows(x0, y0)) return;
        if (x0 > 0) unchecked.enqueue(id - 1);
        if (x0 < maxWidth - 1) unchecked.enqueue(id + 1);
        if (y0 > 0) unchecked.enqueue(id - maxWidth);
        if (y0 < maxHeight - 1) unchecked.enqueue(id + maxWidth);
    }

    @Override
    protected void processDrawing(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {}
}
