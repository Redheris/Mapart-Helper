package rh.maparthelper.painter.drawing;

public final class DrawingContext {
    private final int[] visitedPixels;
    private int currentStamp;

    public DrawingContext(int width, int height) {
        this.currentStamp = 0;
        this.visitedPixels = new int[width * height];
    }

    public void beginStamp() {
        currentStamp++;
    }

    public boolean visitPixel(int index) {
        if (visitedPixels[index] == currentStamp) return false;
        visitedPixels[index] = currentStamp;
        return true;
    }
}
