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
}
