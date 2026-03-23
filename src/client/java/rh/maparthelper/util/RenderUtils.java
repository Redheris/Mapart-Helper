package rh.maparthelper.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import rh.maparthelper.render.ScaledItemGuiElementRenderer;

public class RenderUtils {
    public static void renderItemStack(GuiGraphics context, ItemStack itemStack, String stateName, int x, int y, int width, int height) {
        Minecraft client = Minecraft.getInstance();
        TrackingItemStackRenderState keyedItemRenderState = new TrackingItemStackRenderState();
        client.getItemModelResolver().updateForTopItem(
                keyedItemRenderState,
                itemStack,
                ItemDisplayContext.GUI,
                client.level,
                client.player,
                0
        );
        GuiItemRenderState itemRenderState = new GuiItemRenderState(
                stateName,
                new Matrix3x2f(context.pose()),
                keyedItemRenderState,
                x, y,
                context.scissorStack.peek()
        );
        context.guiRenderState.submitPicturesInPictureState(new ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState(
                itemRenderState,
                x, y,
                x + width, y + height,
                width
        ));
    }
}
