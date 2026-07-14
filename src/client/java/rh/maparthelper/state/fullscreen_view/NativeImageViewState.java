package rh.maparthelper.state.fullscreen_view;

import org.joml.Vector2i;
import org.joml.Vector4i;

public class NativeImageViewState {
    private static final NativeImageViewState FULLSCREEN_VIEW_STATE_INSTANCE = new NativeImageViewState();
    private static final NativeImageViewState PAINTER_STATE_INSTANCE = new NativeImageViewState();

    private NativeImageViewState() {}

    public static NativeImageViewState getFullscreenViewInstance() {
        return FULLSCREEN_VIEW_STATE_INSTANCE;
    }

    public static NativeImageViewState getPainterInstance() {
        return PAINTER_STATE_INSTANCE;
    }

    private InitialImageViewState initialState;

    protected double scale = 1;
    protected double scaledImageWidth;
    protected double scaledImageHeight;
    private double xOffset;
    private double yOffset;

    private final Vector2i hoveredPixelPos = new Vector2i(0, 0);
    private final Vector2i closestHoveredLine = new Vector2i(0, 0);
    private double pixelWidth;
    private double pixelHeight;

    private boolean showPixelGrid = false;
    private boolean showMapGrid = false;

    public InitialImageViewState getInitialState() {
        return initialState;
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

    public Vector2i hoveredPixelPos() {
        return hoveredPixelPos;
    }

    public Vector2i closestHoveredLine() {
        return closestHoveredLine;
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

    public void updateInitialStateIfNeeded(int containerWidth, int containerHeight, int imageWidth, int imageHeight) {
        if (initialState == null
                || imageWidth != initialState.originalWidth() || (imageHeight != initialState.originalHeight())
                || containerWidth != initialState.containerWidth() || containerHeight != initialState.containerHeight()
        ) {
            Vector4i sizeFitted = fitImage(containerWidth, containerHeight, imageWidth, imageHeight);
            this.initialState = new InitialImageViewState(
                    imageWidth, imageHeight,
                    containerWidth, containerHeight,
                    sizeFitted.x, sizeFitted.y,
                    sizeFitted.z, sizeFitted.w,
                    containerHeight / ((sizeFitted.y / (float) imageHeight) * 6)
            );
            this.scale = 1;
            this.xOffset = 0;
            this.yOffset = 0;
            this.scaledImageWidth = initialState.fittedImageWidth();
            this.scaledImageHeight = initialState.fittedImageHeight();
        }
    }

    public static Vector4i fitImage(int containerWidth, int containerHeight, int imageWidth, int imageHeight) {
        if (imageWidth <= 0 || imageHeight <= 0) return new Vector4i(128, 128, 0, 0);

        double aspect = (double) imageWidth / imageHeight;
        double scaleX = (double) containerWidth / imageWidth;
        double scaleY = (double) containerHeight / imageHeight;

        int widthFitted, heightFitted, xFitted, yFitted;

        if (scaleX < scaleY) {
            // Fit by width
            widthFitted = (int) (containerWidth * 0.85);
            xFitted = (containerWidth - widthFitted) / 2;
            heightFitted = (int) (widthFitted / aspect);
            yFitted = (containerHeight - heightFitted) / 2;
        } else {
            // Fit by height
            heightFitted = (int) (containerHeight * 0.85);
            yFitted = (containerHeight - heightFitted) / 2;
            widthFitted = (int) (heightFitted * aspect);
            xFitted = (containerWidth - widthFitted) / 2;
        }

        return new Vector4i(widthFitted, heightFitted, xFitted, yFitted);
    }
}
