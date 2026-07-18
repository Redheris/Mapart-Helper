package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.palette.RegisteredPalettePreset;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PresetsListDropdown extends DropdownOverlayButton {

    public PresetsListDropdown(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                               boolean dynamicText, Component initOption,
                               Consumer<UUID> action, Map<UUID, RegisteredPalettePreset> presets
    ) {
        super(screen, null, width, height, initOption);
        initOverlay(dynamicText, overlayHeight, overlayWidth, action, presets);
    }

    public void initOverlay(boolean dynamicText, int height, int width,
                            Consumer<UUID> action, Map<UUID, RegisteredPalettePreset> presets
    ) {
        Button[] widgets = new Button[presets.size()];
        int id = 0;
        for (Map.Entry<UUID, RegisteredPalettePreset> entry : presets.entrySet()) {
            UUID presetUUID = entry.getKey();
            String presetName = entry.getValue().presetName();
            Component valueText = Component.literal("\"" + presetName + "\"");

            Button widget = Button.builder(
                            valueText,
                            btn -> {
                                if (dynamicText)
                                    this.setMessage(valueText);
                                action.accept(presetUUID);
                            }
                    )
                    .size(width - 10, 15)
                    .build();

            widgets[id++] = widget;
        }
        this.setOverlay(OverlayLayoutFactory.listMenu(height, width, widgets));
    }
}
