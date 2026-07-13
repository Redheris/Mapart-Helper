package rh.maparthelper.painter.drawing.tool;

import org.jetbrains.annotations.Nullable;
import rh.maparthelper.painter.drawing.DrawingContext;
import rh.maparthelper.painter.drawing.Selection;
import rh.maparthelper.painter.drawing.tool.settings.SelectionBehavior;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.history.action.SelectionHistoryAction;

import java.util.BitSet;

public abstract class AbstractSelectionTool implements PainterTool, SelectionBehavior {
    protected final Selection selection;
    protected final BitSet newPart;
    protected final int maxWidth;
    protected final int maxHeight;
    private boolean isSelecting;
    private BitSet before;
    protected int startX;
    protected int startY;

    private final SelectionToolSettings settings;

    public AbstractSelectionTool(SelectionToolSettings settings, Selection selection) {
        this.settings = settings;
        this.selection = selection;
        this.maxWidth = selection.getWidth();
        this.maxHeight = selection.getHeight();
        this.newPart = new BitSet(maxWidth * maxHeight);
    }

    @Override
    public SelectionToolSettings selectionToolSettings() {
        return settings;
    }

    @Override
    public final void start(@Nullable DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        isSelecting = true;
        before = selection.getSelectionMask();
        newPart.clear();
        if (settings.getMode() == SelectionToolSettings.SelectionMode.REPLACE) {
            selection.clear();
        }
        startX = x;
        startY = y;
        startSelecting(x, y, lineX, lineY, firstColor, secondColor);
    }

    @Override
    public final void process(@Nullable DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor) {
        if (!isSelecting) return;
        processSelection(x, y, lineX, lineY, firstColor, secondColor);
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

    protected abstract void startSelecting(int x, int y, int lineX, int lineY, int firstColor, int secondColor);

    protected abstract void processSelection(int x, int y, int lineX, int lineY, int firstColor, int secondColor);

    @Override
    public HistoryAction submit() {
        if (!isSelecting) return HistoryAction.EMPTY;
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
        return isSelecting;
    }
}
