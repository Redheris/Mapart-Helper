package rh.maparthelper.gui.widget;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector4i;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.render.pipeline.CustomPipelines;
import rh.maparthelper.render.pipeline.MapartImageGridUniform;
import rh.maparthelper.state.fullscreen_view.InitialImageViewState;
import rh.maparthelper.state.fullscreen_view.NativeImageViewState;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.RenderUtils;

import java.util.Objects;

//? >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
 *///?}

public class NativeImageViewWidget extends AbstractWidget {
    public static final Identifier TRANSPARENT_TEXTURE = MapartHelper.identifier("textures/gui/background/transparent.png");

    private final Identifier imageId;

    private final int originalWidth;
    private final int originalHeight;

    private final int fittedImageWidth;
    private final int fittedImageHeight;
    private final int fittedImageXOffset;
    private final int fittedImageYOffset;
    private final double maxScale;

    private final Vector2i pixelPos;
    private final NativeImageViewState state = NativeImageViewState.getInstance();

    public NativeImageViewWidget(DynamicTexture imageTexture, Identifier imageId, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());

        NativeImage image = imageTexture == null ? null : imageTexture.getPixels();
        InitialImageViewState savedInitState = state.getInitialState();

        if (savedInitState == null || !Objects.equals(savedInitState.imageId(), imageId)
                || image != null && (image.getWidth() != savedInitState.originalWidth() || (image.getHeight() != savedInitState.originalHeight()))
        ) {
            if (image != null) {
                this.imageId = imageId;
                this.originalWidth = image.getWidth();
                this.originalHeight = image.getHeight();
            } else {
                this.imageId = null;
                this.originalWidth = 128;
                this.originalHeight = 128;
            }
            Vector4i sizeFitted = fitImage();
            this.fittedImageWidth = sizeFitted.x;
            this.fittedImageHeight = sizeFitted.y;
            this.fittedImageXOffset = sizeFitted.z;
            this.fittedImageYOffset = sizeFitted.w;
            this.maxScale = height / ((fittedImageHeight / (float) originalHeight) * 6);
            state.setInitialState(new InitialImageViewState(
                    this.imageId,
                    this.originalWidth,
                    this.originalHeight,
                    this.fittedImageWidth,
                    this.fittedImageHeight,
                    this.fittedImageXOffset,
                    this.fittedImageYOffset,
                    this.maxScale
            ));
            state.setScale(1);
            state.setXOffset(0);
            state.setYOffset(0);
            state.setScaledImageWidth(fittedImageWidth);
            state.setScaledImageHeight(fittedImageHeight);
            state.setPixelWidth(fittedImageWidth / (float) originalWidth);
            state.setPixelHeight(fittedImageHeight / (float) originalHeight);
        } else {
            this.imageId = savedInitState.imageId();
            this.originalWidth = savedInitState.originalWidth();
            this.originalHeight = savedInitState.originalHeight();
            this.fittedImageWidth = savedInitState.fittedImageWidth();
            this.fittedImageHeight = savedInitState.fittedImageHeight();
            this.fittedImageXOffset = savedInitState.fittedImageXOffset();
            this.fittedImageYOffset = savedInitState.fittedImageYOffset();
            this.maxScale = savedInitState.maxScale();
        }
        this.pixelPos = state.pixelPos();
        updateGrid();
    }

    private Vector4i fitImage() {
        if (originalWidth <= 0 || originalHeight <= 0) return new Vector4i(128, 128, 0, 0);

        double aspect = (double) originalWidth / originalHeight;
        double scaleX = (double) width / originalWidth;
        double scaleY = (double) height / originalHeight;

        int widthFitted, heightFitted, xFitted, yFitted;

        if (scaleX < scaleY) {
            // Fit by width
            widthFitted = (int) (width * 0.85);
            xFitted = (width - widthFitted) / 2;
            heightFitted = (int) (widthFitted / aspect);
            yFitted = (height - heightFitted) / 2;
        } else {
            // Fit by height
            heightFitted = (int) (height * 0.85);
            yFitted = (height - heightFitted) / 2;
            widthFitted = (int) (heightFitted * aspect);
            xFitted = (width - widthFitted) / 2;
        }

        return new Vector4i(widthFitted, heightFitted, xFitted, yFitted);
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

    public void updateGrid() {
        if (!state.showMapGrid() && !state.showPixelGrid()) return;
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        MapartImageGridUniform.set(
                getRight() * guiScale, getBottom() * guiScale,
                (int) state.scaledImageWidth() * guiScale,
                (int) state.scaledImageHeight() * guiScale,
                (int) (getInitImageX() + state.xOffset()) * guiScale - 1,
                (int) (getInitImageY() + state.yOffset()) * guiScale,
                // TODO: Make colors custom?
                ARGB.color(0.6f, -1),
                ARGB.color(0.5f, 0),
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
        updateGrid();
    }

    public boolean isShowMapGrid() {
        return state.showMapGrid();
    }

    public void setShowMapGrid(boolean showMapGrid) {
        state.setShowMapGrid(showMapGrid);
        updateGrid();
    }

    public void setOffset(double xOffset, double yOffset) {
        setXOffsetWithoutGridUpdate(xOffset);
        setYOffsetWithoutGridUpdate(yOffset);
        updateGrid();
    }

    public void setXOffset(double xOffset) {
        setXOffsetWithoutGridUpdate(xOffset);
        updateGrid();
    }

    public void setYOffset(double yOffset) {
        setYOffsetWithoutGridUpdate(yOffset);
        updateGrid();
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
    }

    public String pixelPosString() {
        return String.format("(%d, %d)", pixelPos.x, pixelPos.y);
    }

    public Vector2i getPixelPos() {
        return pixelPos;
    }

    protected void calculatePixelPos(double mouseX, double mouseY) {
        double mouseLocalX = mouseX - (getInitImageX() + state.xOffset());
        double mouseLocalY = mouseY - (getInitImageY() + state.yOffset());

        int pixelX = (int) Math.floor(mouseLocalX / state.pixelWidth());
        int pixelY = (int) Math.floor(mouseLocalY / state.pixelHeight());

        pixelPos.set(pixelX, pixelY);
    }

    //~ gui_rendering
    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (imageId == null) return;

        if (!Minecraft.getInstance().isWindowActive()) {
            calculatePixelPos(mouseX, mouseY);
        }

        graphics.enableScissor(getX(), getY(), getRight(), getBottom());

        drawMapartImage(graphics);
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

    protected void drawMapartImage(@NotNull GuiGraphics graphics) {
        int imageX = getInitImageX();
        int imageY = getInitImageY();

        int scissorsX = (int) (imageX + state.xOffset() - 1);
        int scissorsY = (int) (imageY + state.yOffset() - 1);
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
                ARGB.color(0.5f, -1)
        );
        graphics.disableScissor();

        graphics.blit(
                isShowPixelGrid() || isShowMapGrid() ? CustomPipelines.MAPART_IMAGE_GRID : RenderPipelines.GUI_TEXTURED,
                imageId,
                (int) (imageX + state.xOffset()), (int) (imageY + state.yOffset()),
                0.0F, 0.0F,
                (int) (state.scaledImageWidth()), (int) (state.scaledImageHeight()),
                (int) (state.scaledImageWidth()), (int) (state.scaledImageHeight())
        );

    }
    //~ !gui_rendering

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
