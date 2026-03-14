package rh.maparthelper.mapart;

public class MapSegment {
    private final ColorsCounter colorsCounter;
    private final int mapX;
    private final int mapY;

    public MapSegment(int mapX, int mapY) {
        this.colorsCounter = new ColorsCounter();
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public static MapSegment[] initSegmentsForSize(int width, int height) {
        MapSegment[] segments = new MapSegment[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                segments[y * width + x] = new MapSegment(x, y);
            }
        }
        return segments;
    }

    public ColorsCounter getColorsCounter() {
        return colorsCounter;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }
}
