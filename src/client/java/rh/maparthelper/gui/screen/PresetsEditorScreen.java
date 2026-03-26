package rh.maparthelper.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.BlockItemWidget;
import rh.maparthelper.gui.widget.MapColorWidget;
import rh.maparthelper.gui.widget.PresetsDropdownMenuWidget;
import rh.maparthelper.gui.widget.ScrollableGridWidget;
import rh.maparthelper.util.RenderUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class PresetsEditorScreen extends ScreenAdapted {
    private final MapartEditorScreen parent;
    private final int boxX;
    private final int boxY;
    private final int marginRight;
    private final int marginBottom;
    private int boxWidth;
    private int boxHeight;

    private PalettePresetsConfig.Editable presetsConfig = PaletteConfigManager.presetsConfig.getEditable();
    private String editingPreset = presetsConfig.getCurrentPresetFilename();

    private final Set<String> deletedPresets = new HashSet<>();
    private final Set<String> updatedPresets = new HashSet<>();

    private PresetsDropdownMenuWidget presetsListDropdown;
    private EditBox presetNameField;
    private ScrollableGridWidget colorsEditor;

    protected PresetsEditorScreen(MapartEditorScreen parent, int x, int y, int marginRight, int marginBottom) {
        super(Component.translatable("maparthelper.gui.presets_editor_screen"));
        this.parent = parent;
        this.boxX = x;
        this.boxY = y;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
    }

    @Override
    protected void init() {
        parent.width = width;
        parent.height = height;
        parent.clearWidgets();
        parent.init();

        this.boxWidth = parent.width - boxX - marginRight;
        this.boxHeight = parent.height - boxY - marginBottom;

        LinearLayout presetBarLeft = LinearLayout.horizontal();
        presetBarLeft.setPosition(boxX + 5, boxY + 5);
        LayoutSettings presetBarLeftPositioner = presetBarLeft.defaultCellSetting().alignVerticallyMiddle();

        StringWidget presetNameLabel = new StringWidget(Component.translatable("maparthelper.gui.preset"), font);
        presetBarLeft.addChild(presetNameLabel, presetBarLeftPositioner.copy().paddingRight(5));

        presetNameField = new EditBox(
                font, (int) (boxWidth * 0.35), 20, Component.empty()
        );
        presetNameField.setValue(presetsConfig.presetFiles.get(editingPreset));
        presetBarLeft.addChild(presetNameField);

        presetsListDropdown = new PresetsDropdownMenuWidget(
                this, 0, 0, 20, 20, presetNameField.getWidth() + 20,
                Component.nullToEmpty("☰")
        );
        presetsListDropdown.setMenuXOffset(-presetNameField.getWidth());
        presetsListDropdown.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.choose_preset")));
        presetsListDropdown.addEntries(this::changeEditingPreset, presetsConfig.presetFiles);
        presetBarLeft.addChild(presetsListDropdown);

        presetNameField.setResponder(value -> {
            if (value.isBlank()) {
                presetNameField.setSuggestion(Component.translatable("maparthelper.gui.presets.preset_name").getString());
                return;
            }
            presetNameField.setSuggestion(null);
            presetsConfig.presetFiles.put(editingPreset, value);
            if (presetsListDropdown != null)
                presetsListDropdown.updateNames(presetsConfig.presetFiles.values());
        });

        Button createEmptyPreset = Button.builder(Component.nullToEmpty("\uD83D\uDDCB"), b -> this.createNewPreset(false))
                .size(17, 20)
                .build();
        createEmptyPreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.createEmptyPreset_tooltip")));
        presetBarLeft.addChild(createEmptyPreset);

        Button createDefaultPreset = Button.builder(Component.nullToEmpty("➕"), b -> this.createNewPreset(true))
                .size(17, 20)
                .build();
        createDefaultPreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.createDefaultPreset_tooltip")));
        presetBarLeft.addChild(createDefaultPreset);

        Button duplicatePreset = Button.builder(Component.nullToEmpty("\uD83D\uDDD0"), b -> this.duplicatePreset())
                .size(17, 20)
                .build();
        duplicatePreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.duplicatePreset_tooltip")));
        presetBarLeft.addChild(duplicatePreset);

        Button deletePreset = Button.builder(Component.nullToEmpty("\uD83D\uDDD1"), b -> this.deletePreset())
                .size(17, 20)
                .build();
        deletePreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.deletePreset_tooltip")));
        presetBarLeft.addChild(deletePreset);

        presetBarLeft.arrangeElements();
        presetBarLeft.visitWidgets(this::addRenderableWidget);


        LinearLayout presetBarRight = LinearLayout.horizontal();
        presetBarRight.setPosition(0, boxY + 5);
        presetBarRight.defaultCellSetting().alignVerticallyMiddle().paddingRight(1);

        Button updateFiles = Button.builder(Component.literal("⟲").withStyle(ChatFormatting.BOLD), b -> this.updateFiles())
                .size(18, 20)
                .build();
        updateFiles.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.updateFiles_tooltip")));
        presetBarRight.addChild(updateFiles);

        Button save = Button.builder(Component.translatable("maparthelper.gui.save"), b -> saveChanges())
                .size(60, 20)
                .build();
        presetBarRight.addChild(save);

        Button close = Button.builder(Component.nullToEmpty("❌"), b -> this.onClose())
                .size(20, 20)
                .build();
        presetBarRight.addChild(close);

        presetBarRight.arrangeElements();
        presetBarRight.setX(boxX + boxWidth - presetBarRight.getWidth());
        presetBarRight.visitWidgets(this::addRenderableWidget);

        // =========== Colors editing area ===========

        int squareSize = 24;
        int columns = (boxWidth - 5) / (squareSize + 5);
        colorsEditor = new ScrollableGridWidget(
                null,
                boxX, boxY + 31,
                boxWidth, boxHeight - 31, 6
        );
        GridLayout colorsGrid = colorsEditor.grid;
        colorsGrid.addChild(SpacerElement.width(boxWidth - 11), 0, 0, 1, columns);
        colorsGrid.defaultCellSetting().alignHorizontallyCenter().padding(0, 4, 0, 0);

        for (int i = 0; i < 63; i++) {
            MapColor mapColor = MapColor.byId(i + 1);
            if (mapColor == MapColor.NONE) break;

            MapColorWidget color = new MapColorWidget(0, 0, squareSize, squareSize, mapColor, false);
            color.showColorName(true);
            int row = 2 * columns * (i / columns);
            if (row == 0)
                colorsGrid.addChild(color, row, i % columns);
            else
                colorsGrid.addChild(color, row, i % columns, colorsGrid.newCellSettings().paddingTop(10));

            ScrollableGridWidget blocksList = new ScrollableGridWidget(
                    colorsEditor,
                    0, 0,
                    squareSize + 5, 150, 3
            );
            blocksList.grid.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle();
            GridLayout.RowHelper adder = blocksList.grid.createRowHelper(1);
            adder.addChild(SpacerElement.width(blocksList.getWidth()));

            MapColorBlockWidget noneBlock = new MapColorBlockWidget(
                    0, 0, squareSize,
                    Blocks.BARRIER, mapColor,
                    (mx, my) -> {
                        presetsConfig.getPreset(editingPreset).removeColor(mapColor);
                        updatedPresets.add(editingPreset);
                    }
            );
            noneBlock.setTooltip(Component.translatable("maparthelper.gui.presets.remove_color"));

            adder.addChild(noneBlock, blocksList.grid.newCellSettings().alignHorizontallyCenter());

            List<Block> blocks = PaletteConfigManager.completePalette.palette.get(mapColor.id);
            if (blocks != null) {
                for (Block block : blocks) {
                    MapColorBlockWidget blockWidget = new MapColorBlockWidget(
                            0, 0, squareSize,
                            block, mapColor,
                            (mx, my) -> {
                                presetsConfig.getPreset(editingPreset).updateColor(mapColor, block);
                                updatedPresets.add(editingPreset);
                            }
                    );
                    adder.addChild(blockWidget, blocksList.grid.newCellSettings().alignHorizontallyCenter());
                }
            }

            colorsGrid.addChild(
                    blocksList,
                    row + columns,
                    i % columns,
                    colorsGrid.newCellSettings().alignHorizontallyCenter()
            );
            this.addRenderableWidget(blocksList);
        }

        colorsEditor.arrangeElements();
        this.addRenderableWidget(colorsEditor);
    }

    private void createNewPreset(boolean createDefault) {
        String newPreset = presetsConfig.createNewPreset(createDefault, updatedPresets, deletedPresets);
        presetsListDropdown = null;
        changeEditingPreset(newPreset);
        rebuildWidgets();
    }

    private void deletePreset() {
        PalettePresetsConfig.Editable updatedConfig = presetsConfig.deletePreset(editingPreset, updatedPresets, deletedPresets);
        boolean configEmptied = updatedConfig != presetsConfig;
        if (configEmptied) {
            presetsConfig = updatedConfig;
        } else {
            updatedPresets.remove(editingPreset);
        }
        presetsListDropdown = null;
        changeEditingPreset(presetsConfig.getCurrentPresetFilename());
        rebuildWidgets();
    }

    private void duplicatePreset() {
        String newPreset = presetsConfig.duplicatePreset(editingPreset, updatedPresets, deletedPresets);
        presetsListDropdown = null;
        changeEditingPreset(newPreset);
        rebuildWidgets();
    }

    private void changeEditingPreset(String presetFile) {
        this.editingPreset = presetFile;
        this.presetNameField.setValue(presetsConfig.presetFiles.get(presetFile));
    }

    private void updateFiles() {
        PaletteConfigManager.updateCompletePalette();
        PaletteConfigManager.readPresetsConfigFile();

        this.presetsConfig = PaletteConfigManager.presetsConfig.getEditable();
        this.editingPreset = presetsConfig.getCurrentPresetFilename();

        this.deletedPresets.clear();
        this.updatedPresets.clear();
        rebuildWidgets();
    }

    private void saveChanges() {
        boolean updateMapart = !presetsConfig.getCurrentPresetFilename().equals(PaletteConfigManager.presetsConfig.getCurrentPresetFilename());
        updateMapart |= updatedPresets.contains(presetsConfig.getCurrentPresetFilename());
        PaletteConfigManager.presetsConfig = presetsConfig;
        if (!updatedPresets.isEmpty()) {
            for (String filename : updatedPresets) {
                PaletteConfigManager.savePresetFile(filename);
            }
            updatedPresets.clear();
        }
        if (!deletedPresets.isEmpty()) {
            for (String filename : deletedPresets) {
                PaletteConfigManager.deletePresetFile(filename);
            }
            deletedPresets.clear();
        }
        if (updateMapart) MapartImageUpdater.updateMapart(parent.mapart);
        PaletteConfigManager.savePresetsConfigFile();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            return true;
        return colorsEditor.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        Matrix3x2fStack matrixStack = context.pose();

        matrixStack.pushMatrix();
        parent.render(context, 0, 0, partialTick);
        matrixStack.popMatrix();

        context.guiRenderState.nextStratum();
        this.renderBlurredBackground(context);
        this.renderMenuBackground(context);

        int w = boxWidth;
        int h = boxHeight;
        context.fill(boxX, boxY, boxX + w, boxY + h, 0x77000000);
        context.fill(boxX, boxY, boxX + w, boxY + 30, 0x44000000);
        RenderUtils.renderOutline(context, boxX - 1, boxY - 1, w + 2, h + 2, 0x44FFFFFF);
        context.hLine(boxX, boxX + w - 1, boxY + 30, 0x77FFFFFF);

        super.render(context, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        //? <=1.21.8
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }

    private class MapColorBlockWidget extends BlockItemWidget {
        private final MapColor mapColor;
        private final ClickAction clickAction;

        private MapColorBlockWidget(int x, int y, int squareSize, Block block, MapColor mapColor, ClickAction clickAction) {
            super(x, y, squareSize, block, true);
            this.mapColor = mapColor;
            this.clickAction = clickAction;
        }

        //~ widget_events
        @Override
        public void onClick(double mouseX, double mouseY) {
            this.clickAction.click(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        //~ !widget_events

        @Override
        protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(context, mouseX, mouseY, partialTick);

            PalettePresetsConfig.PalettePreset preset = presetsConfig.getPreset(editingPreset);
            Block presetBlock = preset.colors.get(mapColor);
            boolean flag = presetBlock == null && this.getBlock() == Blocks.BARRIER;
            flag = flag || (presetBlock != null && presetBlock == this.getBlock());
            if (flag) {
                context.guiRenderState.nextStratum();
                RenderUtils.renderOutline(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), CommonColors.HIGH_CONTRAST_DIAMOND);
            }
        }

        interface ClickAction {
            void click(double mouseX, double mouseY);
        }
    }
}
