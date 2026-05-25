package rh.maparthelper.gui.widget.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.dropdown.EnumListDropdownWidget;

import java.util.function.Consumer;

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

    public static @NotNull OverlayLayout enumsList(EnumListDropdownWidget dropdownWidget, int height, int width,
                                                   Component fieldName, boolean showTooltips, boolean showFieldName,
                                                   Consumer<Enum<?>> action, Enum<?>... values) {
        Button[] widgets = new Button[values.length];

        for (int i = 0; i < values.length; i++) {
            Enum<?> value = values[i];
            if (value == null) continue;
            Component objectName = Component.translatable("maparthelper.gui.option." + value.name());
            Button widget = Button.builder(
                            objectName,
                            btn -> {
                                dropdownWidget.setMessage(showFieldName ? fieldName.copy().append(objectName) : objectName);
                                action.accept(value);
                            }
                    )
                    .size(width - 10, 15)
                    .build();
            if (showTooltips) {
                Component tooltip = MutableComponent.create(new TranslatableContents(
                        "maparthelper.gui.option." + value.name() + "._TOOLTIP",
                        "",
                        TranslatableContents.NO_ARGS)
                );
                widget.setTooltip(Tooltip.create(tooltip));
            }
            widgets[i] = widget;
        }

        return listMenu(height, width, widgets);
    }
}
