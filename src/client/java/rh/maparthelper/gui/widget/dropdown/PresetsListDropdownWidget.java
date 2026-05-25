package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class PresetsListDropdownWidget extends DropdownOverlayWidget {
    private final Button[] presetButtons;

    public PresetsListDropdownWidget(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                                     boolean dynamicText, Component initOption,
                                     Consumer<String> action, Map<String, String> presets) {
        super(screen, null, width, height, initOption);

        presetButtons = initOverlay(this, dynamicText, overlayHeight, overlayWidth, action, presets);
    }

    public Button[] initOverlay(PresetsListDropdownWidget dropdownWidget, boolean dynamicText,
                                int height, int width,
                                Consumer<String> action, Map<String, String> presets) {
        Button[] widgets = new Button[presets.size()];
        int id = 0;
        for (Map.Entry<String, String> entry : presets.entrySet()) {
            String presetFile = entry.getKey();
            String presetName = entry.getValue();
            Component valueText = Component.nullToEmpty("\"" + presetName + "\"");

            Button widget = Button.builder(
                            valueText,
                            btn -> {
                                if (dynamicText)
                                    dropdownWidget.setMessage(valueText);
                                action.accept(presetFile);
                            }
                    )
                    .size(width - 10, 15)
                    .build();

            widgets[id++] = widget;
        }
        this.setOverlay(OverlayLayoutFactory.listMenu(height, width, widgets));
        return widgets;
    }

    public void updateNames(Collection<String> names) {
        Iterator<String> it = names.iterator();
        for (Button btn : presetButtons) {
            btn.setMessage(Component.nullToEmpty("\"" + it.next() + "\""));
        }
    }
}
