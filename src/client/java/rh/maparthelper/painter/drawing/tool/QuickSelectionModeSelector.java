package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;

public class QuickSelectionModeSelector {
    private final SelectionToolSettings settings;
    private SelectionToolSettings.SelectionMode originalMode;
    private boolean applied = false;

    public QuickSelectionModeSelector(SelectionToolSettings settings) {
        this.settings = settings;
    }

    public void apply(SelectionToolSettings.SelectionMode mode) {
        if (applied) return;
        this.applied = true;
        this.originalMode = settings.getMode();
        settings.setMode(mode);
    }

    public boolean release() {
        if (!applied) return false;
        this.settings.setMode(originalMode);
        this.applied = false;
        return true;
    }

    public boolean isApplied() {
        return applied;
    }
}
