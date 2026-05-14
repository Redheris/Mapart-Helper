package rh.maparthelper.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class RenderUtils {
    public static void renderItemStack(GuiGraphics context, ItemStack itemStack, int x, int y, int width, int height) {
        PoseStack poseStack = context.pose();

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(width / 16f, height / 16f, 1f);
        poseStack.translate(-x, -y, 0);

        context.renderItem(itemStack, x, y);

        poseStack.popPose();
    }

    public static void nextStratum(GuiGraphics guiGraphics, int count, Runnable renderStuff) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 300 * count);

        renderStuff.run();
        poseStack.translate(0, 0, -300 * count);

        poseStack.popPose();
    }

    public static void nextStratum(GuiGraphics guiGraphics, Runnable renderStuff) {
        nextStratum(guiGraphics, 1, renderStuff);
    }

    public static void renderOutline(GuiGraphics context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public static void centeredText(GuiGraphics context, final Font font, final String str, final int x, final int y, final int color) {
        context.drawCenteredString(font, str, x, y, color);
    }

    public static void centeredText(GuiGraphics context, final Font font, final FormattedCharSequence text, final int x, final int y, final int color) {
        context.drawCenteredString(font, text, x, y, color);
    }
}
