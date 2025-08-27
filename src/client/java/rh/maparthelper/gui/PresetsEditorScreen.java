package rh.maparthelper.gui;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.gui.widget.PresetsDropdownMenuWidget;
import rh.maparthelper.gui.widget.ScrollableGridWidget;

import java.util.List;

public class PresetsEditorScreen extends ScreenAdapted {
    private final MapartEditorScreen parent;
    private final int x;
    private final int y;
    private final int marginRight;
    private final int marginBottom;
    private int boxWidth;
    private int boxHeight;

    private PalettePresetsConfig presetsConfig = PaletteConfigManager.presetsConfig;
    private String editingPreset = presetsConfig.getCurrentPresetFilename();
    private String presetName = presetsConfig.presetFiles.get(editingPreset);
    private PalettePresetsConfig.PalettePreset presetEdit = presetsConfig.copyPreset(editingPreset);

    private PresetsDropdownMenuWidget presetsListDropdown;
    private TextFieldWidget presetNameField;

    protected PresetsEditorScreen(MapartEditorScreen parent, Text title, int x, int y, int marginRight, int marginBottom) {
        super(title);
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
    }

    @Override
    protected void init() {
        parent.width = width;
        parent.height = height;
        parent.clearChildren();
        parent.init();

        this.boxWidth = parent.width - x - marginRight;
        this.boxHeight = parent.height - y - marginBottom;

        DirectionalLayoutWidget presetBarLeft = DirectionalLayoutWidget.horizontal();
        presetBarLeft.setPosition(x + 5, y + 5);
        Positioner presetBarLeftPositioner = presetBarLeft.getMainPositioner().alignVerticalCenter();

        TextWidget presetNameLabel = new TextWidget(Text.of("Пресет:"), textRenderer);
        presetBarLeft.add(presetNameLabel, presetBarLeftPositioner.copy().marginRight(5));

        presetNameField = new TextFieldWidget(
                textRenderer, (int)(boxWidth * 0.35), 20, Text.empty()
        );
        presetNameField.setText(this.presetName);
        presetNameField.setChangedListener(value -> {
            if (value.isBlank()) {
                presetNameField.setSuggestion("Название пресета");
                return;
            }
            presetNameField.setSuggestion(null);
            this.presetName = value;
        });
        presetBarLeft.add(presetNameField);

        presetsListDropdown = new PresetsDropdownMenuWidget(
                this, 0, 0, 20, 20, presetNameField.getWidth() + 20, Text.of("☰")
        );
        presetsListDropdown.setTooltip(Tooltip.of(Text.of("Выбрать пресет")));
        presetsListDropdown.addEntries(this::changeEditingPreset, presetsConfig.getPresetKeys());
        presetsListDropdown.forEachEntry(this::addSelectableChild);
        presetBarLeft.add(presetsListDropdown);


        ButtonWidget createNewPreset = ButtonWidget.builder(Text.of("➕"), b -> this.createNewPreset())
                .size(20, 20)
                .build();
        createNewPreset.setTooltip(Tooltip.of(Text.of("Создать новый пресет")));
        presetBarLeft.add(createNewPreset);

        ButtonWidget duplicatePreset = ButtonWidget.builder(Text.of("\uD83D\uDDD0"), b -> this.duplicatePreset())
                .size(20, 20)
                .build();
        duplicatePreset.setTooltip(Tooltip.of(Text.of("Дублировать выбранный пресет")));
        presetBarLeft.add(duplicatePreset);

        ButtonWidget deletePreset = ButtonWidget.builder(Text.of("\uD83D\uDDD1"), b -> this.deletePreset())
                .size(20, 20)
                .build();
        deletePreset.setTooltip(Tooltip.of(Text.of("Удалить выбранный пресет")));
        presetBarLeft.add(deletePreset);

        presetBarLeft.refreshPositions();
        presetBarLeft.forEachChild(this::addDrawableChild);
        presetsListDropdown.refreshPositions();


        DirectionalLayoutWidget presetBarRight = DirectionalLayoutWidget.horizontal();
        presetBarRight.setPosition(0, y + 5);
        presetBarRight.getMainPositioner().alignVerticalCenter().marginRight(2);

        ButtonWidget save = ButtonWidget.builder(Text.of("Сохранить"),b -> saveChanges())
                .size(60, 20)
                .build();
        presetBarRight.add(save);

        ButtonWidget close = ButtonWidget.builder(Text.of("❌"), b -> this.close())
                .size(20, 20)
                .build();
        presetBarRight.add(close);

        presetBarRight.refreshPositions();
        presetBarRight.setX(x + boxWidth - presetBarRight.getWidth());
        presetBarRight.forEachChild(this::addDrawableChild);

        // =========== Colors editing area ===========

        int squareSize = 24;
        int columns = (boxWidth - 5) / (squareSize + 5);
        ScrollableGridWidget colorsEditor = new ScrollableGridWidget(x, y + 31, boxWidth, boxHeight - 31, y, y + boxHeight, 6);
        GridWidget colorsGrid = colorsEditor.grid;
        colorsGrid.getMainPositioner().alignHorizontalCenter().margin(5, 4, 0, 0);

        for (int i = 0; i < 63; i++) {
            MapColor mapColor = MapColor.get(i + 1);
            if (mapColor == MapColor.CLEAR) break;

            MapColorWidget color = new MapColorWidget(0, 0, squareSize, mapColor);
            int row = 2 * columns * (i / columns);
            if (row == 0)
                colorsGrid.add(color, row, i % columns);
            else
                colorsGrid.add(color, row, i % columns, colorsGrid.copyPositioner().marginTop(10));

            ScrollableGridWidget blocksList = new ScrollableGridWidget(
                    0, 0, squareSize, 150, colorsEditor.getY(), y + boxHeight, 3
            );
            GridWidget.Adder adder = blocksList.grid.createAdder(1);

            List<Block> blocks = PaletteConfigManager.completePalette.palette.get(mapColor.id);
            MapColorBlockWidget noneBlock = new MapColorBlockWidget(
                    0, 0, squareSize, squareSize,
                    b -> presetEdit.colors.remove(mapColor),
                    mapColor, Blocks.BARRIER
            );
            adder.add(noneBlock, blocksList.grid.copyPositioner().alignHorizontalCenter());
            for (Block block : blocks) {
                MapColorBlockWidget blockWidget = new MapColorBlockWidget(
                        0, 0, squareSize, squareSize,
                        b -> presetEdit.colors.put(mapColor, block),
                        mapColor, block
                );
                adder.add(blockWidget, blocksList.grid.copyPositioner().alignHorizontalCenter());
            }

            colorsGrid.add(
                    blocksList,
                    row + columns,
                    i % columns,
                    colorsGrid.copyPositioner().alignHorizontalCenter()
            );

            colorsEditor.refreshPositions();
            this.addDrawableChild(blocksList);
        }

        colorsEditor.refreshPositions();
        this.addDrawableChild(colorsEditor);
    }

    private void changeEditingPreset(String presetFile) {
        this.presetName = presetsConfig.presetFiles.get(presetFile);
        this.presetNameField.setText(presetName);
        this.editingPreset = presetFile;
        this.presetEdit = presetsConfig.copyPreset(editingPreset);
    }

    private void saveChanges() {
        PaletteConfigManager.updatePreset(editingPreset, presetEdit);
        PaletteConfigManager.renamePreset(editingPreset, this.presetName);
        this.presetsConfig = PaletteConfigManager.presetsConfig;
        presetsListDropdown.updateNames(presetsConfig.getPresetKeys());
    }

    private void duplicatePreset() {
        PaletteConfigManager.duplicatePreset(editingPreset);
        this.presetsConfig = PaletteConfigManager.presetsConfig;
        changeEditingPreset(presetsConfig.getCurrentPresetFilename());
        clearAndInit();
    }

    private void deletePreset() {
        PaletteConfigManager.deletePreset(editingPreset);
        this.presetsConfig = PaletteConfigManager.presetsConfig;
        changeEditingPreset(presetsConfig.getCurrentPresetFilename());
        clearAndInit();
    }

    private void createNewPreset() {
        PaletteConfigManager.createNewPreset();
        this.presetsConfig = PaletteConfigManager.presetsConfig;
        changeEditingPreset(presetsConfig.getCurrentPresetFilename());
        clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(0, 0, -50);
        parent.render(context, 0, 0, deltaTicks);
        matrixStack.pop();

        int w = boxWidth;
        int h = boxHeight;
        context.fill(x, y, x + w, y + h, 0x77000000);
        context.fill(x, y, x + w, y + 30, 0x44000000);
        context.drawBorder(x - 1, y - 1, w + 2, h + 2,0x44FFFFFF);
        context.drawHorizontalLine(x, x + w, y + 30,0x77FFFFFF);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.applyBlur();
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    private static class MapColorWidget extends ClickableWidget {
        private int x;
        private int y;
        private final int squareSize;

        public final MapColor color;

        public MapColorWidget(int x, int y, int squareSize, MapColor color) {
            super(x, y, squareSize, squareSize, Text.empty());
            this.x = x;
            this.y = y;
            this.squareSize = squareSize;
            this.color = color;
        }

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            if (color == MapColor.WATER_BLUE) {
                context.fill(x, y, x + squareSize, y + squareSize, color.getRenderColor(MapColor.Brightness.NORMAL));
            } else {
                int partHeight = squareSize / 3;
                context.fill(x, y, x + squareSize, y + partHeight, color.getRenderColor(MapColor.Brightness.LOW));
                context.fill(x, y + partHeight, x + squareSize, y + partHeight * 2, color.getRenderColor(MapColor.Brightness.NORMAL));
                context.fill(x, y + partHeight * 2, x + squareSize, y + squareSize, color.getRenderColor(MapColor.Brightness.HIGH));
            }
            context.drawBorder(x, y, squareSize, squareSize, 0xFF555555);
        }
    }

    private class MapColorBlockWidget extends ButtonWidget {
        private final MapColor mapColor;
        private final Block block;

        protected MapColorBlockWidget(int x, int y, int width, int height, PressAction onPress, MapColor mapColor, Block block) {
            super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
            this.mapColor = mapColor;
            this.block = block;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            MatrixStack matrixStack = context.getMatrices();
            matrixStack.push();
            ItemStack blockItem;
            if (block instanceof FluidBlock) {
                blockItem = Registries.FLUID.get(Registries.BLOCK.getId(block)).getBucketItem().getDefaultStack();
            } else {
                blockItem = block.asItem().getDefaultStack();
            }

            int x = getX();
            int y = getY();

            matrixStack.translate(x, y, 0);
            matrixStack.scale(width / 16f, height / 16f, 1);
            matrixStack.translate(-x, -y, 0);
            context.drawItem(blockItem, x, y);
            matrixStack.pop();

            boolean isMouseOverBlock = mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
            if (context.scissorContains(mouseX, mouseY) && isMouseOverBlock) {
                if (block != Blocks.BARRIER) {
                    List<Text> tooltip = PresetsEditorScreen.getTooltipFromItem(MinecraftClient.getInstance(), blockItem);
                    PresetsEditorScreen.this.setTooltip(tooltip.stream().map(Text::asOrderedText).toList());
                } else {
                    PresetsEditorScreen.this.setTooltip(Text.of("Не использовать цвет"));
                }
            }

            if (presetEdit.colors.get(mapColor) == block || block == Blocks.BARRIER && !presetEdit.colors.containsKey(mapColor)) {
                matrixStack.push();
                matrixStack.translate(0, 0, 200);
                context.drawBorder(x, y, width, height, Colors.CYAN);
                matrixStack.pop();
            }
        }
    }
}
