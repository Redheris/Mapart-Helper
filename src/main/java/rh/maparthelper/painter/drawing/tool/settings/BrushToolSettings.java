package rh.maparthelper.painter.drawing.tool.settings;

public class BrushToolSettings {
    private int thickness = 3;
    private boolean circleShape = true;

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public boolean isCircleShape() {
        return circleShape;
    }

    public void setCircleShape(boolean circleShape) {
        this.circleShape = circleShape;
    }

    public void update(BrushToolSettings settings) {
        this.thickness = settings.thickness;
        this.circleShape = settings.circleShape;
    }
}
