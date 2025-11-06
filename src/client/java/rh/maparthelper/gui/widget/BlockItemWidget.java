package rh.maparthelper.gui.widget;

import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import rh.maparthelper.gui.PresetsEditorScreen;
import rh.maparthelper.util.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockItemWidget extends ClickableWidget {
    private final boolean hasClickAction;

    private Block block;
    private Item blockItem;
    protected List<OrderedText> tooltip;

    public BlockItemWidget(int x, int y, int squareSize, Block block, boolean hasClickAction) {
        super(x, y, squareSize, squareSize, Text.of(block.getName()));
        this.setBlock(block);
        List<Text> tooltip = PresetsEditorScreen.getTooltipFromItem(MinecraftClient.getInstance(), blockItem.getDefaultStack());
        this.tooltip = new ArrayList<>(tooltip.stream().map(Text::asOrderedText).toList());
        this.hasClickAction = hasClickAction;
    }

    public BlockItemWidget(int x, int y, int squareSize, Block block) {
        this(x, y, squareSize, block, false);
    }

    public void setBlock(Block block) {
        this.block = block;
        if (block instanceof FluidBlock) {
            this.blockItem = Registries.FLUID.get(Registries.BLOCK.getId(block)).getBucketItem();
        } else {
            this.blockItem = block.asItem();
        }
    }

    public void setTooltip(Text tooltip) {
        this.tooltip = new ArrayList<>(List.of(tooltip.asOrderedText()));
    }

    public void insertToTooltip(int i, Text tooltip) {
        this.tooltip.add(i, tooltip.asOrderedText());
    }

    public int getStackSize() {
        return this.blockItem.getMaxCount();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getX();
        int y = getY();

        ItemStack blockItem = this.blockItem.getDefaultStack();
        RenderUtils.renderItemStack(context, blockItem, blockItem.getItem().getName().toString(), x, y, width, height);

        boolean isMouseOverBlock = mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
        if (context.scissorContains(mouseX, mouseY) && isMouseOverBlock) {
            context.drawTooltip(this.tooltip, mouseX, mouseY);
        }
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.hasClickAction && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
