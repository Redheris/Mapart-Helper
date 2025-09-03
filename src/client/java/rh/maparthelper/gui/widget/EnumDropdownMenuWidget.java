package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class EnumDropdownMenuWidget extends DropdownMenuWidget {
    private final Text fieldName;
    public EnumDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Text fieldName, Text initOption) {
        super(parent, x, y, width, height, menuWidth, -1, fieldName.copy().append(initOption));
        this.fieldName = fieldName;
    }

    public void addEntry(Consumer<Enum<?>> action, Enum<?> object) {
        Text objectName = Text.translatable("maparthelper.gui.option." + object.name());
        ButtonWidget widget = ButtonWidget.builder(
                objectName,
                        btn -> {
                            this.setMessage(fieldName.copy().append(objectName));
                            action.accept(object);
                        }
                )
                .size(menuWidth - 4, 15)
                .build();
        super.addEntry(widget);
    }

    public void addEntries(Consumer<Enum<?>> action, Enum<?>... objects) {
        for (Enum<?> object : objects) {
            addEntry(action, object);
        }
    }
}
