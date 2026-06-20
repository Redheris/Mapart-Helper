package rh.maparthelper.painter.history;

import rh.maparthelper.painter.history.action.HistoryAction;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryManager {
    private final Deque<HistoryAction> undoStack = new ArrayDeque<>();
    private final Deque<HistoryAction> redoStack = new ArrayDeque<>();
    private int maxHistorySize = 60;

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = Math.max(5, maxHistorySize);
    }

    public boolean hasUndo() {
        return !undoStack.isEmpty();
    }

    public boolean hasRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!hasUndo()) return;
        HistoryAction action = undoStack.pop();
        action.undo();
        redoStack.addFirst(action);
    }

    public void redo() {
        if (!hasRedo()) return;
        HistoryAction action = redoStack.pop();
        action.redo();
        undoStack.addFirst(action);
    }

    public void saveAction(HistoryAction action) {
        if (action == HistoryAction.EMPTY) return;
        undoStack.addFirst(action);
        if (undoStack.size() > maxHistorySize) {
            undoStack.removeLast();
        }
        redoStack.clear();
    }
}
