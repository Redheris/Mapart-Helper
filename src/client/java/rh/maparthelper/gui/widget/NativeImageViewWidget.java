package rh.maparthelper.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.render.pipeline.CustomPipelines;
import rh.maparthelper.render.pipeline.MapartImageGridUniform;
import rh.maparthelper.state.fullscreen_view.InitialImageViewState;
import rh.maparthelper.state.fullscreen_view.NativeImageViewState;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.RenderUtils;

//? >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
 *///?}

public class NativeImageViewWidget extends AbstractWidget {
    public static final Identifier TRANSPARENT_TEXTURE = MapartHelper.identifier("textures/gui/background/transparent.png");

    protected final Identifier imageId;

    protected final int originalWidth;
    protected final int originalHeight;

    protected final int fittedImageWidth;
    protected final int fittedImageHeight;
    protected final int fittedImageXOffset;
    protected final int fittedImageYOffset;
    protected final double maxScale;

    protected final Vector2i hoveredPixelPos;
    protected final Vector2i closestHoveredLine;
    protected final NativeImageViewState state;

    public NativeImageViewWidget(NativeImageViewState imageViewState, Identifier imageId, int imageWidth, int imageHeight, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.state = imageViewState;

        state.updateInitialStateIfNeeded(width, height, imageWidth, imageHeight);

        InitialImageViewState initialState = state.getInitialState();
        this.imageId = imageId;
        this.originalWidth = initialState.originalWidth();
        this.originalHeight = initialState.originalHeight();
        this.fittedImageWidth = initialState.fittedImageWidth();
        this.fittedImageHeight = initialState.fittedImageHeight();
        this.fittedImageXOffset = initialState.fittedImageXOffset();
        this.fittedImageYOffset = initialState.fittedImageYOffset();
        this.maxScale = initialState.maxScale();
        this.hoveredPixelPos = state.hoveredPixelPos();
        this.closestHoveredLine = state.closestHoveredLine();
        state.setPixelWidth((int) state.scaledImageWidth() / (float) originalWidth);
        state.setPixelHeight((int) state.scaledImageHeight() / (float) originalHeight);
        updateGridUniform();
    }

    public NativeImageViewWidget(NativeImageViewState imageViewState, DynamicTexture texture, Identifier imageId, int x, int y, int width, int height) {
        this(imageViewState, imageId, getWidth(texture), getHeight(texture), x, y, width, height);
    }

    private static int getWidth(DynamicTexture texture) {
        if (texture == null || texture.getPixels() == null) return 128;
        return texture.getPixels().getWidth();
    }

    private static int getHeight(DynamicTexture texture) {
        if (texture == null || texture.getPixels() == null) return 128;
        return texture.getPixels().getHeight();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (imageId == null) return false;

        double scaleMod = scrollY > 0 ? 1.12 : 1 / 1.12;

        if (CompatUtils.hasControlDown()) {
            setScale(state.scale() * scaleMod, mouseX, mouseY);
        } else if (CompatUtils.hasShiftDown()) {
            setXOffset(state.xOffset() + Math.signum(scrollY) * 30);
        } else {
            setYOffset(state.yOffset() + Math.signum(scrollY) * 30);
        }
        calculatePixelPos(mouseX, mouseY);

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        calculatePixelPos(mouseX, mouseY);
    }

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (imageId == null) return false;
        setOffset(state.xOffset() + dragX, state.yOffset() + dragY);
        return true;
    }
    //~ !widget_events

    protected void updateGridUniform() {
        if (!state.showMapGrid() && !state.showPixelGrid()) return;
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        MapartImageGridUniform.set(
                getRight() * guiScale, getBottom() * guiScale,
                (int) state.scaledImageWidth() * guiScale,
                (int) state.scaledImageHeight() * guiScale,
                (int) (getInitImageX() + state.xOffset()) * guiScale - 1,
                (int) (getInitImageY() + state.yOffset()) * guiScale,
                CommonColors.HIGH_CONTRAST_DIAMOND,
                isShowPixelGrid() && (state.pixelWidth() * guiScale > 6 && state.pixelHeight() * guiScale > 6),
                isShowMapGrid()
        );
    }

    public boolean isShowPixelGrid() {
        return state.showPixelGrid();
    }

    public void setShowPixelGrid(boolean showPixelGrid) {
        state.setShowPixelGrid(showPixelGrid);
        updateGridUniform();
    }

    public boolean isShowMapGrid() {
        return state.showMapGrid();
    }

    public void setShowMapGrid(boolean showMapGrid) {
        state.setShowMapGrid(showMapGrid);
        updateGridUniform();
    }

    public void setOffset(double xOffset, double yOffset) {
        setXOffsetWithoutGridUpdate(xOffset);
        setYOffsetWithoutGridUpdate(yOffset);
        updateGridUniform();
    }

    public void setXOffset(double xOffset) {
        setXOffsetWithoutGridUpdate(xOffset);
        updateGridUniform();
    }

    public void setYOffset(double yOffset) {
        setYOffsetWithoutGridUpdate(yOffset);
        updateGridUniform();
    }

    protected void setXOffsetWithoutGridUpdate(double xOffset) {
        double minVisibleWidth = Math.min(state.scaledImageWidth() / 2.0, width / 2.0);
        state.setXOffset(Math.clamp(
                xOffset,
                -getInitImageX() - state.scaledImageWidth() + minVisibleWidth,
                width - getInitImageX() - minVisibleWidth
        ));
    }

    protected void setYOffsetWithoutGridUpdate(double yOffset) {
        double minVisibleHeight = Math.min(state.scaledImageHeight() / 2.0, height / 2.0);
        state.setYOffset(Math.clamp(
                yOffset,
                -getInitImageY() - state.scaledImageHeight() + minVisibleHeight,
                height - getInitImageY() - minVisibleHeight
        ));
    }

    public int getInitImageX() {
        return getX() + fittedImageXOffset;
    }

    public int getInitImageY() {
        return getY() + fittedImageYOffset;
    }

    public void setScale(double scale, double anchorX, double anchorY) {
        if (imageId == null) return;

        scale = Math.clamp(scale, 0.5, maxScale);
        if (scale == state.scale()) return;

        anchorX = Math.clamp(anchorX, getInitImageX() + state.xOffset(), getInitImageX() + state.xOffset() + state.scaledImageWidth());
        anchorY = Math.clamp(anchorY, getInitImageY() + state.yOffset(), getInitImageY() + state.yOffset() + state.scaledImageHeight());
        double imageLocalX = (anchorX - getInitImageX() - state.xOffset()) / state.scale();
        double imageLocalY = (anchorY - getInitImageY() - state.yOffset()) / state.scale();

        state.setScale(scale);
        state.setScaledImageWidth(fittedImageWidth * scale);
        state.setScaledImageHeight(fittedImageHeight * scale);

        state.setPixelWidth((int) state.scaledImageWidth() / (float) originalWidth);
        state.setPixelHeight((int) state.scaledImageHeight() / (float) originalHeight);

        setOffset(
                anchorX - getInitImageX() - imageLocalX * scale,
                anchorY - getInitImageY() - imageLocalY * scale
        );
    }

    public void setScale(double scale) {
        this.setScale(scale, getInitImageX() + fittedImageWidth / 2.0, getInitImageY() + fittedImageHeight / 2.0);
    }

    public void resetScaleAndOffset() {
        setScale(1.0);
        state.setXOffset(0);
        state.setYOffset(0);
        updateGridUniform();
    }

    public String pixelPosString() {
        return String.format("(%d, %d)", hoveredPixelPos.x, hoveredPixelPos.y);
    }

    protected void calculatePixelPos(double mouseX, double mouseY) {
        double mouseLocalX = mouseX - (getInitImageX() + state.xOffset());
        double mouseLocalY = mouseY - (getInitImageY() + state.yOffset());

        double pixelXDouble = mouseLocalX / state.pixelWidth();
        double pixelYDouble = mouseLocalY / state.pixelHeight();

        hoveredPixelPos.set((int) Math.floor(pixelXDouble), (int) Math.floor(pixelYDouble));
        closestHoveredLine.set((int) Math.round(pixelXDouble), (int) Math.round(pixelYDouble));
    }

    //~ gui_rendering
    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (imageId == null) return;

        if (!Minecraft.getInstance().isWindowActive() && !Minecraft.getInstance().getWindow().isMinimized()
                && mouseX != -1 && mouseY != -1) {
            calculatePixelPos(mouseX, mouseY);
        }

        graphics.enableScissor(getX(), getY(), getRight(), getBottom());

        drawTransparencyChessboard(graphics);
        drawMapartImage(graphics, imageId, 1f);

        RenderUtils.renderOutline(
                graphics,
                (int) (getInitImageX() + state.xOffset() - 1),
                (int) (getInitImageY() + state.yOffset() - 1),
                (int) (state.scaledImageWidth() + 2),
                (int) (state.scaledImageHeight() + 2),
                CommonColors.GRAY
        );
        graphics.disableScissor();
    }

    protected void drawTransparencyChessboard(@NotNull GuiGraphics graphics) {
        int scissorsX = (int) (getInitImageX() + state.xOffset() - 1);
        int scissorsY = (int) (getInitImageY() + state.yOffset() - 1);
        graphics.enableScissor(
                scissorsX,
                scissorsY,
                scissorsX + (int) (state.scaledImageWidth() + 2),
                scissorsY + (int) (state.scaledImageHeight() + 2)
        );
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TRANSPARENT_TEXTURE,
                getX(), getY(),
                0.0F, 0.0F,
                width, height,
                48, 48,
                ARGB.white(0.5f)
        );
        graphics.disableScissor();
    }

    protected void drawMapartImage(@NotNull GuiGraphics graphics, Identifier texture, float alpha) {
        graphics.blit(
                isShowPixelGrid() || isShowMapGrid() ? CustomPipelines.MAPART_IMAGE_GRID : RenderPipelines.GUI_TEXTURED,
                texture,
                (int) (getInitImageX() + state.xOffset()), (int) (getInitImageY() + state.yOffset()),
                0.0F, 0.0F,
                (int) (state.scaledImageWidth()), (int) (state.scaledImageHeight()),
                (int) (state.scaledImageWidth()), (int) (state.scaledImageHeight()),
                ARGB.white(alpha)
        );
    }
    //~ !gui_rendering

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    public ScreenRectangle getImageRectangle() {
        return new ScreenRectangle(
                (int) (getInitImageX() + state.xOffset()),
                (int) (getInitImageY() + state.yOffset()),
                (int) (state.scaledImageWidth()),
                (int) (state.scaledImageHeight())
        );
    }
}
