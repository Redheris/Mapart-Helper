package rh.maparthelper.gui.widget.layout;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OverlaysManager {
    @Nullable
    private static OverlayLayout activeOverlay;

    public static void handleMouseClick(Set<OverlayLayout> overlays, double x, double y) {
        if (activeOverlay != null && activeOverlay.isAutoCloseable()) {
            if (!activeOverlay.isMouseOverSwitch(x, y) && !activeOverlay.isMouseOverLayout(x, y)) {
                activeOverlay.setVisible(false);
            }
        }
        for (OverlayLayout overlay : overlays) {
            if (!overlay.isVisible()) continue;
            if (overlay.isMouseOverLayout(x, y)) {
                setActiveOverlay(overlay);
                break;
            }
        }
    }

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
            activeOverlay.renderOverlay(graphics, mouseX, mouseY, partialTick);
            activeOverlayHovered = activeOverlay.isMouseOverLayout(mouseX, mouseY);
        }
        for (OverlayLayout overlay : overlays) {
            if (overlay == activeOverlay || !overlay.isVisible()) continue;
            if (activeOverlayHovered && overlay.isMouseOverLayout(mouseX, mouseY)) {
                overlay.renderOverlay(graphics, -1, -1, partialTick);
            } else {
                overlay.renderOverlay(graphics, mouseX, mouseY, partialTick);
            }
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

    public static boolean isActiveOverlay(OverlayLayout overlayLayout) {
        return activeOverlay == overlayLayout;
    }
}
