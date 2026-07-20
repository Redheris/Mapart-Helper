package rh.maparthelper.painter.history.action;

import rh.maparthelper.painter.drawing.Selection;

import java.util.BitSet;

public class SelectionHistoryAction implements HistoryAction {
    private final Selection selection;
    private final BitSet before;
    private final BitSet after;

    public SelectionHistoryAction(Selection selection, BitSet before, BitSet after) {
        this.selection = selection;
        this.before = before;
        this.after = after;
    }

    @Override
    public HistoryActionType type() {
        return HistoryActionType.SELECTION;
    }

    @Override
    public void undo() {
        selection.setSelectionMask(before);
    }

    @Override
    public void redo() {
        selection.setSelectionMask(after);
    }
}
