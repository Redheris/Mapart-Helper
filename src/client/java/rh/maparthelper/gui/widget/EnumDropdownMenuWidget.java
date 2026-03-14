package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.function.Consumer;

public class EnumDropdownMenuWidget extends DropdownMenuWidget {
    private final Text fieldName;
    private boolean showTooltips = true;
    private final boolean showFieldName;

    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, int maxMenuHeight, Text fieldName, Text initOption, boolean showFieldName) {
        super(parent, x, y, width, height, menuWidth, maxMenuHeight, showFieldName ? fieldName.copy().append(initOption) : initOption);
        this.fieldName = fieldName;
        this.showFieldName = showFieldName;
    }

    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Text fieldName, Text initOption, boolean showFieldName) {
        this(parent, x, y, width, height, menuWidth, -1, fieldName, initOption, showFieldName);
    }

    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Text fieldName, Text initOption) {
        this(parent, x, y, width, height, menuWidth, fieldName, initOption, true);
    }

    public void toggleTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    public void addEntry(Consumer<Enum<?>> action, Enum<?> object) {
        if (object == null) return;
        Text objectName = Text.translatable("maparthelper.gui.option." + object.name());
        ButtonWidget widget = ButtonWidget.builder(
                        objectName,
                        btn -> {
                            this.setMessage(showFieldName ? fieldName.copy().append(objectName) : objectName);
                            action.accept(object);
                        }
                )
                .size(menuWidth - 4, 15)
                .build();
        if (showTooltips) {
            Text tooltip = MutableText.of(new TranslatableTextContent("maparthelper.gui.option." + object.name() + "._TOOLTIP", "", TranslatableTextContent.EMPTY_ARGUMENTS));
            widget.setTooltip(Tooltip.of(tooltip));
        }
        super.addEntry(widget);
    }

    public void addEntries(Consumer<Enum<?>> action, Enum<?>... objects) {
        for (Enum<?> object : objects) {
            addEntry(action, object);
        }
    }
}
