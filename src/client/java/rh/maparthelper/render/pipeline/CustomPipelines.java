package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.renderer.RenderPipelines;
import rh.maparthelper.MapartHelper;

public class CustomPipelines {
    public final static RenderPipeline PREVIEW_COLOR_HIGHLIGHT = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("pipeline/preview_color_highlight"))
            .withFragmentShader(MapartHelper.identifier("core/preview_color_highlight"))
            .withUniform("ColorsHighlight", UniformType.UNIFORM_BUFFER)
            .build();

    public final static RenderPipeline MAPART_IMAGE_GRID = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("pipeline/mapart_image_grid"))
            .withFragmentShader(MapartHelper.identifier("core/mapart_image_grid"))
            .withUniform("MapartImageGrid", UniformType.UNIFORM_BUFFER)
            .build();

    public final static RenderPipeline PAINTER_SELECTION = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(MapartHelper.identifier("pipeline/painter_selection"))
            .withFragmentShader(MapartHelper.identifier("core/painter_selection"))
            .withUniform("PainterSelection", UniformType.UNIFORM_BUFFER)
            .build();
}
