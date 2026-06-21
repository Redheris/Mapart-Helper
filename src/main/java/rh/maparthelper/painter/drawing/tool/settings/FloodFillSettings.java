package rh.maparthelper.painter.drawing.tool.settings;

public class FloodFillSettings {
    private float tolerance = 0;
    private boolean globalFill = false;

    public float getTolerance() {
        return tolerance;
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isGlobalFill() {
        return globalFill;
    }

    public void setGlobalFill(boolean globalFill) {
        this.globalFill = globalFill;
    }
}
