package rh.maparthelper.gui.screen.panel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
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

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class MaterialListPanel extends AbstractLayout {
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
        if (Minecraft.getInstance().player != null) {
            inventoryItemsCounter = new InventoryItemsCounter();
            inventoryItemsCounter.count(Minecraft.getInstance().player.getInventory());
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
    public void visitChildren(@NotNull Consumer<LayoutElement> consumer) {
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

    public void updateMaterialList(Consumer<ScrollableGridWidget> childAdder, Consumer<GuiEventListener> childRemover) {
        MaterialListBlockWidget.fixedHighlight = null;
        MaterialListBlockWidget.selectedForExcluding.clear();

        childRemover.accept(materialList);
        int listTop = getY();
        materialList = new ScrollableGridWidget(
                null,
                getX() - 6, listTop,
                screen.width - getX() - 5, screen.height - listTop, 6
        );

        if (!CurrentConversionSettings.isMapartConverted() || MapartHelper.conversionConfig().useUnobtainable()) return;

        materialList.setLeftScroll(true);
        materialList.grid.columnSpacing(0);
        materialList.grid.defaultCellSetting().alignVerticallyMiddle();

        GridLayout.RowHelper materialListAdder = materialList.grid.createRowHelper(2);
        PalettePresetsConfig palette = PaletteConfigManager.presetsConfig;

        ColorsCounter colorsCounter = mapart.getTotalColorsCounter(MapartHelper.conversionConfig().getMaterialsCountMode());
        ColorsCounter.MapColorCount[] colorCounts = colorsCounter.getColorCounts(materialsAscendingOrder);

        this.auxBlockCount = mapart.getWidth() * 128;
        BlockItemWidget auxBlockItemWidget = new BlockItemWidget(0, 0, 24, MapartHelper.conversionConfig().getAuxBlock());
        auxBlockItemWidget.insertToTooltip(1, Component.translatable("maparthelper.aux_block").withStyle(ChatFormatting.GRAY));

        StringWidget auxAmountText = new StringWidget(Component.empty(), screen.getFont());
        materialListAdder.addChild(auxBlockItemWidget, materialList.grid.newCellSettings().paddingLeft(6));
        materialListAdder.addChild(auxAmountText);

        boolean hasAuxBlockInColors = false;
        if (displayRemainingAmount)
            hasAuxBlockInColors = calculateRemainingCounts(colorCounts);

        for (ColorsCounter.MapColorCount colorCount : colorCounts) {
            addBlockToMaterialList(materialListAdder, palette, colorCount);
        }

        if (displayRemainingAmount && !hasAuxBlockInColors)
            auxBlockCount -= inventoryItemsCounter.getCounts().getOrDefault(MapartHelper.conversionConfig().getAuxBlock().asItem(), 0);

        MutableComponent amountText = Component.literal(getAmountString(Math.max(0, auxBlockCount), auxBlockItemWidget.getStackSize()));
        if (auxBlockCount <= 0)
            amountText.withStyle(ChatFormatting.GREEN);
        auxAmountText.setWidth(screen.getFont().width(amountText));
        auxAmountText.setMessage(amountText);
        auxAmountText.setTooltip(Tooltip.create(amountText));

        materialList.arrangeElements();
        childAdder.accept(materialList);
    }

    private void addBlockToMaterialList(GridLayout.RowHelper adder, PalettePresetsConfig palette, ColorsCounter.MapColorCount color) {
        MapColor mapColor = MapColor.byId(color.id());
        Block block = palette.getBlockOfMapColor(mapColor);
        if (block == null) return;

        MaterialListBlockWidget blockItemWidget = new MaterialListBlockWidget(0, 0, 24, block, mapColor);
        adder.addChild(blockItemWidget, materialList.grid.newCellSettings().paddingLeft(6));
        MutableComponent text = Component.literal(getAmountString(color.amount(), block.asItem().getDefaultMaxStackSize()));
        if (color.amount() == 0)
            text.withStyle(ChatFormatting.GREEN);
        StringWidget amountText = new StringWidget(text, screen.getFont());
        adder.addChild(amountText);
        amountText.setTooltip(Tooltip.create(amountText.getMessage()));
        amountText.setTooltipDelay(Duration.ofMillis(100));

        if (MapartSchematicBuilder.shouldPlaceAuxBlock(block)) {
            auxBlockCount += color.amount();
        }
    }

    /// @return Whether the color-blocks list contains the same block as the auxiliary block.
    private boolean calculateRemainingCounts(ColorsCounter.MapColorCount[] colors) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        boolean hasAuxBlock = false;

        PalettePresetsConfig paletteConfig = PaletteConfigManager.presetsConfig;

        List<Item> countingBlocks = Arrays.stream(colors)
                .map(c -> {
                    Block block = paletteConfig.getBlockOfMapColor(MapColor.byId(c.id()));
                    if (block instanceof LiquidBlock)
                        return BuiltInRegistries.FLUID.getValue(BuiltInRegistries.BLOCK.getKey(block)).getBucket();
                    return block.asItem();
                })
                .toList();

        Item auxBlockItem = MapartHelper.conversionConfig().getAuxBlock().asItem();
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
            text.append(shBoxes).append("§3").append(Component.translatable("maparthelper.gui.shulker_box_abbr").getString()).append("§r");
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
                    block.getName().getVisualOrderText(),
                    Component.translatable("maparthelper.gui.LMB_to_highlight").withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                    Component.translatable("maparthelper.gui.RMB_to_remove").withStyle(ChatFormatting.GRAY).getVisualOrderText()
            ));
            if (Minecraft.getInstance().options.advancedItemTooltips)
                this.tooltip.add(Component.literal(BuiltInRegistries.BLOCK.getKey(block).toString()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(context, mouseX, mouseY, partialTick);
            if (fixedHighlight == this) {
                context.nextStratum();
                RenderUtils.renderOutline(context, getX(), getY(), this.width, this.height, MapartHelper.commonConfig().previewHighlightingColor.getRGB());
            } else if (MapartHelper.commonConfig().previewHighlightOnHover
                    && context.containsPointInScissor(mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
                screen.setHighlightingColor(mapColor);
                hoveringAny = true;
            }
            if (confirmRemoving) {
                RenderUtils.renderItemStack(
                        context,
                        Blocks.BARRIER.asItem().getDefaultInstance(),
                        "RemoveColor",
                        getX(), getY(),
                        width, height
                );
            }
        }
        //~ !gui_rendering

        //~ widget_events
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
                    tooltip.set(1, Component.translatable("maparthelper.gui.LMB_to_confirm").withStyle(ChatFormatting.RED).getVisualOrderText());
                    tooltip.set(2, Component.translatable("maparthelper.gui.RMB_to_cancel").withStyle(ChatFormatting.RED).getVisualOrderText());
                } else {
                    confirmRemoving = false;
                    selectedForExcluding.remove(mapColor);
                    tooltip.set(1, Component.translatable("maparthelper.gui.LMB_to_highlight").withStyle(ChatFormatting.GRAY).getVisualOrderText());
                    tooltip.set(2, Component.translatable("maparthelper.gui.RMB_to_remove").withStyle(ChatFormatting.GRAY).getVisualOrderText());
                }
            }
            return true;
        }
        //~ !widget_events

        public static boolean isHoveringAny() {
            return hoveringAny;
        }

        public static void resetHovering() {
            hoveringAny = false;
        }

        public static void setDefaultHighlight(MapartPreviewWidget mapartPreview) {
            if (fixedHighlight == null)
                mapartPreview.setHighlightingColor(MapColor.NONE);
            else
                mapartPreview.setHighlightingColor(fixedHighlight.mapColor);
        }
    }
}
