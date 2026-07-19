package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.renderer.RenderPipelines;
import rh.maparthelper.MapartHelper;

//? >=26.2
//import com.mojang.blaze3d.pipeline.BindGroupLayout;

public class CustomPipelines {
    public final static RenderPipeline PREVIEW_COLOR_HIGHLIGHT = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("pipeline/preview_color_highlight"))
            .withFragmentShader(MapartHelper.identifier("core/preview_color_highlight"))
            //? if >=26.2 {
            /*.withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("ColorsHighlight", UniformType.UNIFORM_BUFFER)
                    .build())
            *///?} else
            .withUniform("ColorsHighlight", UniformType.UNIFORM_BUFFER)
            .build();

    public final static RenderPipeline MAPART_IMAGE_GRID = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(MapartHelper.identifier("pipeline/mapart_image_grid"))
            .withFragmentShader(MapartHelper.identifier("core/mapart_image_grid"))
            //? if >=26.2 {
            /*.withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("MapartImageGrid", UniformType.UNIFORM_BUFFER)
                    .build())
            *///?} else
            .withUniform("MapartImageGrid", UniformType.UNIFORM_BUFFER)
            .build();

    public final static RenderPipeline PAINTER_SELECTION = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET/*? if <26.2 {*/, RenderPipelines.GLOBALS_SNIPPET/*?}*/)
            .withLocation(MapartHelper.identifier("pipeline/painter_selection"))
            .withFragmentShader(MapartHelper.identifier("core/painter_selection"))
            //? if >=26.2 {
            /*.withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("PainterSelection", UniformType.UNIFORM_BUFFER)
                    .build())
            *///?} else
            .withUniform("PainterSelection", UniformType.UNIFORM_BUFFER)
            .build();

    public final static RenderPipeline PAINTER_TOOL_AREA = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET/*? if <26.2 {*/, RenderPipelines.GLOBALS_SNIPPET/*?}*/)
            .withLocation(MapartHelper.identifier("pipeline/painter_tool_area"))
            .withFragmentShader(MapartHelper.identifier("core/painter_tool_area"))
            //? if >=26.2 {
            /*.withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("PainterToolArea", UniformType.UNIFORM_BUFFER)
                    .build())
            *///?} else
            .withUniform("PainterToolArea", UniformType.UNIFORM_BUFFER)
            .build();
}
