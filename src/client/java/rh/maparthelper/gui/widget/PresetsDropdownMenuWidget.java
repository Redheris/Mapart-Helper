package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class PresetsDropdownMenuWidget extends DropdownMenuWidget {
    private boolean dynamicText = false;

    public PresetsDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Component initOption, boolean dynamicText) {
        super(parent, x, y, width, height, menuWidth, 120, initOption);
        this.dynamicText = dynamicText;
    }

    public PresetsDropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Component fieldName) {
        super(parent, x, y, width, height, menuWidth, 120, fieldName);
    }

    public void addEntry(Consumer<String> action, String presetFile, String presetName) {
        Component valueText = Component.nullToEmpty("\"" + presetName + "\"");
        Button widget = Button.builder(
                        valueText,
                        btn -> {
                            if (dynamicText)
                                this.setMessage(valueText);
                            action.accept(presetFile);
                        }
                )
                .size(menuWidth - 6, 15)
                .build();
        super.addEntry(widget);
    }

    public void addEntries(Consumer<String> action, Map<String, String> presetFiles) {
        for (Map.Entry<String, String> entry : presetFiles.entrySet()) {
            addEntry(action, entry.getKey(), entry.getValue());
        }
    }

    public void updateNames(Collection<String> names) {
        Iterator<String> it = names.iterator();
        this.forEachEntry(btn -> btn.setMessage(Component.nullToEmpty("\"" + it.next() + "\"")));
    }
}
