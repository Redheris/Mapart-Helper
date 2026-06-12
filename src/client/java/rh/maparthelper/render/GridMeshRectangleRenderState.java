package rh.maparthelper.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record GridMeshRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x0,
        int y0,
        int x1,
        int y1,
        float pixelWidth,
        float pixelHeight,
        int guiScale,
        int color1,
        int color2,
        ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    // TODO: I've spent a day on this, but maybe there's a way to use a shader or looped texture instead?
    public GridMeshRectangleRenderState(
            RenderPipeline renderPipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            int x0,
            int y0,
            int x1,
            int y1,
            float pixelWidth,
            float pixelHeight,
            int guiScale,
            int color1,
            int color2,
            ScreenRectangle scissorArea
    ) {
        this(
                renderPipeline, textureSetup, pose,
                x0, y0, x1, y1,
                pixelWidth, pixelHeight,
                guiScale,
                color1, color2,
                scissorArea, getBounds(x0, y0, x1, y1, pose, scissorArea)
        );
    }

    @Override
    public void buildVertices(VertexConsumer consumer, float z) {
        float scaledOne = 1f / guiScale;
        float step = 2 * scaledOne;

        float minX = Math.max(scissorArea.left(), x0);
        float minY = Math.max(scissorArea.top(), y0);
        float maxX = Math.min(scissorArea.right(), x1);
        float maxY = Math.min(scissorArea.bottom(), y1);

        float startX = minX - (minX - x0) % pixelWidth;
        float startY = minY - (minY - y0) % pixelHeight;

        int startColumn = (int) Math.floor((startX - x0) / pixelWidth);
        int startRow = (int) Math.floor((startY - y0) / pixelHeight);

        for (int column = startColumn;; column++) {
            float x = x0 + column * pixelWidth - scaledOne;
            if (x >= maxX) break;
            if (x == x0) continue;

            addQuad(consumer, pose, x, minY, x + scaledOne, maxY + scaledOne, z, color1);
            for (float y = minY; y < maxY; y += step) {
                addQuad(consumer, pose, x, y + scaledOne, x + scaledOne, y + step / 2 + scaledOne, z, color2);
            }
        }
        for (int row = startRow;; row++) {
            float y = y0 + row * pixelHeight - scaledOne;
            if (y >= maxY) break;
            if (y == y0) continue;
            addQuad(consumer, pose, minX, y, maxX + scaledOne, y + scaledOne, z, color1);
            for (float x = minX; x < maxX; x += step) {
                addQuad(consumer, pose, x + scaledOne, y, x + step / 2 + scaledOne,  y + scaledOne, z, color2);
            }
        }
    }

    @Nullable
    private static ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }

    private static void addQuad(
            VertexConsumer consumer,
            Matrix3x2f pose,
            float minX, float minY,
            float maxX, float maxY,
            float z,
            int color
    ) {
        consumer.addVertexWith2DPose(pose, minX, minY, z).setColor(color);
        consumer.addVertexWith2DPose(pose, minX, maxY, z).setColor(color);
        consumer.addVertexWith2DPose(pose, maxX, maxY, z).setColor(color);
        consumer.addVertexWith2DPose(pose, maxX, minY, z).setColor(color);
    }
}
