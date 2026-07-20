package rh.maparthelper.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

//? if <26.2 {
import net.minecraft.client.renderer.MultiBufferSource;
//?} else
//import net.minecraft.client.renderer.SubmitNodeStorage;

//? if >=1.21.10 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.jspecify.annotations.NonNull;
*///?}

public class ScaledItemGuiElementRenderer extends PictureInPictureRenderer<ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState> {
    //? if <26.2 {
    public ScaledItemGuiElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }
    //?}

    @Override
    public @NotNull Class<ScaledItemGuiElementRenderState> getRenderStateClass() {
        return ScaledItemGuiElementRenderState.class;
    }

    //? if >=26.2 {
    /*@Override
    protected void renderToTexture(ScaledItemGuiElementRenderState state, PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector) {
        poseStack.translate(0, -0.5, 0);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        TrackingItemStackRenderState keyedItemRenderState = state.guiItemRenderState().itemStackRenderState();
        boolean useBlockLight = !keyedItemRenderState.usesBlockLight();
        if (useBlockLight) {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        } else {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        }
        FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.featureRenderDispatcher();
        keyedItemRenderState.submit(
                poseStack, submitNodeCollector, 15728880,
                OverlayTexture.NO_OVERLAY, 0
        );

        renderDispatcher.renderAllFeatures((SubmitNodeStorage) submitNodeCollector);
    }
    *///?} else {
    @Override
    protected void renderToTexture(ScaledItemGuiElementRenderState state, PoseStack matrixStack) {
        matrixStack.translate(0, -0.5, 0);
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        TrackingItemStackRenderState keyedItemRenderState = state.guiItemRenderState().itemStackRenderState();
        boolean useBlockLight = !keyedItemRenderState.usesBlockLight();
        if (useBlockLight) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        } else {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        }

        //? if >=1.21.10 {
        /*FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeCollector nodeCollector = renderDispatcher.getSubmitNodeStorage();
        keyedItemRenderState.submit(
                matrixStack, nodeCollector, 15728880,
                OverlayTexture.NO_OVERLAY, 0
        );
        renderDispatcher.renderAllFeatures();
        *///?} else
        keyedItemRenderState.render(matrixStack, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
    }
    //?}

    @Override
    public void blitTexture(@NotNull ScaledItemGuiElementRenderState state, @NotNull GuiRenderState guiRenderState) {
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