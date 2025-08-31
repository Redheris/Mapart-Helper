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
import org.joml.Matrix3x2fStack;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.PresetsEditorScreen;

import java.util.ArrayList;
import java.util.List;

public class BlockItemWidget extends ClickableWidget {
    private int x;
    private int y;
    private final int width;
    private final int height;
    private final boolean hasClickAction;

    protected final Block block;
    protected final Item blockItem;
    private List<OrderedText> tooltip;

    public BlockItemWidget(int x, int y, int width, int height, Block block, boolean hasClickAction) {
        super(x, y, width, height, Text.of(block.getName()));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.block = block;
        if (block instanceof FluidBlock) {
            this.blockItem = Registries.FLUID.get(Registries.BLOCK.getId(block)).getBucketItem();
        } else {
            this.blockItem = block.asItem();
        }
        List<Text> tooltip = PresetsEditorScreen.getTooltipFromItem(MinecraftClient.getInstance(), blockItem.getDefaultStack());
        this.tooltip = new ArrayList<>(tooltip.stream().map(Text::asOrderedText).toList());
        this.hasClickAction = hasClickAction;
    }

    public BlockItemWidget(int x, int y, int width, int height, Block block) {
        this(x, y, width, height, block, false);
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
        matrixStack.pushMatrix();

        if (MapartHelper.commonConfig.scaleBlockWidgets) {
            matrixStack.translate(x, y);
            matrixStack.scale(width / 16f, height / 16f);
            matrixStack.translate(-x, -y);
        } else {
            matrixStack.translate(4, 4);
        }
        context.drawItem(blockItem, x, y);

        matrixStack.popMatrix();

//        mc.getItemModelManager().clearAndUpdate(keyedItemRenderState, blockItem, ItemDisplayContext.GUI, mc.world, mc.player, 0);
//        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
//        renderer.renderItem(
//                blockItem,
//                ItemDisplayContext.GUI,
//                0xF000F0,
//                OverlayTexture.DEFAULT_UV,
//                itemMatrices,
//                vertexConsumers,
//                mc.world,
//                0
//        );

        boolean isMouseOverBlock = mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
        if (context.scissorContains(mouseX, mouseY) && isMouseOverBlock) {
            context.drawTooltip(this.tooltip, mouseX, mouseY);
        }
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
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
