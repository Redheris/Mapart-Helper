package rh.maparthelper.state.fullscreen_view;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record InitialImageViewState(
        @Nullable Identifier imageId,
        int originalWidth,
        int originalHeight,
        int fittedImageWidth,
        int fittedImageHeight,
        int fittedImageXOffset,
        int fittedImageYOffset,
        double maxScale
) {}
