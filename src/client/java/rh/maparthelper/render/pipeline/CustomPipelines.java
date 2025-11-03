package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelper;

public class CustomPipelines {
    public final static RenderPipeline PREVIEW_COLOR_HIGHLIGHT = RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation(Identifier.of(MapartHelper.MOD_ID, "preview_color_highlight"))
            .withFragmentShader(Identifier.of(MapartHelper.MOD_ID, "core/preview_color_highlight"))
            .withUniform("ColorsHighlight", UniformType.UNIFORM_BUFFER)
            .build();
}
