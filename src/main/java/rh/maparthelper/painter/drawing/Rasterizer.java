package rh.maparthelper.painter.drawing;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;
import java.util.function.BiConsumer;

public class Rasterizer {

    public static BiConsumer<Integer, Integer> drawingPixelConsumer(Selection selection, PixelSurface surface, Int2IntMap changedPixels, int color) {
        return (x, y) -> setPixel(selection, surface, changedPixels, x, y, color);
    }

    public static void setPixel(Selection selection, PixelSurface surface, Int2IntMap changedPixels,
                                int x, int y, int color
    ) {
        if (!selection.allows(x, y) || changedPixels.containsKey(x + y * surface.getWidth()))
            return;

        int oldColor = surface.getPixel(x, y);
        if (surface.setPixel(x, y, color)) {
            changedPixels.putIfAbsent(x + y * surface.getWidth(), oldColor);
        }
    }


    public static void drawLine(BiConsumer<Integer, Integer> pixelConsumer,
                                @Nullable Rectangle changedArea,
                                boolean circleShape,
                                int thickness,
                                int x0, int y0, int x1, int y1
    ) {
        int minX = Math.min(x0, x1);
        int minY = Math.min(y0, y1);
        int width = Math.abs(x1 - x0) + 1;
        int height = Math.abs(y1 - y0) + 1;

        int deltaX = Math.abs(x1 - x0);
        int deltaY = Math.abs(y1 - y0);
        int signX = x0 < x1 ? 1 : -1;
        int signY = y0 < y1 ? 1 : -1;
        int d = deltaX - deltaY;

        drawFigureFromCenter(pixelConsumer, changedArea, circleShape, thickness, x1, y1);
        while (x0 != x1 || y0 != y1) {
            drawFigureFromCenter(pixelConsumer, changedArea, circleShape, thickness, x0, y0);

            int d2 = d * 2;
            if (d2 > -deltaY) {
                d -= deltaY;
                x0 += signX;
            }
            if (d2 < deltaX) {
                d += deltaX;
                y0 += signY;
            }
        }
        if (changedArea != null) {
            changedArea.setBounds(minX - thickness, minY - thickness, width + thickness * 2, height + thickness * 2);
        }
    }

    public static void drawFigureFromCenter(BiConsumer<Integer, Integer> pixelConsumer,
                                            @Nullable Rectangle changedArea,
                                            boolean circleShape,
                                            int thickness,
                                            int xCenter, int yCenter
    ) {
        if (circleShape)
            drawCircle(pixelConsumer, changedArea, xCenter, yCenter, thickness, true);
        else {
            int x = xCenter - thickness / 2;
            int y = yCenter - thickness / 2;
            drawRect(
                    pixelConsumer, changedArea,
                    x, y,
                    x + thickness - 1, y + thickness - 1,
                    true
            );
        }
    }

    public static void drawRect(BiConsumer<Integer, Integer> pixelConsumer, @Nullable Rectangle changedArea,
                                int x0, int y0, int x1, int y1, boolean fill
    ) {
        int minX = Math.min(x0, x1);
        int minY = Math.min(y0, y1);
        int maxX = Math.max(x0, x1);
        int maxY = Math.max(y0, y1);

        if (fill) {
            fillRectByCoords(pixelConsumer, minX, minY, maxX, maxY);
        } else {
            fillRectByCoords(pixelConsumer, minX, minY, maxX, minY);
            fillRectByCoords(pixelConsumer, minX, minY, minX, maxY);
            fillRectByCoords(pixelConsumer, maxX, maxY, maxX, minY);
            fillRectByCoords(pixelConsumer, maxX, maxY, minX, maxY);
        }

        if (changedArea != null) {
            changedArea.setBounds(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }
    }

    public static void drawCircle(BiConsumer<Integer, Integer> pixelConsumer, @Nullable Rectangle changedArea,
                                  int x0, int y0, int diameter, boolean fill
    ) {
        int dx = 0;
        int dy = diameter / 2;
        int delta = 3 - 2 * dy;

        int evenLenError = diameter % 2 == 0 ? 1 : 0;

        if (dy == 0) {
            pixelConsumer.accept(x0, y0);
            if (changedArea != null) changedArea.setBounds(x0, y0, 1, 1);
            return;
        }

        while (dx <= dy) {
            if (fill) {
                fillRectBySize(pixelConsumer, x0 - dx, y0 + dy - evenLenError, dx * 2 + 1 - evenLenError, 1);
                fillRectBySize(pixelConsumer, x0 - dx, y0 - dy, dx * 2 + 1 - evenLenError, 1);
                fillRectBySize(pixelConsumer, x0 - dy, y0 + dx - evenLenError, dy * 2 + 1 - evenLenError, 1);
                fillRectBySize(pixelConsumer, x0 - dy, y0 - dx, dy * 2 + 1 - evenLenError, 1);
            } else {
                drawCircleFragment(pixelConsumer, x0, y0, dx, dy);
                drawCircleFragment(pixelConsumer, x0, y0, dy, dx);
            }

            delta += delta < 0 ? 4 * dx + 6 : 4 * (dx - dy--) + 10;
            ++dx;
        }

        if (changedArea != null) {
            changedArea.setBounds(x0 - diameter, y0 - diameter, 2 * diameter + 1, 2 * diameter + 1);
        }
    }

    private static void drawCircleFragment(BiConsumer<Integer, Integer> pixelConsumer,
                                           int x0, int y0, int dx, int dy
    ) {
        pixelConsumer.accept(x0 + dx, y0 + dy);
        pixelConsumer.accept(x0 + dx, y0 - dy);
        pixelConsumer.accept(x0 - dx, y0 + dy);
        pixelConsumer.accept(x0 - dx, y0 - dy);
    }

    private static void fillRectByCoords(BiConsumer<Integer, Integer> pixelConsumer, int x0, int y0, int x1, int y1) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                pixelConsumer.accept(x, y);
            }
        }
    }

    private static void fillRectBySize(BiConsumer<Integer, Integer> pixelConsumer, int x, int y, int width, int height) {
        fillRectByCoords(pixelConsumer, x, y, x + width - 1, y + height - 1);
    }
}
