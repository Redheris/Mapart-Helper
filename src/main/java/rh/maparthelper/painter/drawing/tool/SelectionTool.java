package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.util.BitSetUtils;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.history.action.SelectionHistoryAction;

import java.util.BitSet;

public class SelectionTool implements PainterTool {
    private final Selection selection;
    private final BitSet newPart;
    private final int maxWidth;
    private final int maxHeight;
    private boolean isSelecting;
    private BitSet before;
    private int startX;
    private int startY;

    private final SelectionToolSettings settings;

    public SelectionTool(SelectionToolSettings settings, Selection selection) {
        this.selection = selection;
        this.settings = settings;
        this.maxWidth = selection.getWidth();
        this.maxHeight = selection.getHeight();
        this.newPart = new BitSet(maxWidth * maxHeight);
    }

    @Override
    public void start(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        isSelecting = true;
        before = selection.getSelectionMask();
        newPart.clear();
        if (settings.getMode() == SelectionToolSettings.SelectionMode.REPLACE) {
            selection.clear();
        }
        startX = x;
        startY = y;
    }

    @Override
    public void process(int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (!isSelecting) return;
        BitSetUtils.clearAndFill(newPart, maxWidth, maxHeight, startX, startY, x, y);
        BitSet result = (BitSet) before.clone();

        switch (settings.getMode()) {
            case REPLACE -> {
                selection.setSelectionMask(newPart);
                return;
            }
            case CONCAT -> result.or(newPart);
            case SUBTRACT -> result.andNot(newPart);
            case AND -> result.and(newPart);
            case XOR -> result.xor(newPart);
        }
        selection.setSelectionMask(result);
    }

    @Override
    public HistoryAction submit() {
        this.isSelecting = false;
        BitSet after = selection.getSelectionMask();
        selection.setActive(!after.isEmpty());

        if (before.equals(after)) {
            return HistoryAction.EMPTY;
        }
        return new SelectionHistoryAction(selection, before, after);
    }

    @Override
    public void cancel() {
        isSelecting = false;
    }

    @Override
    public boolean isDrawing() {
        return false;
    }
}
