package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.palette.PatchTypes;
import rh.maparthelper.palette.RegisteredPresetPatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PresetPatchesListDropdown extends DropdownOverlayWidget {
    private static final Identifier REMOVE_ICON_TEXTURE = Identifier.fromNamespaceAndPath(MapartHelper.MOD_ID, "textures/gui/icons/trash_can.png");

    private final Map<UUID, Button> presetButtons;
    private final Consumer<RegisteredPresetPatch> onRemove;

    public PresetPatchesListDropdown(@NotNull Screen screen, int width, int height, int overlayWidth, int overlayHeight,
                                     boolean dynamicText, Component initOption,
                                     Consumer<UUID> action, Consumer<RegisteredPresetPatch> onRemove,
                                     Map<UUID, RegisteredPresetPatch> presets
    ) {
        super(screen, null, width, height, initOption);
        this.onRemove = onRemove;
        presetButtons = initOverlay(dynamicText, overlayHeight, overlayWidth, action, presets);
    }

    public Map<UUID, Button> initOverlay(boolean dynamicText, int height, int width,
                                         Consumer<UUID> action, Map<UUID, RegisteredPresetPatch> presets
    ) {
        LinearLayout menu = LinearLayout.vertical().spacing(-2);
        menu.defaultCellSetting()
                .alignVerticallyMiddle()
                .alignHorizontallyCenter()
                .padding(2);

        Map<UUID, Button> presetWidgets = new HashMap<>();

        for (Map.Entry<UUID, RegisteredPresetPatch> entry : presets.entrySet()) {
            LinearLayout line = LinearLayout.horizontal();
            line.defaultCellSetting().alignVerticallyMiddle();
            UUID presetUUID = entry.getKey();
            RegisteredPresetPatch patch = entry.getValue();
            MutableComponent valueText = getValueComponent(patch);

            Button presetWidget = Button.builder(
                            valueText,
                            btn -> {
                                if (dynamicText)
                                    this.setMessage(valueText);
                                action.accept(presetUUID);
                            }
                    )
                    .size(width - 26, 16)
                    .build();
            DecorativeButtonWidget removeButton = DecorativeButtonWidget.builder(
                    REMOVE_ICON_TEXTURE,
                    btn -> {
                        if (patch.getState() == PatchTypes.CREATED) {
                            onRemove.accept(patch);
                            return;
                        }
                        patch.toggleToRemove();
                        presetWidget.setMessage(getValueComponent(patch));
                        onRemove.accept(patch);

                        if (patch.getState() == PatchTypes.REMOVED) {
                            btn.setTextureColor(0xFF_ff2222);
                            btn.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.cancel_deletePreset_tooltip")));
                        } else {
                            btn.setTextureColor(0xFF_c4c4c4);
                            btn.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.deletePreset_tooltip")));
                        }
                    }
            ).size(16, 16).build();

            if (patch.getState() == PatchTypes.REMOVED) {
                removeButton.setTextureColor(0xFF_ff2222);
                removeButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.cancel_deletePreset_tooltip")));
            } else {
                removeButton.setTextureColor(0xFF_c4c4c4);
                removeButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.deletePreset_tooltip")));
            }

            line.addChild(presetWidget);
            line.addChild(removeButton);
            menu.addChild(line);

            presetWidgets.put(presetUUID, presetWidget);
        }

        this.setOverlay(OverlayLayoutFactory.defaultOverlay(menu, height, width));
        return presetWidgets;
    }

    private static @NotNull MutableComponent getValueComponent(RegisteredPresetPatch preset) {
        MutableComponent valueText = Component.literal("\"" + preset.getPresetName() + "\"");

        valueText.withStyle(style -> switch (preset.getState()) {
            case UNCHANGED -> style.withColor(-1).withItalic(false);
            case CREATED -> style.withColor(0xFF_00FF00);
            case CHANGED -> style.withColor(0xFF_ffaa00).withItalic(true);
            case REMOVED -> style.withColor(0xFF_ff5151);
        });
        return valueText;
    }

    public void updateNameFor(RegisteredPresetPatch preset) {
        presetButtons.get(preset.getUUID()).setMessage(getValueComponent(preset));
    }
}
