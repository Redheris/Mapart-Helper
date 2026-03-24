package rh.maparthelper.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.screen.PresetsEditorScreen;
import rh.maparthelper.util.RenderUtils;

import java.util.ArrayList;
import java.util.List;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class BlockItemWidget extends AbstractWidget {
    private final boolean hasClickAction;

    private Block block;
    private Item blockItem;
    protected List<FormattedCharSequence> tooltip;

    public BlockItemWidget(int x, int y, int squareSize, Block block, boolean hasClickAction) {
        super(x, y, squareSize, squareSize, Component.translationArg(block.getName()));
        this.setBlock(block);
        initBlockTooltip();
        this.hasClickAction = hasClickAction;
    }

    protected void initBlockTooltip() {
        List<Component> tooltip = PresetsEditorScreen.getTooltipFromItem(Minecraft.getInstance(), blockItem.getDefaultInstance());
        this.tooltip = new ArrayList<>(tooltip.stream().map(Component::getVisualOrderText).toList());
    }

    public BlockItemWidget(int x, int y, int squareSize, Block block) {
        this(x, y, squareSize, block, false);
    }

    public void setBlock(Block block) {
        this.block = block;
        if (block instanceof LiquidBlock) {
            this.blockItem = BuiltInRegistries.FLUID.getValue(BuiltInRegistries.BLOCK.getKey(block)).getBucket();
        } else {
            this.blockItem = block.asItem();
        }
        initBlockTooltip();
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = new ArrayList<>(List.of(tooltip.getVisualOrderText()));
    }

    public void insertToTooltip(int i, Component tooltip) {
        this.tooltip.add(i, tooltip.getVisualOrderText());
    }

    public int getStackSize() {
        return this.blockItem.getDefaultMaxStackSize();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();

        ItemStack blockItem = this.blockItem.getDefaultInstance();
        RenderUtils.renderItemStack(context, blockItem, blockItem.getItem().getName().toString(), x, y, width, height);

        boolean isMouseOverBlock = mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
        if (context.containsPointInScissor(mouseX, mouseY) && isMouseOverBlock) {
            context.setTooltipForNextFrame(this.tooltip, mouseX, mouseY);
        }
    }

    public Block getBlock() {
        return block;
    }

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.hasClickAction && super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
    }
}
