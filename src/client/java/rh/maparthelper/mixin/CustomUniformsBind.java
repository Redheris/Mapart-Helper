package rh.maparthelper.mixin;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rh.maparthelper.render.pipeline.ColorsHighlightUniform;
import rh.maparthelper.render.pipeline.MapartImageGridUniform;
import rh.maparthelper.render.pipeline.PainterSelectionUniform;

@Mixin(RenderSystem.class)
public class CustomUniformsBind {

    @Inject(method = "bindDefaultUniforms", at = @At("HEAD"))
    private static void bindDefaultUniforms(RenderPass pass, CallbackInfo ci) {
        pass.setUniform("ColorsHighlight", ColorsHighlightUniform.BUFFER);
        pass.setUniform("MapartImageGrid", MapartImageGridUniform.BUFFER);
        pass.setUniform("PainterSelection", PainterSelectionUniform.BUFFER);
    }
}
