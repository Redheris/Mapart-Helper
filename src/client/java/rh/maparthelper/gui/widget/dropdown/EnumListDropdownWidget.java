package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;

import java.util.function.Consumer;

public class EnumListDropdownWidget extends DropdownOverlayWidget {
    public EnumListDropdownWidget(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                                  Component fieldName, Enum<?> initOption, boolean showFieldName, boolean showTooltips,
                                  Consumer<Enum<?>> action, Enum<?>... entries) {
        super(screen, null, width, height, constructInitComponent(fieldName, showFieldName, initOption));

        this.setOverlay(OverlayLayoutFactory.enumsList(
                this, overlayHeight, overlayWidth, fieldName, showTooltips, showFieldName, action, entries
        ));
    }

    public EnumListDropdownWidget(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                                  Component fieldName, Enum<?> initOption, boolean showFieldName,
                                  Consumer<Enum<?>> action, Enum<?>... entries) {
        this(screen, width, height, overlayWidth, overlayHeight, fieldName, initOption, showFieldName, true, action, entries);
    }

    private static Component constructInitComponent(Component fieldName, boolean showFieldName, Enum<?> value) {
        Component objectName = Component.translatable("maparthelper.gui.option." + value);
        return showFieldName ? fieldName.copy().append(objectName) : objectName;
    }
}
