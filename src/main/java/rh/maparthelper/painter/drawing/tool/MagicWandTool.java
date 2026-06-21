package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.FloodFillSettings;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.PixelSurface;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class MagicWandTool<T extends PixelSurface> extends AbstractSelectionTool {
    private final FloodFillSettings floodSettings;
    private final LayerManager<T, ? extends Layer<T>> layerManager;
    private int startedWithColor;
    private boolean selected;

    public MagicWandTool(FloodFillSettings floodSettings, SelectionToolSettings settings, Selection selection,
                         LayerManager<T, ? extends Layer<T>> layerManager
    ) {
        super(settings, selection);

        this.floodSettings = floodSettings;
        this.layerManager = layerManager;
    }

    @Override
    protected void startSelecting(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        selected = false;
        startedWithColor = layerManager.getSelectedLayer().getSurface().getPixel(startX, startY);
        process(x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    protected void processSelection(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (x < 0 || y < 0 || x >= maxWidth || y >= maxHeight) {
            cancel();
            return;
        }

        if (floodSettings.isGlobalFill()) {
            globalSelect(x, y);
        } else {
            localSelect(x, y);
        }

        selected = true;
    }

    private void localSelect(int x, int y) {
        PixelSurface surface = layerManager.getSelectedLayer().getSurface();
        int color = surface.getPixel(x, y);

        if (!selected || color != startedWithColor) {
            newPart.clear();
            startedWithColor = color;
            Queue<Integer> unchecked = new ArrayDeque<>();
            Set<Integer> checked = new HashSet<>();
            int id = x + y * maxWidth;

            while (true) {
                int x0 = id % maxWidth;
                int y0 = id / maxWidth;

                if (!checked.contains(id)) {
                    boolean closeEnough = ColorUtils.matches(
                            surface.getPixel(x0, y0), startedWithColor,
                            floodSettings.getTolerance()
                    );
                    if (closeEnough) {
                        newPart.set(id);
                        FloodFillTool.floodFillScanStep(null, unchecked, id, x0, y0, maxWidth, maxHeight);
                    }
                }
                checked.add(id);
                if (unchecked.isEmpty()) break;
                id = unchecked.poll();
            }
        }
    }

    private void globalSelect(int x, int y) {
        PixelSurface surface = layerManager.getSelectedLayer().getSurface();
        int color = surface.getPixel(x, y);

        if (selected && startedWithColor == color) return;
        newPart.clear();
        startedWithColor = color;

        for (int x0 = 0; x0 < surface.getWidth(); x0++) {
            for (int y0 = 0; y0 < surface.getHeight(); y0++) {
                boolean closeEnough = ColorUtils.matches(
                        surface.getPixel(x0, y0), color,
                        floodSettings.getTolerance()
                );
                if (closeEnough) {
                    newPart.set(x0 + y0 * maxWidth);
                }
            }
        }
    }

    @Override
    public HistoryAction submit() {
        selected = false;
        return super.submit();
    }

    @Override
    public void cancel() {
        selected = false;
        super.cancel();
    }
}
