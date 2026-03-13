package rh.maparthelper.gui.screen.panel;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.WrapperWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.conversion.schematic.MapartSchematicBuilder;
import rh.maparthelper.gui.screen.MapartEditorScreen;
import rh.maparthelper.gui.widget.BlockItemWidget;
import rh.maparthelper.gui.widget.MapartPreviewWidget;
import rh.maparthelper.gui.widget.ScrollableGridWidget;
import rh.maparthelper.mapart.ColorsCounter;
import rh.maparthelper.mapart.MapartProcessing;
import rh.maparthelper.util.InventoryItemsCounter;
import rh.maparthelper.util.RenderUtils;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class MaterialListPanel extends WrapperWidget {
    private final MapartEditorScreen screen;
    private final MapartProcessing mapart;
    private ScrollableGridWidget materialList;

    private int auxBlockCount = 0;
    private boolean materialsAscendingOrder = false;
    private boolean displayRemainingAmount = false;
    private final InventoryItemsCounter inventoryItemsCounter;
    private boolean displayTotalCount = true;

    public MaterialListPanel(MapartEditorScreen screen, MapartProcessing mapart, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.screen = screen;
        this.mapart = mapart;
        if (MinecraftClient.getInstance().player != null) {
            inventoryItemsCounter = new InventoryItemsCounter();
            inventoryItemsCounter.count(MinecraftClient.getInstance().player.getInventory());
        } else
            inventoryItemsCounter = null;
    }

    public void setDisplayTotalCount(boolean displayTotalCount) {
        this.displayTotalCount = displayTotalCount;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
        if (materialList != null)
            consumer.accept(materialList);
    }

    public boolean isMaterialsAscendingOrder() {
        return materialsAscendingOrder;
    }

    public void toggleMaterialsAscendingOrder() {
        this.materialsAscendingOrder = !materialsAscendingOrder;
    }

    public boolean isDisplayRemainingAmount() {
        return displayRemainingAmount;
    }

    public void toggleDisplayRemainingAmount() {
        if (inventoryItemsCounter == null)
            return;
        this.displayRemainingAmount = !displayRemainingAmount;
    }

    public void updateMaterialList(Consumer<ScrollableGridWidget> childAdder, Consumer<Element> childRemover) {
        MaterialListBlockWidget.fixedHighlight = null;
        MaterialListBlockWidget.selectedForExcluding.clear();

        childRemover.accept(materialList);
        int listTop = getY();
        materialList = new ScrollableGridWidget(
                null,
                getX() - 6, listTop,
                screen.width - getX() - 5, screen.height - listTop, 6
        );

        if (!CurrentConversionSettings.isMapartConverted()) return;

        materialList.setLeftScroll(true);
        materialList.grid.setColumnSpacing(0);
        materialList.grid.getMainPositioner().alignVerticalCenter();

        GridWidget.Adder materialListAdder = materialList.grid.createAdder(2);
        PalettePresetsConfig palette = PaletteConfigManager.presetsConfig;

        ColorsCounter colorsCounter = mapart.getTotalColorsCounter(MapartHelper.conversionSettings.getMaterialsCountMode());
        ColorsCounter.MapColorCount[] colorCounts = colorsCounter.getColorCounts(materialsAscendingOrder);

        this.auxBlockCount = mapart.getWidth() * 128;
        BlockItemWidget auxBlockItemWidget = new BlockItemWidget(0, 0, 24, MapartHelper.conversionSettings.getAuxBlock());
        auxBlockItemWidget.insertToTooltip(1, Text.translatable("maparthelper.aux_block").formatted(Formatting.GRAY));

        TextWidget auxAmountText = new TextWidget(Text.empty(), screen.getTextRenderer());
        materialListAdder.add(auxBlockItemWidget, materialList.grid.copyPositioner().marginLeft(6));
        materialListAdder.add(auxAmountText);

        boolean hasAuxBlockInColors = false;
        if (displayRemainingAmount)
            hasAuxBlockInColors = calculateRemainingCounts(colorCounts);

        for (ColorsCounter.MapColorCount colorCount : colorCounts) {
            addBlockToMaterialList(materialListAdder, palette, colorCount);
        }

        if (displayRemainingAmount && !hasAuxBlockInColors)
            auxBlockCount -= inventoryItemsCounter.getCounts().getOrDefault(MapartHelper.conversionSettings.getAuxBlock().asItem(), 0);

        MutableText amountText = Text.literal(getAmountString(Math.max(0, auxBlockCount), auxBlockItemWidget.getStackSize()));
        if (auxBlockCount <= 0)
            amountText = amountText.formatted(Formatting.GREEN);
        auxAmountText.setWidth(screen.getTextRenderer().getWidth(amountText));
        auxAmountText.setMessage(amountText);
        auxAmountText.setTooltip(Tooltip.of(amountText));

        materialList.refreshPositions();
        childAdder.accept(materialList);
    }

    private void addBlockToMaterialList(GridWidget.Adder adder, PalettePresetsConfig palette, ColorsCounter.MapColorCount color) {
        MapColor mapColor = MapColor.get(color.id());
        Block block = palette.getBlockOfMapColor(mapColor);
        if (block == null) return;

        MaterialListBlockWidget blockItemWidget = new MaterialListBlockWidget(0, 0, 24, block, mapColor);
        adder.add(blockItemWidget, materialList.grid.copyPositioner().marginLeft(6));
        MutableText text = Text.literal(getAmountString(color.amount(), block.asItem().getMaxCount()));
        if (color.amount() == 0)
            text = text.formatted(Formatting.GREEN);
        TextWidget amountText = new TextWidget(text, screen.getTextRenderer());
        adder.add(amountText);
        amountText.setTooltip(Tooltip.of(amountText.getMessage()));
        amountText.setTooltipDelay(Duration.ofMillis(100));

        if (MapartSchematicBuilder.shouldPlaceAuxBlock(block)) {
            auxBlockCount += color.amount();
        }
    }

    /// @return Whether the color-blocks list contains the same block as the auxiliary block.
    private boolean calculateRemainingCounts(ColorsCounter.MapColorCount[] colors) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        boolean hasAuxBlock = false;

        PalettePresetsConfig paletteConfig = PaletteConfigManager.presetsConfig;

        List<Item> countingBlocks = Arrays.stream(colors)
                .map(c -> {
                    Block block = paletteConfig.getBlockOfMapColor(MapColor.get(c.id()));
                    if (block instanceof FluidBlock)
                        return Registries.FLUID.get(Registries.BLOCK.getId(block)).getBucketItem();
                    return block.asItem();
                })
                .toList();

        Item auxBlockItem = MapartHelper.conversionSettings.getAuxBlock().asItem();
        Map<Item, Integer> inventory = inventoryItemsCounter.getCounts();

        for (int i = 0; i < colors.length; i++) {
            Item item = countingBlocks.get(i);

            int have = inventory.getOrDefault(item, 0);
            int remaining = colors[i].amount() - have;
            if (item == auxBlockItem) {
                auxBlockCount += Math.min(0, remaining);
                hasAuxBlock = true;
            }
            colors[i] = new ColorsCounter.MapColorCount(colors[i].id(), Math.max(0, remaining));
        }

        Comparator<ColorsCounter.MapColorCount> cmp = Comparator.comparingInt(ColorsCounter.MapColorCount::amount);
        Arrays.sort(colors, materialsAscendingOrder ? cmp : cmp.reversed());
        return hasAuxBlock;
    }

    private String getAmountString(int amount, int stackSize) {
        StringBuilder text = new StringBuilder();
        int shBoxSize = 27 * stackSize;
        int shBoxes = amount / shBoxSize;
        int stacks = amount % shBoxSize / stackSize;
        int items = amount % shBoxSize % stackSize;
        boolean counted = shBoxes > 0 || stacks > 0;

        if (shBoxes > 0)
            text.append(shBoxes).append("§3").append(Text.translatable("maparthelper.gui.shulker_box_abbr").getString()).append("§r");
        if (stacks > 0) {
            text.append(shBoxes > 0 ? " + " : "").append(stacks);
            if (stackSize > 1) text.append("§3x").append(stackSize).append("§r");
        }
        if (counted) {
            if (displayTotalCount)
                text.insert(0, " = ");
            if (items > 0) text.append(" + ").append(items);
        }

        if (displayTotalCount || !counted)
            text.insert(0, "" + amount);

        return text.toString();
    }

    public class MaterialListBlockWidget extends BlockItemWidget {
        private static final Set<MapColor> selectedForExcluding = new HashSet<>();
        private static MaterialListBlockWidget fixedHighlight;
        private static boolean hoveringAny = false;
        private final MapColor mapColor;
        private boolean confirmRemoving = false;

        private MaterialListBlockWidget(int x, int y, int squareSize, Block block, MapColor mapColor) {
            super(x, y, squareSize, block);
            this.mapColor = mapColor;
            this.tooltip = new ArrayList<>(List.of(
                    block.getName().asOrderedText(),
                    Text.translatable("maparthelper.gui.LMB_to_highlight").formatted(Formatting.GRAY).asOrderedText(),
                    Text.translatable("maparthelper.gui.RMB_to_remove").formatted(Formatting.GRAY).asOrderedText()
            ));
            if (MinecraftClient.getInstance().options.advancedItemTooltips)
                this.tooltip.add(Text.literal(Registries.BLOCK.getId(block).toString()).formatted(Formatting.DARK_GRAY).asOrderedText());
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
            if (fixedHighlight == this) {
                context.createNewRootLayer();
                context.drawBorder(getX(), getY(), this.width, this.height, MapartHelper.commonConfig.mapartEditor.previewHighlightingColor);
            } else if (MapartHelper.commonConfig.mapartEditor.previewHighlightOnHover
                    && context.scissorContains(mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
                screen.setHighlightingColor(mapColor);
                hoveringAny = true;
            }
            if (confirmRemoving) {
                RenderUtils.renderItemStack(
                        context,
                        Blocks.BARRIER.asItem().getDefaultStack(),
                        "RemoveColor",
                        getX(), getY(),
                        width, height
                );
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (confirmRemoving) {
                    MapartImageUpdater.excludeColorsFromMapart(mapart, selectedForExcluding);
                    screen.updateResetExcludedColorsButton(true);
                    selectedForExcluding.clear();
                    return true;
                }
                if (fixedHighlight == this) {
                    fixedHighlight = null;
                } else {
                    fixedHighlight = this;
                    screen.setHighlightingColor(mapColor);
                }
                return true;
            }
            if (button == 1) {
                if (fixedHighlight == this) {
                    fixedHighlight = null;
                }
                if (!confirmRemoving) {
                    confirmRemoving = true;
                    selectedForExcluding.add(mapColor);
                    tooltip.set(1, Text.translatable("maparthelper.gui.LMB_to_confirm").formatted(Formatting.RED).asOrderedText());
                    tooltip.set(2, Text.translatable("maparthelper.gui.RMB_to_cancel").formatted(Formatting.RED).asOrderedText());
                } else {
                    confirmRemoving = false;
                    selectedForExcluding.remove(mapColor);
                    tooltip.set(1, Text.translatable("maparthelper.gui.LMB_to_highlight").formatted(Formatting.GRAY).asOrderedText());
                    tooltip.set(2, Text.translatable("maparthelper.gui.RMB_to_remove").formatted(Formatting.GRAY).asOrderedText());
                }
            }
            return true;
        }

        public static boolean isHoveringAny() {
            return hoveringAny;
        }

        public static void resetHovering() {
            hoveringAny = false;
        }

        public static void setDefaultHighlight(MapartPreviewWidget mapartPreview) {
            if (fixedHighlight == null)
                mapartPreview.setHighlightingColor(MapColor.CLEAR);
            else
                mapartPreview.setHighlightingColor(fixedHighlight.mapColor);
        }
    }
}
