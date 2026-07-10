package rh.maparthelper.gui.widget.layout;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class OverlaysManager {
    @Nullable
    private static OverlayLayout activeOverlay;

    //~ widget_events
    public static boolean handleMouseClick(Screen screen, Set<OverlayLayout> overlays, double mouseX, double mouseY, int button) {
        if (activeOverlay != null && activeOverlay.isAutoCloseable()) {
            if (!activeOverlay.isMouseOverSwitch(mouseX, mouseY) && !activeOverlay.isMouseOverLayout(mouseX, mouseY)) {
                activeOverlay.setVisible(false);
            }
        }
        if (activeOverlay != null && activeOverlay.isMouseOverLayout(mouseX, mouseY)) {
            var container = activeOverlay.getLayout().container;
            boolean clickResult = activeOverlay.mouseClicked(mouseX, mouseY, button);
            if (clickResult) {
                screen.setFocused(container);
                if (button == 0) screen.setDragging(true);
            }
            return clickResult;
        }
        for (OverlayLayout overlay : overlays) {
            if (!overlay.isVisible()) continue;
            if (overlay.isMouseOverLayout(mouseX, mouseY)) {
                setActiveOverlay(overlay);
                boolean clickResult = overlay.mouseClicked(mouseX, mouseY, button);
                if (clickResult) {
                    screen.setFocused(overlay.getLayout().container);
                    if (button == 0) screen.setDragging(true);
                }
                return clickResult;
            }
        }
        return false;
    }
    //~ !widget_events

    public static boolean isVisibleOverlayContent(Set<OverlayLayout> overlays, Renderable renderable) {
        for (OverlayLayout overlay : overlays) {
            if (!overlay.isVisible()) continue;
            if (overlay.isLayoutContent(renderable)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMouseOverVisibleLayout(Set<OverlayLayout> overlays, double mouseX, double mouseY) {
        for (OverlayLayout overlay : overlays) {
            if (!overlay.isVisible()) continue;
            if (overlay.isMouseOverLayout(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    public static void renderOverlays(Set<OverlayLayout> overlays, @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean activeOverlayHovered = false;
        if (activeOverlay != null) {
            activeOverlayHovered = activeOverlay.isMouseOverLayout(mouseX, mouseY);
        }
        OverlayLayout nextActiveOverlay = null;
        for (OverlayLayout overlay : overlays) {
            if (overlay == activeOverlay || !overlay.isVisible()) continue;
            if (activeOverlayHovered && overlay.isMouseOverLayout(mouseX, mouseY)) {
                overlay.renderOverlay(graphics, -1, -1, partialTick);
            } else {
                if (overlay.isMouseOverLayout(mouseX, mouseY)) {
                    nextActiveOverlay = overlay;
                }
                overlay.renderOverlay(graphics, mouseX, mouseY, partialTick);
            }
        }
        if (activeOverlay != null) {
            activeOverlay.renderOverlay(graphics, mouseX, mouseY, partialTick);
        }
        if (nextActiveOverlay != null) {
            setActiveOverlay(nextActiveOverlay);
        }
    }

    public static void close(OverlayLayout overlay) {
        if (overlay == null || overlay == activeOverlay) {
            activeOverlay = null;
        }
    }

    public static void setActiveOverlay(@Nullable OverlayLayout overlay) {
        if (overlay == activeOverlay) return;

        if (activeOverlay != null && activeOverlay.isAutoCloseable()) {
            activeOverlay.setVisible(false);
        }

        activeOverlay = overlay;

        if (activeOverlay != null) {
            activeOverlay.setVisible(true);
        }
    }
}
