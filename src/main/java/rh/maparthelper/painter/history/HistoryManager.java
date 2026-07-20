package rh.maparthelper.painter.history;

import org.jetbrains.annotations.Nullable;
import rh.maparthelper.painter.history.action.HistoryAction;
import rh.maparthelper.painter.history.action.HistoryActionType;

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

    public @Nullable HistoryActionType undo() {
        if (!hasUndo()) return null;
        HistoryAction action = undoStack.pop();
        action.undo();
        redoStack.addFirst(action);
        return action.type();
    }

    public @Nullable HistoryActionType redo() {
        if (!hasRedo()) return null;
        HistoryAction action = redoStack.pop();
        action.redo();
        undoStack.addFirst(action);
        return action.type();
    }

    public void saveAction(HistoryAction action) {
        if (action == HistoryAction.EMPTY) return;
        undoStack.addFirst(action);
        this.clearRedoStack();
        this.trimUndoStack();
    }

    private void clearRedoStack() {
        while (!redoStack.isEmpty()) {
            HistoryAction action = redoStack.remove();
            action.discardedFromRedoHistory();
        }
    }

    private void clearUndoStack() {
        while (!undoStack.isEmpty()) {
            HistoryAction action = undoStack.remove();
            action.discardedFromUndoHistory();
        }
    }

    private void trimUndoStack() {
        while (undoStack.size() > maxHistorySize) {
            HistoryAction action = undoStack.removeLast();
            action.discardedFromUndoHistory();
        }
    }

    public void clear() {
        clearRedoStack();
        clearUndoStack();
    }
}
