package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;

//? if >=1.21.10
//import net.minecraft.client.input.InputWithModifiers;

public class DropdownOverlayWidget extends DecorativeButtonWidget {
    private final Screen screen;
    private OverlayLayout overlay;
    private int overlayXOffset = 0;
    private int maxOverlayHeight;
    /// If the overlay can be placed with its full height either downward or upward - it will be prioritized
    private final boolean keepMaxOverlayHeight;

    public DropdownOverlayWidget(@NotNull Screen screen, OverlayLayout overlay, int x, int y, int width, int height,
                                 Component message, boolean keepMaxOverlayHeight
    ) {
        super(true, -1, x, y, width, height, message, btn -> {});
        this.keepMaxOverlayHeight = keepMaxOverlayHeight;
        this.screen = screen;
        this.setOverlay(overlay);
    }

    public DropdownOverlayWidget(@NotNull Screen screen, OverlayLayout overlay, int width, int height, Component message,
                                 boolean keepMaxOverlayHeight) {
        this(screen, overlay, 0, 0, width, height, message, keepMaxOverlayHeight);
    }

    public DropdownOverlayWidget(@NotNull Screen screen, OverlayLayout overlay, int width, int height, Component message) {
        this(screen, overlay, width, height, message, false);
    }

    public DropdownOverlayWidget(@NotNull Screen screen, OverlayLayout overlay, int x, int y, int width, int height,
                                 boolean keepMaxOverlayHeight, boolean renderVanillaBackground, WidgetSprites customSprites
    ) {
        super(renderVanillaBackground, customSprites, x, y, width, height, btn -> {});
        this.keepMaxOverlayHeight = keepMaxOverlayHeight;
        this.screen = screen;
        this.setOverlay(overlay);
    }

    public DropdownOverlayWidget(@NotNull Screen screen, OverlayLayout overlay, int width, int height,
                                 boolean keepMaxOverlayHeight, boolean renderVanillaBackground, WidgetSprites customSprites
    ) {
        this(screen, overlay, 0, 0, width, height, keepMaxOverlayHeight, renderVanillaBackground, customSprites);
    }

    protected void setOverlay(OverlayLayout overlay) {
        this.overlay = overlay;
        if (overlay != null) {
            overlay.setSwitchWidget(this);
            maxOverlayHeight = overlay.getHeight();
        }
    }

    public OverlayLayout getOverlay() {
        return overlay;
    }

    public void setOverlayXOffset(int overlayXOffset) {
        this.overlayXOffset = overlayXOffset;
    }

    private void updateOverlayPosition() {
        int minX = Math.max(2, getX() + overlayXOffset);
        overlay.setX(Math.min(minX, screen.width - overlay.getWidth() - 2));

        int maxDownwardHeight = screen.height - getBottom();
        int downwardHeight = Math.min(maxDownwardHeight, maxOverlayHeight);
        int upwardHeight = Math.min(getY(), maxOverlayHeight);

        if (downwardHeight >= upwardHeight
                || keepMaxOverlayHeight && maxDownwardHeight >= maxOverlayHeight
                || !keepMaxOverlayHeight && maxDownwardHeight > maxOverlayHeight / 2) {
            overlay.setY(getBottom());
            overlay.setHeight(downwardHeight);
        } else {
            overlay.setY(Math.max(0, getY() - upwardHeight));
            overlay.setHeight(upwardHeight);
        }
    }

    @Override
    public void onPress(/*? if >=1.21.10 {*/ /*@NotNull InputWithModifiers input *//*?}*/) {
        updateOverlayPosition();
        overlay.setVisible(!overlay.isVisible());
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        updateOverlayPosition();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        updateOverlayPosition();
    }
}
