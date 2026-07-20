package rh.maparthelper.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import rh.maparthelper.render.ScaledItemGuiElementRenderer;

public class RenderUtils {
    public static void renderItemStack(GuiGraphics graphics, ItemStack itemStack, String stateName, int x, int y, int width, int height) {
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
                //? <26.1
                stateName,
                new Matrix3x2f(graphics.pose()),
                keyedItemRenderState,
                x, y,
                graphics.scissorStack.peek()
        );
        //~ if >=26.1 'submitPicturesInPictureState' -> 'addPicturesInPictureState'
        graphics.guiRenderState.submitPicturesInPictureState(new ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState(
                itemRenderState,
                x, y,
                x + width, y + height,
                width
        ));
    }

    public static void renderOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public static void centeredText(GuiGraphics graphics, final Font font, final String str, final int x, final int y, final int color) {
        //~ if >=26.1 'drawCenteredString' -> 'centeredText'
        graphics.drawCenteredString(font, str, x, y, color);
    }

    public static void centeredText(GuiGraphics graphics, final Font font, final FormattedCharSequence text, final int x, final int y, final int color) {
        //~ if >=26.1 'drawCenteredString' -> 'centeredText'
        graphics.drawCenteredString(font, text, x, y, color);
    }

    public static boolean pointInCurrentScissor(GuiGraphics graphics, int x, int y) {
        ScreenRectangle scissors = graphics.scissorStack.peek();
        return scissors == null || scissors.containsPoint(x, y);
    }
}
