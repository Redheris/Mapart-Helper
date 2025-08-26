package rh.maparthelper.gui;

import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
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

        DirectionalLayoutWidget presetBar = DirectionalLayoutWidget.horizontal();
        presetBar.setPosition(x + 5, y + 5);
        Positioner presetBarPositioner = presetBar.getMainPositioner();

        TextWidget presetNameLabel = new TextWidget(Text.of("Пресет:"), textRenderer);
        presetBar.add(presetNameLabel, presetBarPositioner.copy().alignVerticalCenter().marginRight(10));

        TextFieldWidget presetName = new TextFieldWidget(
                textRenderer, (int)(boxWidth * 0.4), 20, Text.empty()
        );
        presetName.setText(this.presetName);
        presetName.setChangedListener(value -> {
            if (value.isBlank()) {
                presetName.setSuggestion("Название пресета");
                return;
            }
            presetName.setSuggestion(null);
            this.presetName = value;
        });
        presetBar.add(presetName);

        PresetsDropdownMenuWidget presetsListDropdown = new PresetsDropdownMenuWidget(
                this, 0, 0, 20, 20, presetName.getWidth() + 20, Text.of("...")
        );
        presetsListDropdown.addEntries(name -> changeEditingPreset(presetName, name), presetsConfig.getPresetKeys());
        presetsListDropdown.addSelectableEntries(this::addSelectableChild);
        presetBar.add(presetsListDropdown);

        presetBar.refreshPositions();
        presetBar.forEachChild(this::addDrawableChild);
        presetsListDropdown.refreshPositions();

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

            // TODO: add an option of not using the color
            List<Block> blocks = PaletteConfigManager.completePalette.palette.get(mapColor.id);
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

    private void changeEditingPreset(TextFieldWidget presetNameField, String preset) {
        presetNameField.setText(presetsConfig.presetFiles.get(preset));
        this.editingPreset = preset;
        this.presetEdit = presetsConfig.copyPreset(editingPreset);
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
                List<Text> tooltip = PresetsEditorScreen.getTooltipFromItem(MinecraftClient.getInstance(), blockItem);
                PresetsEditorScreen.this.setTooltip(tooltip.stream().map(Text::asOrderedText).toList());
            }

            if (presetEdit.colors.get(mapColor) == block) {
                matrixStack.push();
                matrixStack.translate(0, 0, 200);
                context.drawBorder(x, y, width, height, Colors.CYAN);
                matrixStack.pop();
            }
        }
    }
}
