package rh.maparthelper.state.fullscreen_view;

import org.joml.Vector2i;

public class NativeImageViewState {
    private static final NativeImageViewState INSTANCE = new NativeImageViewState();

    private NativeImageViewState() {}

    public static NativeImageViewState getInstance() {
        return INSTANCE;
    }

    private InitialImageViewState initialState;

    protected double scale = 1;
    protected double scaledImageWidth;
    protected double scaledImageHeight;
    private double xOffset;
    private double yOffset;

    private final Vector2i pixelPos = new Vector2i(0, 0);
    private double pixelWidth;
    private double pixelHeight;

    private boolean showPixelGrid = false;
    private boolean showMapGrid = false;

    public InitialImageViewState getInitialState() {
        return initialState;
    }

    public void setInitialState(InitialImageViewState initialState) {
        this.initialState = initialState;
    }

    public double scale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double scaledImageWidth() {
        return scaledImageWidth;
    }

    public void setScaledImageWidth(double scaledImageWidth) {
        this.scaledImageWidth = scaledImageWidth;
    }

    public double scaledImageHeight() {
        return scaledImageHeight;
    }

    public void setScaledImageHeight(double scaledImageHeight) {
        this.scaledImageHeight = scaledImageHeight;
    }

    public Vector2i pixelPos() {
        return pixelPos;
    }

    public void setPixelPos(int x, int y) {
        this.pixelPos.set(x, y);
    }

    public double pixelWidth() {
        return pixelWidth;
    }

    public void setPixelWidth(double pixelWidth) {
        this.pixelWidth = pixelWidth;
    }

    public double pixelHeight() {
        return pixelHeight;
    }

    public void setPixelHeight(double pixelHeight) {
        this.pixelHeight = pixelHeight;
    }

    public double xOffset() {
        return xOffset;
    }

    public void setXOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public double yOffset() {
        return yOffset;
    }

    public void setYOffset(double yOffset) {
        this.yOffset = yOffset;
    }

    public boolean showPixelGrid() {
        return showPixelGrid;
    }

    public void setShowPixelGrid(boolean showPixelGrid) {
        this.showPixelGrid = showPixelGrid;
    }

    public boolean showMapGrid() {
        return showMapGrid;
    }

    public void setShowMapGrid(boolean showMapGrid) {
        this.showMapGrid = showMapGrid;
    }
}
