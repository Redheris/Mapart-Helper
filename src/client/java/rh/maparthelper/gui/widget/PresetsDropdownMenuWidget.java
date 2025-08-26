package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import rh.maparthelper.config.palette.PaletteConfigManager;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class PresetsDropdownMenuWidget extends DropdownMenuWidget {
    private boolean dynamicText = false;

    public PresetsDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Text initOption, boolean dynamicText) {
        super(parent, x, y, width, height, menuWidth, initOption);
        this.dynamicText = dynamicText;
    }

    public PresetsDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Text fieldName) {
        super(parent, x, y, width, height, menuWidth, fieldName);
    }

    public void addEntry(Consumer<String> action, String value) {
        Text valueText = Text.of("\"" + PaletteConfigManager.presetsConfig.presetFiles.get(value) + "\"");
        ButtonWidget widget = ButtonWidget.builder(
                        valueText,
                        btn -> {
                            if (dynamicText)
                                this.setMessage(valueText);
                            action.accept(value);
                        }
                )
                .size(menuWidth - 4, 15)
                .build();
        super.addEntry(widget);
    }

    public void addEntries(Consumer<String> action, Set<String> values) {
        for (String value : values) {
            addEntry(action, value);
        }
    }

    public void updateNames(Set<String> values) {
        Iterator<String> it = values.iterator();
        this.forEachEntry(w -> {
            Text valueText = Text.of("\"" + PaletteConfigManager.presetsConfig.presetFiles.get(it.next()) + "\"");
            w.setMessage(valueText);
        });
    }
}
