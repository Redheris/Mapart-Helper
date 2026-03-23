package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.function.Consumer;

public class EnumDropdownMenuWidget extends DropdownMenuWidget {
    private final Component fieldName;
    private boolean showTooltips = true;
    private final boolean showFieldName;

    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, int maxMenuHeight, Component fieldName, Component initOption, boolean showFieldName) {
        super(parent, x, y, width, height, menuWidth, maxMenuHeight, showFieldName ? fieldName.copy().append(initOption) : initOption);
        this.fieldName = fieldName;
        this.showFieldName = showFieldName;
    }

    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Component fieldName, Component initOption, boolean showFieldName) {
        this(parent, x, y, width, height, menuWidth, -1, fieldName, initOption, showFieldName);
    }

    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Component fieldName, Component initOption) {
        this(parent, x, y, width, height, menuWidth, fieldName, initOption, true);
    }

    public void toggleTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    public void addEntry(Consumer<Enum<?>> action, Enum<?> object) {
        if (object == null) return;
        Component objectName = Component.translatable("maparthelper.gui.option." + object.name());
        Button widget = Button.builder(
                        objectName,
                        btn -> {
                            this.setMessage(showFieldName ? fieldName.copy().append(objectName) : objectName);
                            action.accept(object);
                        }
                )
                .size(menuWidth - 4, 15)
                .build();
        if (showTooltips) {
            Component tooltip = MutableComponent.create(new TranslatableContents("maparthelper.gui.option." + object.name() + "._TOOLTIP", "", TranslatableContents.NO_ARGS));
            widget.setTooltip(Tooltip.create(tooltip));
        }
        super.addEntry(widget);
    }

    public void addEntries(Consumer<Enum<?>> action, Enum<?>... objects) {
        for (Enum<?> object : objects) {
            addEntry(action, object);
        }
    }
}
