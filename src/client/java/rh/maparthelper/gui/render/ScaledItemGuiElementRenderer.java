package rh.maparthelper.gui.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public class ScaledItemGuiElementRenderer extends SpecialGuiElementRenderer<ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState> {
    public ScaledItemGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public Class<ScaledItemGuiElementRenderState> getElementClass() {
        return ScaledItemGuiElementRenderState.class;
    }

    protected void render(ScaledItemGuiElementRenderState state, MatrixStack matrixStack) {
        matrixStack.translate(0, -0.5, 0);
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        KeyedItemRenderState keyedItemRenderState = state.guiItemRenderState().state();
        boolean isBlock = !keyedItemRenderState.isSideLit();
        if (isBlock) {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
        } else {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_3D);
        }

        keyedItemRenderState.render(matrixStack, this.vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV);
    }

    public void renderElement(ScaledItemGuiElementRenderState state, GuiRenderState guiRenderState) {
        super.renderElement(state, guiRenderState);
    }

    @Override
    protected String getName() {
        return "scaled_item";
    }


    public record ScaledItemGuiElementRenderState(ItemGuiElementRenderState guiItemRenderState, int x1, int y1, int x2, int y2, float texWidth)
            implements SpecialGuiElementRenderState {
        @Override
        public float scale() {
            return this.texWidth;
        }

        @Override
        public Matrix3x2f pose() {
            return this.guiItemRenderState.pose();
        }

        @Nullable
        @Override
        public ScreenRect scissorArea() {
            return this.guiItemRenderState.scissorArea();
        }

        @Nullable
        @Override
        public ScreenRect bounds() {
            return this.guiItemRenderState.bounds();
        }
    }
}