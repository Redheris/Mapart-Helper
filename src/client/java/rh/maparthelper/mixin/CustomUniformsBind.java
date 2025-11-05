package rh.maparthelper.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rh.maparthelper.render.pipeline.ColorsHighlightUniform;

@Mixin(RenderSystem.class)
public class CustomUniformsBind {
    @Inject(method = "bindDefaultUniforms", at = @At("HEAD"))
    private static void bindDefaultUniforms(RenderPass pass, CallbackInfo ci) {
        GpuBuffer gpuBuffer = ColorsHighlightUniform.BUFFER;
        if (gpuBuffer != null) {
            pass.setUniform("ColorsHighlight", gpuBuffer);
        }
    }
}
