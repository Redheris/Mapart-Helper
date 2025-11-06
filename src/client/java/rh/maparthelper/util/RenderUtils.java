package rh.maparthelper.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2f;
import rh.maparthelper.render.ScaledItemGuiElementRenderer;

public class RenderUtils {
    public static void renderItemStack(DrawContext context, ItemStack itemStack, String stateName, int x, int y, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        KeyedItemRenderState keyedItemRenderState = new KeyedItemRenderState();
        client.getItemModelManager().clearAndUpdate(
                keyedItemRenderState,
                itemStack,
                ItemDisplayContext.GUI,
                client.world,
                client.player,
                0
        );
        ItemGuiElementRenderState itemRenderState = new ItemGuiElementRenderState(
                stateName,
                new Matrix3x2f(context.getMatrices()),
                keyedItemRenderState,
                x, y,
                context.scissorStack.peekLast()
        );
        context.state.addSpecialElement(new ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState(
                itemRenderState,
                x, y,
                x + width, y + height,
                width
        ));
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}
