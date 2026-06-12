package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.renderer.RenderPipelines;
import rh.maparthelper.MapartHelper;

public class CustomPipelines {
    public final static RenderPipeline PREVIEW_COLOR_HIGHLIGHT = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("preview_color_highlight"))
            .withFragmentShader(MapartHelper.identifier("core/preview_color_highlight"))
            .withUniform("ColorsHighlight", UniformType.UNIFORM_BUFFER)
            .build();

    public final static RenderPipeline DOTTED_GRID = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("dotted_grid"))
            .withFragmentShader(MapartHelper.identifier("core/dotted_grid"))
            .withUniform("DottedGrid", UniformType.UNIFORM_BUFFER)
            .build();
}
