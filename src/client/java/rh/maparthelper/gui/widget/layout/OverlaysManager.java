package rh.maparthelper.gui.widget.layout;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.Nullable;

public class OverlaysManager {
    @Nullable
    private static OverlayLayout visibleOne;

    public static void handleMouseClick(double x, double y) {
        if (visibleOne == null) return;
        if (visibleOne.isMouseOverSwitch(x, y)) return;
        if (!visibleOne.isMouseOverLayout(x, y)) {
            visibleOne.setVisible(false);
        }
    }

    public static boolean isVisibleOneContent(Renderable renderable) {
        if (visibleOne == null) return false;
        if (renderable instanceof AbstractWidget widget)
            return visibleOne.isLayoutContent(widget);
        return false;
    }

    public static boolean isMouseOverVisibleLayout(double mouseX, double mouseY) {
        if (visibleOne == null) return false;
        return visibleOne.isMouseOverLayout(mouseX, mouseY);
    }

    public static void renderVisibleOne(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        if (visibleOne == null) return;
        //~ if >=26.1 '.render(' -> '.extractRenderState('
        visibleOne.visitWidgets(w -> w.render(graphics, mouseX, mouseY, deltaTicks));
    }

    public static void close() {
        visibleOne = null;
    }

    static void setVisibleOne(@Nullable OverlayLayout overlayLayout) {
        if (visibleOne != null)
            visibleOne.setVisible(false);
        visibleOne = overlayLayout;
    }
}
