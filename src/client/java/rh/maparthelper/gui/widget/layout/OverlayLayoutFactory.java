package rh.maparthelper.gui.widget.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class OverlayLayoutFactory {
    public static @NotNull OverlayLayout defaultOverlay(Layout contentLayout, int height, int width) {
        var scrollable = new AdjScrollableLayoutWidget(contentLayout, height);
        if (width > 0)
            scrollable.setWidth(width);
        scrollable.setMarginX(1);
        scrollable.setBackgroundColor(0x99FFFFFF);
        scrollable.setOutlineColor(0x77_000000);
        scrollable.setScrollBarWidth(5);
        scrollable.arrangeElements();
        return new OverlayLayout(scrollable);
    }

    public static @NotNull OverlayLayout defaultOverlay(Layout contentLayout, int height) {
        return defaultOverlay(contentLayout, height, 0);
    }

    public static @NotNull OverlayLayout listMenu(int height, int width, AbstractWidget... entries) {
        LinearLayout menu = LinearLayout.vertical().spacing(-2);
        menu.defaultCellSetting()
                .alignHorizontallyCenter()
                .padding(2, 2, 2, 2);
        for (AbstractWidget entry : entries) {
            if (entry == null) continue;
            menu.addChild(entry);
        }
        return defaultOverlay(menu, height, width);
    }
}
