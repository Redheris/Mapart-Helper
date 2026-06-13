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

    public final static RenderPipeline MAPART_IMAGE_GRID = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("mapart_image_grid"))
            .withFragmentShader(MapartHelper.identifier("core/mapart_image_grid"))
            .withUniform("MapartImageGrid", UniformType.UNIFORM_BUFFER)
            .build();
}
