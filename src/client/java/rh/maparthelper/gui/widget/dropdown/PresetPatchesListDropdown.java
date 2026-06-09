package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.palette.RegisteredPresetPatch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PresetPatchesListDropdown extends DropdownOverlayWidget {
    private final Map<UUID, Button> presetButtons;

    public PresetPatchesListDropdown(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                                     boolean dynamicText, Component initOption,
                                     Consumer<UUID> action, Map<UUID, RegisteredPresetPatch> presets
    ) {
        super(screen, null, width, height, initOption);
        presetButtons = initOverlay(dynamicText, overlayHeight, overlayWidth, action, presets);
    }

    public Map<UUID, Button> initOverlay(boolean dynamicText, int height, int width,
                                         Consumer<UUID> action, Map<UUID, RegisteredPresetPatch> presets
    ) {
        Map<UUID, Button> presetWidgets = new LinkedHashMap<>();

        for (Map.Entry<UUID, RegisteredPresetPatch> entry : presets.entrySet()) {
            UUID presetUUID = entry.getKey();
            MutableComponent valueText = getValueComponent(entry.getValue());

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

            presetWidgets.put(presetUUID, widget);
        }

        this.setOverlay(OverlayLayoutFactory.listMenu(height, width, presetWidgets.values().toArray(AbstractWidget[]::new)));
        return presetWidgets;
    }

    private static @NotNull MutableComponent getValueComponent(RegisteredPresetPatch preset) {
        MutableComponent valueText = Component.literal("\"" + preset.getPresetName() + "\"");

        valueText.withStyle(style -> switch (preset.getState()) {
            case UNCHANGED -> style.withColor(-1).withItalic(false);
            case CREATED -> style.withColor(0xFF_00FF00);
            case CHANGED -> style.withColor(16755200).withItalic(true);
            case REMOVED -> style.withColor(0xFF_ff5151);
        });
        return valueText;
    }

    public void updateNameFor(RegisteredPresetPatch preset) {
        presetButtons.get(preset.getUUID()).setMessage(getValueComponent(preset));
    }
}
