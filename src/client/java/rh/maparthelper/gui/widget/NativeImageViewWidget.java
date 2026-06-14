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
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.RenderUtils;

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

    protected double scale = 1;
    protected double scaledImageWidth;
    protected double scaledImageHeight;

    private final Vector2i pixelPos = new Vector2i(0, 0);
    private double pixelWidth;
    private double pixelHeight;
    private double xOffset;
    private double yOffset;

    private boolean showPixelGrid = false;
    private boolean showMapGrid = false;

    public NativeImageViewWidget(DynamicTexture imageTexture, Identifier imageId, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        if (imageTexture != null && imageTexture.getPixels() != null) {
            NativeImage nativeImage = imageTexture.getPixels();
            this.originalWidth = nativeImage.getWidth();
            this.originalHeight = nativeImage.getHeight();
            this.imageId = imageId;
        } else {
            this.originalWidth = 128;
            this.originalHeight = 128;
            this.imageId = null;
        }
        Vector4i sizeFitted = fitImage();
        this.fittedImageWidth = sizeFitted.x;
        this.fittedImageHeight = sizeFitted.y;
        this.fittedImageXOffset = sizeFitted.z;
        this.fittedImageYOffset = sizeFitted.w;

        this.scaledImageWidth = fittedImageWidth * scale;
        this.scaledImageHeight = fittedImageHeight * scale;

        this.pixelWidth = (int) scaledImageWidth / (float) originalWidth;
        this.pixelHeight = (int) scaledImageHeight / (float) originalHeight;
        updateGrid();

        this.maxScale = height / (pixelHeight * 6);
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
            setScale(scale * scaleMod, mouseX, mouseY);
        } else if (CompatUtils.hasShiftDown()) {
            setXOffset(xOffset + Math.signum(scrollY) * 30);
        } else {
            setYOffset(yOffset + Math.signum(scrollY) * 30);
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
        setOffset(xOffset + dragX, yOffset + dragY);
        return true;
    }
    //~ !widget_events

    public void updateGrid() {
        if (!showMapGrid && !showPixelGrid) return;
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        MapartImageGridUniform.set(
                getRight() * guiScale, getBottom() * guiScale,
                (int) scaledImageWidth * guiScale, (int) scaledImageHeight * guiScale,
                (int) (getInitImageX() + xOffset) * guiScale - 1, (int) (getInitImageY() + yOffset) * guiScale,
                // TODO: Make colors custom?
                ARGB.color(0.6f, -1),
                ARGB.color(0.5f, 0),
                CommonColors.HIGH_CONTRAST_DIAMOND,
                showPixelGrid && (pixelWidth * guiScale > 6 && pixelHeight * guiScale > 6),
                showMapGrid
        );
    }

    public boolean isShowPixelGrid() {
        return showPixelGrid;
    }

    public void setShowPixelGrid(boolean showPixelGrid) {
        this.showPixelGrid = showPixelGrid;
        updateGrid();
    }

    public boolean isShowMapGrid() {
        return showMapGrid;
    }

    public void setShowMapGrid(boolean showMapGrid) {
        this.showMapGrid = showMapGrid;
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
        double minVisibleWidth = Math.min(scaledImageWidth / 2.0, width / 2.0);
        this.xOffset = Math.clamp(
                xOffset,
                -getInitImageX() - scaledImageWidth + minVisibleWidth,
                width - getInitImageX() - minVisibleWidth
        );
    }

    protected void setYOffsetWithoutGridUpdate(double yOffset) {
        double minVisibleHeight = Math.min(scaledImageHeight / 2.0, height / 2.0);
        this.yOffset = Math.clamp(
                yOffset,
                -getInitImageY() - scaledImageHeight + minVisibleHeight,
                height - getInitImageY() - minVisibleHeight
        );
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
        if (scale == this.scale) return;

        anchorX = Math.clamp(anchorX, getInitImageX() + xOffset, getInitImageX() + xOffset + scaledImageWidth);
        anchorY = Math.clamp(anchorY, getInitImageY() + yOffset, getInitImageY() + yOffset + scaledImageHeight);
        double imageLocalX = (anchorX - getInitImageX() - xOffset) / this.scale;
        double imageLocalY = (anchorY - getInitImageY() - yOffset) / this.scale;

        this.scale = scale;
        this.scaledImageWidth = fittedImageWidth * scale;
        this.scaledImageHeight = fittedImageHeight * scale;

        this.pixelWidth = (int) scaledImageWidth / (float) originalWidth;
        this.pixelHeight = (int) scaledImageHeight / (float) originalHeight;

        setOffset(anchorX - getInitImageX() - imageLocalX * scale,
                anchorY - getInitImageY() - imageLocalY * scale
        );
    }

    public void setScale(double scale) {
        this.setScale(scale, getInitImageX() + fittedImageWidth / 2.0, getInitImageY() + fittedImageHeight / 2.0);
    }

    public void resetScaleAndOffset() {
        setScale(1.0);
        xOffset = 0;
        yOffset = 0;
    }

    public String pixelPosString() {
        return String.format("(%d, %d)", pixelPos.x, pixelPos.y);
    }

    public Vector2i getPixelPos() {
        return pixelPos;
    }

    protected void calculatePixelPos(double mouseX, double mouseY) {
        double mouseLocalX = mouseX - (getInitImageX() + xOffset);
        double mouseLocalY = mouseY - (getInitImageY() + yOffset);

        int pixelX = (int) Math.floor(mouseLocalX / pixelWidth);
        int pixelY = (int) Math.floor(mouseLocalY / pixelHeight);

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
                (int) (getInitImageX() + xOffset - 1), (int) (getInitImageY() + yOffset - 1),
                (int) (scaledImageWidth + 2), (int) (scaledImageHeight + 2),
                CommonColors.GRAY
        );
        graphics.disableScissor();
    }

    protected void drawMapartImage(@NotNull GuiGraphics graphics) {
        int imageX = getInitImageX();
        int imageY = getInitImageY();

        int scissorsX = (int) (imageX + xOffset - 1);
        int scissorsY = (int) (imageY + yOffset - 1);
        graphics.enableScissor(
                scissorsX,
                scissorsY,
                scissorsX + (int) (scaledImageWidth + 2),
                scissorsY + (int) (scaledImageHeight + 2)
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
                showMapGrid || showPixelGrid ? CustomPipelines.MAPART_IMAGE_GRID : RenderPipelines.GUI_TEXTURED,
                imageId,
                (int) (imageX + xOffset), (int) (imageY + yOffset),
                0.0F, 0.0F,
                (int) (scaledImageWidth), (int) (scaledImageHeight),
                (int) (scaledImageWidth), (int) (scaledImageHeight)
        );

    }
    //~ !gui_rendering

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
