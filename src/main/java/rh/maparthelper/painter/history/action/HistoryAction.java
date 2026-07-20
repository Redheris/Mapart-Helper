package rh.maparthelper.painter.history.action;

public interface HistoryAction {
    HistoryAction EMPTY = new EmptyAction();

    HistoryActionType type();

    void undo();

    void redo();

    /**
     * Called when the action is removed from the history while it is on the redo stack.
     * For example, to dispose unused resources like GPU textures.
     */
    default void discardedFromRedoHistory() {}

    /**
     * Called when the action is removed from the history while being on the undo stack.
     * For example, to dispose unused resources like GPU textures.
     */
    default void discardedFromUndoHistory() {}

    class EmptyAction implements HistoryAction {
        private EmptyAction() {}

        @Override
        public HistoryActionType type() {
            return HistoryActionType.OTHER;
        }

        @Override
        public void undo() {}

        @Override
        public void redo() {}
    }
}
