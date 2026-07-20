package rh.maparthelper.painter.drawing.tool.settings;

public class SelectionToolSettings {
    private SelectionMode mode = SelectionMode.REPLACE;

    public SelectionMode getMode() {
        return mode;
    }

    public void setMode(SelectionMode mode) {
        this.mode = mode;
    }

    public void update(SelectionToolSettings settings) {
        this.mode = settings.mode;
    }

    public enum SelectionMode {
        REPLACE,
        CONCAT,
        SUBTRACT,
        AND,
        XOR
    }
}
