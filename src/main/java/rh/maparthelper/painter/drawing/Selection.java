package rh.maparthelper.painter.drawing;

import rh.maparthelper.painter.surface.PixelSurface;

import java.util.BitSet;

public class Selection {
    private int width;
    private int height;
    private final BitSet selectionMask = new BitSet();
    private boolean active;

    public void setSize(PixelSurface surface) {
        this.width = surface.getWidth();
        this.height = surface.getHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean allows(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return !active || contains(x, y);
    }

    private boolean contains(int x, int y) {
        return selectionMask.get(x + y * width);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BitSet getSelectionMask() {
        return (BitSet) selectionMask.clone();
    }

    public void clear() {
        this.selectionMask.clear();
        this.active = false;
    }

    public void setSelectionMask(BitSet bitMask) {
        this.selectionMask.clear();
        this.selectionMask.or(bitMask);
        this.active = !bitMask.isEmpty();
    }
}
