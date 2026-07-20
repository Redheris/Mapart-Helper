package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.tool.settings.BrushToolSettings;
import rh.maparthelper.painter.drawing.tool.settings.FloodFillSettings;
import rh.maparthelper.painter.drawing.tool.settings.PatternSettings;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;

public class ToolSettingsProvider {
    private final static ToolSettingsProvider INSTANCE = new ToolSettingsProvider();

    public final BrushToolSettings BRUSH = new BrushToolSettings();
    public final SelectionToolSettings SELECTION = new SelectionToolSettings();
    public final FloodFillSettings FLOOD_FILL = new FloodFillSettings();
    public final PatternSettings PATTERN = new PatternSettings();

    private ToolSettingsProvider() {}

    public static ToolSettingsProvider getInstance() {
        return INSTANCE;
    }

    public static void patchInstance(ToolSettingsProvider patch) {
        INSTANCE.BRUSH.update(patch.BRUSH);
        INSTANCE.SELECTION.update(patch.SELECTION);
        INSTANCE.FLOOD_FILL.update(patch.FLOOD_FILL);
        INSTANCE.PATTERN.update(patch.PATTERN);
    }
}
