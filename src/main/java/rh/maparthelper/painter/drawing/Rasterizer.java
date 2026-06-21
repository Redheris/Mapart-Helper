package rh.maparthelper.painter.drawing;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;

public class Rasterizer {

    public static void drawLine(Selection selection, PixelSurface surface,
                                Rectangle changedArea, Int2IntMap changedPixels,
                                boolean circleShape,
                                int thickness,
                                int x0, int y0, int x1, int y1, int color
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

        drawFigureFromCenter(selection, surface, changedArea, changedPixels, circleShape, thickness, x1, y1, color);
        while (x0 != x1 || y0 != y1) {
            drawFigureFromCenter(selection, surface, changedArea, changedPixels, circleShape, thickness, x0, y0, color);

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
        changedArea.setBounds(minX - thickness, minY - thickness, width + thickness * 2, height + thickness * 2);
    }

    private static void drawFigureFromCenter(Selection selection, PixelSurface surface,
                                             Rectangle changedArea, Int2IntMap changedPixels,
                                             boolean circleShape,
                                             int thickness,
                                             int xCenter, int yCenter, int color
    ) {
        if (circleShape)
            drawCircle(selection, surface, changedArea, changedPixels, xCenter, yCenter, thickness, color, true);
        else {
            int x = xCenter - thickness / 2;
            int y = yCenter - thickness / 2;
            drawRect(
                    selection, surface, changedArea, changedPixels,
                    x, y,
                    x + thickness - 1, y + thickness - 1,
                    color, true
            );
        }
    }

    public static void drawRect(Selection selection, PixelSurface surface, Rectangle changedArea, Int2IntMap changedPixels,
                                int x0, int y0, int x1, int y1, int color, boolean fill
    ) {
        int minX = Math.min(x0, x1);
        int minY = Math.min(y0, y1);
        int maxX = Math.max(x0, x1);
        int maxY = Math.max(y0, y1);

        if (fill) {
            fillRectByCoords(selection, surface, changedPixels, minX, minY, maxX, maxY, color);
        } else {
            fillRectByCoords(selection, surface, changedPixels, minX, minY, maxX, minY, color);
            fillRectByCoords(selection, surface, changedPixels, minX, minY, minX, maxY, color);
            fillRectByCoords(selection, surface, changedPixels, maxX, maxY, maxX, minY, color);
            fillRectByCoords(selection, surface, changedPixels, maxX, maxY, minX, maxY, color);
        }

        changedArea.setBounds(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public static void drawCircle(Selection selection, PixelSurface surface, Rectangle changedArea, Int2IntMap changedPixels,
                                  int x0, int y0, int diameter, int color, boolean fill
    ) {
        int dx = 0;
        int dy = diameter / 2;
        int delta = 3 - 2 * dy;

        int evenLenError = diameter % 2 == 0 ? 1 : 0;

        if (dy == 0) {
            setPixel(selection, surface, changedPixels, x0, y0, color);
            changedArea.setBounds(x0, y0, 1, 1);
            return;
        }

        while (dx <= dy) {
            if (fill) {
                fillRectBySize(selection, surface, changedPixels, x0 - dx, y0 + dy - evenLenError, dx * 2 + 1 - evenLenError, 1, color);
                fillRectBySize(selection, surface, changedPixels, x0 - dx, y0 - dy, dx * 2 + 1 - evenLenError, 1, color);
                fillRectBySize(selection, surface, changedPixels, x0 - dy, y0 + dx - evenLenError, dy * 2 + 1 - evenLenError, 1, color);
                fillRectBySize(selection, surface, changedPixels, x0 - dy, y0 - dx, dy * 2 + 1 - evenLenError, 1, color);
            } else {
                drawCircleFragment(selection, surface, changedPixels, x0, y0, dx, dy, color);
                drawCircleFragment(selection, surface, changedPixels, x0, y0, dy, dx, color);
            }

            delta += delta < 0 ? 4 * dx + 6 : 4 * (dx - dy--) + 10;
            ++dx;
        }

        changedArea.setBounds(x0 - diameter, y0 - diameter, 2 * diameter + 1, 2 * diameter + 1);
    }

    private static void drawCircleFragment(Selection selection, PixelSurface surface, Int2IntMap changedPixels,
                                           int x0, int y0, int dx, int dy, int color
    ) {
        setPixel(selection, surface, changedPixels, x0 + dx, y0 + dy, color);
        setPixel(selection, surface, changedPixels, x0 + dx, y0 - dy, color);
        setPixel(selection, surface, changedPixels, x0 - dx, y0 + dy, color);
        setPixel(selection, surface, changedPixels, x0 - dx, y0 - dy, color);
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

    private static void fillRectByCoords(Selection selection, PixelSurface surface, Int2IntMap changedPixels,
                                         int x0, int y0, int x1, int y1, int color
    ) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                setPixel(selection, surface, changedPixels, x, y, color);
            }
        }
    }

    private static void fillRectBySize(Selection selection, PixelSurface surface, Int2IntMap changedPixels,
                                       int x, int y, int width, int height, int color
    ) {
        fillRectByCoords(selection, surface, changedPixels, x, y, x + width - 1, y + height - 1, color);
    }
}
