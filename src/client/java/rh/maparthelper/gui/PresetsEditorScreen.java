package rh.maparthelper.gui;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.conversion.MapartImageConverter;
import rh.maparthelper.gui.widget.BlockItemWidget;
import rh.maparthelper.gui.widget.MapColorWidget;
import rh.maparthelper.gui.widget.PresetsDropdownMenuWidget;
import rh.maparthelper.gui.widget.ScrollableGridWidget;
import rh.maparthelper.render.RenderUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private TextFieldWidget presetNameField;
    private ScrollableGridWidget colorsEditor;

    protected PresetsEditorScreen(MapartEditorScreen parent, int x, int y, int marginRight, int marginBottom) {
        super(Text.translatable("maparthelper.gui.presets_editor_screen"));
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
        parent.clearChildren();
        parent.init();

        this.boxWidth = parent.width - boxX - marginRight;
        this.boxHeight = parent.height - boxY - marginBottom;

        DirectionalLayoutWidget presetBarLeft = DirectionalLayoutWidget.horizontal();
        presetBarLeft.setPosition(boxX + 5, boxY + 5);
        Positioner presetBarLeftPositioner = presetBarLeft.getMainPositioner().alignVerticalCenter();

        TextWidget presetNameLabel = new TextWidget(Text.translatable("maparthelper.gui.preset"), textRenderer);
        presetBarLeft.add(presetNameLabel, presetBarLeftPositioner.copy().marginRight(5));

        presetNameField = new TextFieldWidget(
                textRenderer, (int) (boxWidth * 0.35), 20, Text.empty()
        );
        presetNameField.setText(presetsConfig.presetFiles.get(editingPreset));
        presetBarLeft.add(presetNameField);

        presetsListDropdown = new PresetsDropdownMenuWidget(
                this, 0, 0, 20, 20, presetNameField.getWidth() + 20,
                Text.of("☰")
        );
        presetsListDropdown.setMenuXOffset(-presetNameField.getWidth());
        presetsListDropdown.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.presets.choose_preset")));
        presetsListDropdown.addEntries(this::changeEditingPreset, presetsConfig.presetFiles);
        presetBarLeft.add(presetsListDropdown);

        presetNameField.setChangedListener(value -> {
            if (value.isBlank()) {
                presetNameField.setSuggestion(Text.translatable("maparthelper.gui.presets.preset_name").getString());
                return;
            }
            presetNameField.setSuggestion(null);
            presetsConfig.presetFiles.put(editingPreset, value);
            if (presetsListDropdown != null)
                presetsListDropdown.updateNames(presetsConfig.presetFiles.values());
        });

        ButtonWidget createEmptyPreset = ButtonWidget.builder(Text.of("\uD83D\uDDCB"), b -> this.createNewPreset(false))
                .size(17, 20)
                .build();
        createEmptyPreset.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.presets.createEmptyPreset_tooltip")));
        presetBarLeft.add(createEmptyPreset);

        ButtonWidget createDefaultPreset = ButtonWidget.builder(Text.of("➕"), b -> this.createNewPreset(true))
                .size(17, 20)
                .build();
        createDefaultPreset.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.presets.createDefaultPreset_tooltip")));
        presetBarLeft.add(createDefaultPreset);

        ButtonWidget duplicatePreset = ButtonWidget.builder(Text.of("\uD83D\uDDD0"), b -> this.duplicatePreset())
                .size(17, 20)
                .build();
        duplicatePreset.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.presets.duplicatePreset_tooltip")));
        presetBarLeft.add(duplicatePreset);

        ButtonWidget deletePreset = ButtonWidget.builder(Text.of("\uD83D\uDDD1"), b -> this.deletePreset())
                .size(17, 20)
                .build();
        deletePreset.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.presets.deletePreset_tooltip")));
        presetBarLeft.add(deletePreset);

        presetBarLeft.refreshPositions();
        presetBarLeft.forEachChild(this::addDrawableChild);


        DirectionalLayoutWidget presetBarRight = DirectionalLayoutWidget.horizontal();
        presetBarRight.setPosition(0, boxY + 5);
        presetBarRight.getMainPositioner().alignVerticalCenter().marginRight(1);

        ButtonWidget updateFiles = ButtonWidget.builder(Text.of("⟲"), b -> this.updateFiles())
                .size(17, 20)
                .build();
        updateFiles.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.presets.updateFiles_tooltip")));
        presetBarRight.add(updateFiles);

        ButtonWidget save = ButtonWidget.builder(Text.translatable("maparthelper.gui.save"), b -> saveChanges())
                .size(60, 20)
                .build();
        presetBarRight.add(save);

        ButtonWidget close = ButtonWidget.builder(Text.of("❌"), b -> this.close())
                .size(20, 20)
                .build();
        presetBarRight.add(close);

        presetBarRight.refreshPositions();
        presetBarRight.setX(boxX + boxWidth - presetBarRight.getWidth());
        presetBarRight.forEachChild(this::addDrawableChild);

        // =========== Colors editing area ===========

        int squareSize = 24;
        int columns = (boxWidth - 5) / (squareSize + 5);
        colorsEditor = new ScrollableGridWidget(
                null,
                boxX, boxY + 31,
                boxWidth, boxHeight - 31, 6
        );
        GridWidget colorsGrid = colorsEditor.grid;
        colorsGrid.add(EmptyWidget.ofWidth(boxWidth - 11), 0, 0, 1, columns);
        colorsGrid.getMainPositioner().alignHorizontalCenter().margin(0, 4, 0, 0);

        for (int i = 0; i < 63; i++) {
            MapColor mapColor = MapColor.get(i + 1);
            if (mapColor == MapColor.CLEAR) break;

            MapColorWidget color = new MapColorWidget(0, 0, squareSize, squareSize, mapColor, false);
            color.showColorName(true);
            int row = 2 * columns * (i / columns);
            if (row == 0)
                colorsGrid.add(color, row, i % columns);
            else
                colorsGrid.add(color, row, i % columns, colorsGrid.copyPositioner().marginTop(10));

            ScrollableGridWidget blocksList = new ScrollableGridWidget(
                    colorsEditor,
                    0, 0,
                    squareSize + 5, 150, 3
            );
            blocksList.grid.getMainPositioner().alignHorizontalCenter().alignVerticalCenter();
            GridWidget.Adder adder = blocksList.grid.createAdder(1);
            adder.add(EmptyWidget.ofWidth(blocksList.getWidth()));

            MapColorBlockWidget noneBlock = new MapColorBlockWidget(
                    0, 0, squareSize,
                    Blocks.BARRIER, mapColor,
                    (click, doubled) -> {
                        presetsConfig.getPreset(editingPreset).removeColor(mapColor);
                        updatedPresets.add(editingPreset);
                    }
            );
            noneBlock.setTooltip(Text.translatable("maparthelper.gui.presets.remove_color"));

            adder.add(noneBlock, blocksList.grid.copyPositioner().alignHorizontalCenter());

            List<Block> blocks = PaletteConfigManager.completePalette.palette.get(mapColor.id);
            if (blocks != null) {
                for (Block block : blocks) {
                    MapColorBlockWidget blockWidget = new MapColorBlockWidget(
                            0, 0, squareSize,
                            block, mapColor,
                            (click, doubled) -> {
                                presetsConfig.getPreset(editingPreset).updateColor(mapColor, block);
                                updatedPresets.add(editingPreset);
                            }
                    );
                    adder.add(blockWidget, blocksList.grid.copyPositioner().alignHorizontalCenter());
                }
            }

            colorsGrid.add(
                    blocksList,
                    row + columns,
                    i % columns,
                    colorsGrid.copyPositioner().alignHorizontalCenter()
            );
            this.addDrawableChild(blocksList);
        }

        colorsEditor.refreshPositions();
        this.addDrawableChild(colorsEditor);
    }

    private void createNewPreset(boolean createDefault) {
        String newPreset = presetsConfig.createNewPreset(createDefault, updatedPresets, deletedPresets);
        presetsListDropdown = null;
        changeEditingPreset(newPreset);
        clearAndInit();
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
        clearAndInit();
    }

    private void duplicatePreset() {
        String newPreset = presetsConfig.duplicatePreset(editingPreset, updatedPresets, deletedPresets);
        presetsListDropdown = null;
        changeEditingPreset(newPreset);
        clearAndInit();
    }

    private void changeEditingPreset(String presetFile) {
        this.editingPreset = presetFile;
        this.presetNameField.setText(presetsConfig.presetFiles.get(presetFile));
    }

    private void updateFiles() {
        PaletteConfigManager.updateCompletePalette();
        PaletteConfigManager.readPresetsConfigFile();

        this.presetsConfig = PaletteConfigManager.presetsConfig.getEditable();
        this.editingPreset = presetsConfig.getCurrentPresetFilename();

        this.deletedPresets.clear();
        this.updatedPresets.clear();
        clearAndInit();
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
        if (updateMapart) MapartImageConverter.updateMapart(parent.mapart);
        PaletteConfigManager.savePresetsConfigFile();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            return true;
        return colorsEditor.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Matrix3x2fStack matrixStack = context.getMatrices();

        matrixStack.pushMatrix();
        parent.render(context, 0, 0, deltaTicks);
        matrixStack.popMatrix();

        context.state.createNewRootLayer();
        this.applyBlur(context);
        this.renderDarkening(context);

        int w = boxWidth;
        int h = boxHeight;
        context.fill(boxX, boxY, boxX + w, boxY + h, 0x77000000);
        context.fill(boxX, boxY, boxX + w, boxY + 30, 0x44000000);
        RenderUtils.drawBorder(context, boxX - 1, boxY - 1, w + 2, h + 2, 0x44FFFFFF);
        context.drawHorizontalLine(boxX, boxX + w - 1, boxY + 30, 0x77FFFFFF);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    private class MapColorBlockWidget extends BlockItemWidget {
        private final MapColor mapColor;
        private final ClickAction clickAction;

        private MapColorBlockWidget(int x, int y, int squareSize, Block block, MapColor mapColor, ClickAction clickAction) {
            super(x, y, squareSize, block, true);
            this.mapColor = mapColor;
            this.clickAction = clickAction;
        }

        @Override
        public void onClick(Click click, boolean doubled) {
            this.clickAction.click(click, doubled);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            return super.mouseClicked(click, doubled);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);

            PalettePresetsConfig.PalettePreset preset = presetsConfig.getPreset(editingPreset);
            Block presetBlock = preset.colors.get(mapColor);
            boolean flag = presetBlock == null && this.getBlock() == Blocks.BARRIER;
            flag = flag || (presetBlock != null && presetBlock == this.getBlock());
            if (flag) {
                context.state.createNewRootLayer();
                RenderUtils.drawBorder(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), Colors.CYAN);
            }
        }

        interface ClickAction {
            void click(Click click, boolean doubled);
        }
    }
}
