package rh.maparthelper.gui.widget;

import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.gui.PresetsEditorScreen;
import rh.maparthelper.render.ScaledItemGuiElementRenderer;

import java.util.ArrayList;
import java.util.List;

public class BlockItemWidget extends ClickableWidget {
    private int x;
    private int y;
    private final int squareSize;
    private final boolean hasClickAction;

    private Block block;
    private Item blockItem;
    private List<OrderedText> tooltip;

    public BlockItemWidget(int x, int y, int squareSize, Block block, boolean hasClickAction) {
        super(x, y, squareSize, squareSize, Text.of(block.getName()));
        this.x = x;
        this.y = y;
        this.squareSize = squareSize;
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
        ItemStack blockItem = this.blockItem.getDefaultStack();
        Matrix3x2fStack matrixStack = context.getMatrices();

        MinecraftClient mc = MinecraftClient.getInstance();
        KeyedItemRenderState keyedItemRenderState = new KeyedItemRenderState();
        mc.getItemModelManager().clearAndUpdate(keyedItemRenderState, blockItem, ItemDisplayContext.GUI, mc.world, mc.player, 0);
        ItemGuiElementRenderState itemRenderState = new ItemGuiElementRenderState(
                blockItem.getItem().getName().toString(),
                new Matrix3x2f(matrixStack),
                keyedItemRenderState,
                x, y,
                context.scissorStack.peekLast()
        );
        context.state.addSpecialElement(new ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState(
                itemRenderState,
                x, y,
                x + width, y + height,
                squareSize
        ));

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
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
