package rh.maparthelper.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public class ScaledItemGuiElementRenderer extends PictureInPictureRenderer<ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState> {
    public ScaledItemGuiElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public @NotNull Class<ScaledItemGuiElementRenderState> getRenderStateClass() {
        return ScaledItemGuiElementRenderState.class;
    }

    @Override
    protected void renderToTexture(ScaledItemGuiElementRenderState state, PoseStack matrixStack) {
        matrixStack.translate(0, -0.5, 0);
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        TrackingItemStackRenderState keyedItemRenderState = state.guiItemRenderState().itemStackRenderState();
        boolean isBlock = !keyedItemRenderState.usesBlockLight();
        if (isBlock) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        } else {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        }

        keyedItemRenderState.render(matrixStack, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void blitTexture(ScaledItemGuiElementRenderState state, GuiRenderState guiRenderState) {
        super.blitTexture(state, guiRenderState);
    }

    @Override
    protected @NotNull String getTextureLabel() {
        return "scaled_item";
    }


    public record ScaledItemGuiElementRenderState(GuiItemRenderState guiItemRenderState, int x0, int y0, int x1, int y1,
                                                  float texWidth) implements PictureInPictureRenderState {
        @Override
        public float scale() {
            return this.texWidth;
        }

        @Override
        public @NotNull Matrix3x2f pose() {
            return this.guiItemRenderState.pose();
        }

        @Nullable
        @Override
        public ScreenRectangle scissorArea() {
            return this.guiItemRenderState.scissorArea();
        }

        @Nullable
        @Override
        public ScreenRectangle bounds() {
            return this.guiItemRenderState.bounds();
        }
    }
}