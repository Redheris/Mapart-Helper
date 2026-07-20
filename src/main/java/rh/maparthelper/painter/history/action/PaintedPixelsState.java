package rh.maparthelper.painter.history.action;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public record PaintedPixelsState(
        IntArrayList indices,
        IntArrayList before,
        IntArrayList after
) {
    public PaintedPixelsState() {
        this(new IntArrayList(), new IntArrayList(), new IntArrayList());
    }
}
