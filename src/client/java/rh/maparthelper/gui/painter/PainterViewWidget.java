package rh.maparthelper.gui.painter;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.painter.cursor.PainterCursorManager;
import rh.maparthelper.gui.widget.NativeImageViewWidget;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.drawing.tool.AbstractSelectionTool;
import rh.maparthelper.painter.drawing.tool.HandTool;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.render.pipeline.CustomPipelines;
import rh.maparthelper.render.pipeline.PainterSelectionUniform;
import rh.maparthelper.state.fullscreen_view.NativeImageViewState;

import java.util.BitSet;

//? if >=1.21.10 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

public class PainterViewWidget extends NativeImageViewWidget {
    private final DrawingEngine<NativeImageSurface> drawingEngine;
    private final LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager;
    private boolean dirtySelectionTexture = false;
    private int lastMouseButton = -1;

    public PainterViewWidget(PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject, int x, int y, int width, int height) {
        super(NativeImageViewState.getPainterInstance(),
                painterProject.getLayerManager().getSelectedLayer().getTextureId(),
                painterProject.getLayerManager().getSelectedLayer().getSurface().getWidth(),
                painterProject.getLayerManager().getSelectedLayer().getSurface().getHeight(),
                x, y,
                width, height);
        Layer<NativeImageSurface> layer = painterProject.getLayerManager().getSelectedLayer();
        this.drawingEngine = painterProject.getDrawingEngine();
        this.layerManager = painterProject.getLayerManager();

        drawingEngine.selection.setSize(layer.getSurface());

        PainterScreen.selectionMask = new DynamicTexture("mapart_painter_selection_mask", originalWidth, originalHeight, false);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.register(PainterScreen.SELECTION_MASK_ID, PainterScreen.selectionMask);

        updateSelectionUniform();
        updateSelectionMaskTexture();
        PainterCursorManager.getInstance().resetState();
    }

    protected void updateSelectionUniform() {
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        PainterSelectionUniform.set(
                (int) state.scaledImageWidth() * guiScale,
                (int) state.scaledImageHeight() * guiScale,
                ARGB.color(80, 0xff_8eb1ff),
                drawingEngine.getSelectedTool() instanceof AbstractSelectionTool
        );
    }

    @Override
    public void setScale(double scale, double anchorX, double anchorY) {
        super.setScale(scale, anchorX, anchorY);
        updateSelectionUniform();
        PainterCursorManager.getInstance().updateCursorAreaUniform();
    }

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && !drawingEngine.isProcessing()) {
            lastMouseButton = button;
            drawingEngine.start(
                    hoveredPixelPos.x, hoveredPixelPos.y,
                    closestHoveredLine.x, closestHoveredLine.y,
                    lastMouseButton == 1
            );
            if (drawingEngine.getSelectedTool() instanceof AbstractSelectionTool) {
                updateSelectionMaskTexture();
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if ((button == 0 || button == 1) && !(drawingEngine.getSelectedTool() instanceof HandTool)) {
            drawingEngine.process(
                    hoveredPixelPos.x, hoveredPixelPos.y,
                    closestHoveredLine.x, closestHoveredLine.y,
                    lastMouseButton == 1
            );
            if (drawingEngine.getSelectedTool() instanceof AbstractSelectionTool) {
                updateSelectionMaskTexture();
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            drawingEngine.submit();
            if (drawingEngine.getSelectedTool() instanceof AbstractSelectionTool) {
                updateSelectionMaskTexture();
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    //~ !widget_events

    //~ gui_rendering
    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
    //~ !gui_rendering

    @Override
    protected void drawMapartImage(@NotNull GuiGraphics graphics, Identifier texture, float alpha) {
        for (DynamicTextureLayer layer : layerManager.getLayers()) {
            if (!layer.isVisible()) continue;
            layer.upload();
            super.drawMapartImage(graphics, layer.getTextureId(), layer.getAlpha());
        }

        if (drawingEngine.selection.isActive()) {
            if (dirtySelectionTexture) {
                PainterScreen.selectionMask.upload();
                dirtySelectionTexture = false;
            }
            graphics.blit(
                    CustomPipelines.PAINTER_SELECTION,
                    PainterScreen.SELECTION_MASK_ID,
                    (int) (getInitImageX() + state.xOffset()), (int) (getInitImageY() + state.yOffset()),
                    0.0F, 0.0F,
                    (int) (state.scaledImageWidth()), (int) (state.scaledImageHeight()),
                    (int) (state.scaledImageWidth()), (int) (state.scaledImageHeight())
            );
        }

        graphics.enableScissor(
                (int) (getInitImageX() + state.xOffset()),
                (int) (getInitImageY() + state.yOffset()),
                (int) (getInitImageX() + state.xOffset()) + (int) (state.scaledImageWidth()),
                (int) (getInitImageY() + state.yOffset()) + (int) (state.scaledImageHeight())
        );
        PainterCursorManager.getInstance().renderCursorArea(
                this,
                drawingEngine,
                graphics,
                hoveredPixelPos.x,
                hoveredPixelPos.y,
                closestHoveredLine.x,
                closestHoveredLine.y
        );
        graphics.disableScissor();
    }

    protected void updateSelectionMaskTexture() {
        BitSet selectionMask = drawingEngine.selection.getSelectionMask();
        NativeImage selectionMaskImage = new NativeImage(originalWidth, originalHeight, true);
        selectionMask.stream().forEach(id -> {
            int x = id % originalWidth;
            int y = id / originalWidth;
            selectionMaskImage.setPixel(x, y, -1);
        });
        dirtySelectionTexture = true;
        PainterScreen.selectionMask.setPixels(selectionMaskImage);
    }
}
