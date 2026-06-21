package rh.maparthelper.painter.drawing.tool;

import org.jetbrains.annotations.Nullable;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.FloodFillSettings;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class FloodFillTool<T extends PixelSurface> extends AbstractDrawingTool<T> {
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
    protected void startDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (x < 0 || y < 0 || x >= maxWidth || y >= maxHeight) {
            cancel();
            return;
        }

        minX = maxWidth - 1;
        maxX = 0;
        minY = maxHeight - 1;
        maxY = 0;

        if (settings.isGlobalFill()) {
            globalFill(x, y, firstColor);
        } else {
            localFill(x, y, firstColor);
        }

        changedAreaStepBuffer.setBounds(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void localFill(int x, int y, int color) {
        PixelSurface surface = layerManager.getSelectedLayer().getSurface();

        int startColor = surface.getPixel(x, y);
        Queue<Integer> unchecked = new ArrayDeque<>();
        Set<Integer> checked = new HashSet<>();
        int id = x + y * maxWidth;

        do {
            int x0 = id % maxWidth;
            int y0 = id / maxWidth;

            if (!checked.contains(id)) {
                boolean closeEnough = ColorUtils.matches(
                        surface.getPixel(x0, y0), startColor,
                        settings.getTolerance()
                );
                if (closeEnough) {
                    minX = Math.min(minX, x0);
                    maxX = Math.max(maxX, x0);
                    minY = Math.min(minY, y0);
                    maxY = Math.max(maxY, y0);
                    Rasterizer.setPixel(selection, surface, changedPixelsBefore, x0, y0, color);
                    floodFillScanStep(selection, unchecked, id, x0, y0, maxWidth, maxHeight);
                }
            }

            checked.add(id);
            if (unchecked.isEmpty()) break;
            id = unchecked.poll();
        } while (!unchecked.isEmpty());
    }

    private void globalFill(int x, int y, int color) {
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
                    Rasterizer.setPixel(selection, surface, changedPixelsBefore, x0, y0, color);
                }
            }
        }
    }

    static void floodFillScanStep(@Nullable Selection selection, Queue<Integer> unchecked, int id, int x0, int y0, int maxWidth, int maxHeight) {
        if (selection != null && !selection.allows(x0, y0)) return;
        if (x0 > 0) unchecked.add(id - 1);
        if (x0 < maxWidth - 1) unchecked.add(id + 1);
        if (y0 > 0) unchecked.add(id - maxWidth);
        if (y0 < maxHeight - 1) unchecked.add(id + maxWidth);
    }

    @Override
    protected void processDrawing(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {}
}
