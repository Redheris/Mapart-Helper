package rh.maparthelper.gui.painter.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.widget.NativeImageViewWidget;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.drawing.Rasterizer;
import rh.maparthelper.painter.drawing.tool.*;
import rh.maparthelper.render.pipeline.CustomPipelines;
import rh.maparthelper.render.pipeline.PainterToolAreaUniform;
import rh.maparthelper.state.fullscreen_view.NativeImageViewState;

public final class PainterCursorManager {
    private static final PainterCursorManager INSTANCE = new PainterCursorManager();
    public static final Identifier PAINTER_CURSOR_AREA_TEXTURE = MapartHelper.identifier("mapart_painter_tool_area");
    private static final ReloadableTexture CURSOR_AREA = new SimpleTexture(PAINTER_CURSOR_AREA_TEXTURE);
    private static final NativeImage defaultImage;

    static {
        defaultImage = new NativeImage(1, 1, true);
        defaultImage.setPixel(0, 0, -1);
        CURSOR_AREA.apply(new TextureContents(defaultImage, null));
        Minecraft.getInstance().getTextureManager()
                .register(PAINTER_CURSOR_AREA_TEXTURE, CURSOR_AREA);
    }

    private final ToolSettingsProvider toolSettingsProvider = ToolSettingsProvider.getInstance();

    private boolean isDefaultArea;
    private PainterTool lastTool;
    private int areaSideSize;
    private boolean brushCircleShape;

    private PainterCursorManager() {}

    public static PainterCursorManager getInstance() {
        return INSTANCE;
    }

    public void updateCursorAreaUniform() {
        NativeImageViewState imageState = NativeImageViewState.getPainterInstance();
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        PainterToolAreaUniform.set(
                (float) ((int) (areaSideSize * imageState.pixelWidth()) * guiScale),
                (float) ((int) (areaSideSize * imageState.pixelHeight()) * guiScale),
                lastTool instanceof AbstractSelectionTool
        );
    }

    public void resetState() {
        this.lastTool = null;
    }

    public boolean updateCursor(DrawingEngine<?> drawingEngine) {
        PainterTool selectedTool = drawingEngine.getSelectedTool();

        if (selectedTool instanceof HandTool) {
            if (lastTool == selectedTool) return false;

            isDefaultArea = false;
            lastTool = selectedTool;
            return false;
        }

        if (selectedTool instanceof BrushTool<?> || selectedTool instanceof SelectionBrushTool) {
            isDefaultArea = false;
            var brushSettings = toolSettingsProvider.BRUSH;
            int thickness = brushSettings.getThickness();

            if (selectedTool == lastTool && thickness == areaSideSize && brushSettings.isCircleShape() == brushCircleShape) {
                return false;
            }

            NativeImage cursorAreaMask = new NativeImage(thickness, thickness, true);
            int xCenter = thickness / 2;
            int yCenter = thickness / 2;
            Rasterizer.drawFigureFromCenter(
                    (x, y) -> cursorAreaMask.setPixel(x, y, -1),
                    null,
                    brushSettings.isCircleShape(),
                    thickness,
                    xCenter,
                    yCenter
            );

            lastTool = selectedTool;
            areaSideSize = thickness;
            brushCircleShape = brushSettings.isCircleShape();
            CURSOR_AREA.apply(new TextureContents(cursorAreaMask, null));
            return true;
        }

        if (isDefaultArea) return false;
        isDefaultArea = true;
        lastTool = selectedTool;
        areaSideSize = 1;
        NativeImage defaultImage = new NativeImage(1, 1, true);
        defaultImage.setPixel(0, 0, -1);
        CURSOR_AREA.apply(new TextureContents(defaultImage, null));
        return true;
    }

    public void renderCursorArea(NativeImageViewWidget imageViewWidget, DrawingEngine<?> drawingEngine,
                                 GuiGraphics graphics, int hoveredPixelX, int hoveredPixelY, int closestXLine, int closestYLine
    ) {
        if (updateCursor(drawingEngine)) {
            updateCursorAreaUniform();
        }
        if (lastTool instanceof HandTool) return;

        int xCenter = hoveredPixelX;
        int yCenter = hoveredPixelY;
        if (areaSideSize % 2 == 0) {
            xCenter = closestXLine;
            yCenter = closestYLine;
        }

        NativeImageViewState imageState = NativeImageViewState.getPainterInstance();
        float x = (int) (imageViewWidget.getInitImageX() + imageState.xOffset()) + xCenter * (float) imageState.pixelWidth();
        float y = (int) (imageViewWidget.getInitImageY() + imageState.yOffset()) + yCenter * (float) imageState.pixelHeight();

        graphics.pose().pushMatrix()
                .translate(
                        x - (int) (areaSideSize / 2f) * (float) imageState.pixelWidth(),
                        y - (int) (areaSideSize / 2f) * (float) imageState.pixelHeight()
                )
                .scale((float) imageState.pixelWidth(), (float) imageState.pixelHeight());
        graphics.blit(
                CustomPipelines.PAINTER_TOOL_AREA,
                PAINTER_CURSOR_AREA_TEXTURE,
                0, 0,
                0, 0,
                areaSideSize, areaSideSize,
                areaSideSize, areaSideSize,
                ARGB.white(0.3f)
        );
        graphics.pose().popMatrix();
    }
}
