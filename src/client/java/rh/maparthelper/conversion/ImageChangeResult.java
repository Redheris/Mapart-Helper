package rh.maparthelper.conversion;

public enum ImageChangeResult {
    NEED_FULL_UPDATE(10),
    ONLY_TOP_LINE(5),
    SIMPLE(0);

    public final int priority;

    ImageChangeResult(int priority) {
        this.priority = priority;
    }
}
