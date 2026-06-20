package rh.maparthelper.painter.layer;

import org.jetbrains.annotations.NotNull;
import rh.maparthelper.painter.surface.PixelSurface;

import java.awt.*;

public abstract class Layer<T extends PixelSurface> {
    private final PixelSurface surface;
    private final Rectangle surfaceArea;
    private boolean dirty;
    private Rectangle dirtyArea;

    private float alpha;
    private boolean visible;

    public Layer(@NotNull T surface) {
        this.surface = surface;
        this.surfaceArea = new Rectangle(0, 0, surface.getWidth(), surface.getHeight());
        this.alpha = 1.0f;
        this.visible = true;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0f, 1f);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public PixelSurface getSurface() {
        return surface;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty(Rectangle dirtyArea) {
        if (dirtyArea == null || dirtyArea.isEmpty()) return;

        Rectangle clipped = this.surfaceArea.intersection(dirtyArea);
        if (this.dirty) {
            this.dirtyArea.add(clipped);
        } else {
            this.dirtyArea = clipped;
        }

        if (!this.dirtyArea.isEmpty()) {
            this.dirty = true;
        }
    }

    public final void upload() {
        if (dirty) {
            uploadDirtyArea(dirtyArea);
            dirty = false;
        }
    }

    protected abstract void uploadDirtyArea(Rectangle dirtyArea);
}
