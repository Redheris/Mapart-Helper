package rh.maparthelper.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.MapartHelper;

public class RenderUtils {
    public static void renderItemStack(DrawContext context, ItemStack itemStack, String stateName, int x, int y, int width, int height) {
        Matrix3x2fStack matrixStack = context.getMatrices();
        matrixStack.pushMatrix();

        if (MapartHelper.commonConfig.mapartEditor.scaleBlockWidgets) {
            matrixStack.translate(x, y);
            matrixStack.scale(width / 16f, height / 16f);
            matrixStack.translate(-x, -y);
        } else {
            matrixStack.translate(4, 4);
        }
        context.drawItem(itemStack, x, y);

        matrixStack.popMatrix();
    }
}
