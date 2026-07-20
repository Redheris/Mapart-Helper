package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;

import java.util.function.Consumer;

public class EnumListDropdown extends DropdownOverlayButton {
    private final PressAction modifySwitchButtonOnChange;

    public EnumListDropdown(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                            Component fieldName, Enum<?> initOption, boolean showFieldName, boolean showTooltips,
                            PressAction modifySwitchButtonOnChange, Consumer<Enum<?>> action, Enum<?>... entries) {
        super(screen, null, width, height, constructInitComponent(fieldName, showFieldName, initOption));

        this.modifySwitchButtonOnChange = modifySwitchButtonOnChange;
        initOverlay(overlayHeight, overlayWidth, fieldName, showTooltips, showFieldName, action, entries);
    }

    public EnumListDropdown(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                            Component fieldName, Enum<?> initOption, boolean showFieldName, boolean showTooltips,
                            Consumer<Enum<?>> action, Enum<?>... entries) {
        this(
                screen, width, height, overlayWidth, overlayHeight,
                fieldName, initOption, showFieldName, showTooltips,
                null, action, entries
        );
    }

    private void initOverlay(int height, int width,
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
                                this.setMessage(showFieldName ? fieldName.copy().append(objectName) : objectName);
                                action.accept(value);
                                if (modifySwitchButtonOnChange != null)
                                    modifySwitchButtonOnChange.onPress(this);
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

        this.setOverlay(OverlayLayoutFactory.listMenu(height, width, widgets));
    }

    private static Component constructInitComponent(Component fieldName, boolean showFieldName, Enum<?> value) {
        Component objectName = Component.translatable("maparthelper.gui.option." + value);
        return showFieldName ? fieldName.copy().append(objectName) : objectName;
    }
}
