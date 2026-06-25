package rh.maparthelper.painter.drawing.tool.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternSettings {
    private int width = 4;
    private int height = 4;
    private boolean placeTransparent = false;
    private List<Integer> pattern = new ArrayList<>(Collections.nCopies(16, 0));

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPatternSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.pattern = new ArrayList<>(Collections.nCopies(width * height, 0));
        clearPattern();
    }

    public boolean isPlaceTransparent() {
        return placeTransparent;
    }

    public void setPlaceTransparent(boolean placeTransparent) {
        this.placeTransparent = placeTransparent;
    }

    public int getPatternPixel(int x, int y) {
        return this.pattern.get(Math.abs(x) % width + Math.abs(y) % height * width);
    }

    public void setPatternPixel(int x, int y, int color) {
        this.pattern.set(x +  y * width, color);
    }

    public void clearPattern() {
        this.pattern = new ArrayList<>(Collections.nCopies(width * height, 0));
    }

    public void update(PatternSettings patch) {
        this.pattern = patch.pattern;
        this.width = patch.width;
        this.height = patch.height;
    }
}
