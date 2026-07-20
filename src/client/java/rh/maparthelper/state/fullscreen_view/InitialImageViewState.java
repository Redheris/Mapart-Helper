package rh.maparthelper.state.fullscreen_view;

public record InitialImageViewState(
        int originalWidth,
        int originalHeight,
        int containerWidth,
        int containerHeight,
        int fittedImageWidth,
        int fittedImageHeight,
        int fittedImageXOffset,
        int fittedImageYOffset,
        double maxScale
) {}
