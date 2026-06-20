package rh.maparthelper.painter.history.action;

public interface HistoryAction {
    HistoryAction EMPTY = new EmptyAction();

    void undo();

    void redo();

    class EmptyAction implements HistoryAction {
        private EmptyAction() {}

        @Override
        public void undo() {}

        @Override
        public void redo() {}
    }
}
