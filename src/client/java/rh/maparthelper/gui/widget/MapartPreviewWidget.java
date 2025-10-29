package rh.maparthelper.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageConverter;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.scheduler.DelayedRepeater;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static rh.maparthelper.gui.sprites.ManualCroppingSprites.*;

public class MapartPreviewWidget extends ClickableWidget {
    private final ConvertedMapartImage mapart;
    private final int maxWidth;
    private final int maxHeight;

    private final DelayedRepeater repeater = new DelayedRepeater();

    private boolean scaleToCursor = true;
    private ManualCroppingAction hoveredAction = null;
    private boolean hoveringAction = false;

    public MapartPreviewWidget(ConvertedMapartImage mapart, int x, int y, int maxX, int maxY) {
        super(x, y, mapart.getWidth(), mapart.getHeight(), Text.empty());
        this.maxWidth = maxX - 16 - x;
        this.maxHeight = maxY - y;
        this.mapart = mapart;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getImageX();
        int y = getY();

        int mapartWidth = mapart.getWidth() * 128;
        int mapartHeight = mapart.getHeight() * 128;
        double scale = 2.0;
        width = (int) (mapartWidth * scale);
        height = (int) (mapartHeight * scale);

        if (width > maxWidth || height > maxHeight) {
            scale = Math.min(maxWidth / (double) mapartWidth, maxHeight / (double) mapartHeight);
            width = (int) (mapartWidth * scale);
            height = (int) (mapartHeight * scale);
        }

        if (CurrentConversionSettings.guiMapartImage != null) {
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    CurrentConversionSettings.guiMapartId,
                    x, y,
                    0.0F, 0.0F,
                    width, height,
                    width, height
            );
        } else if (!MapartImageConverter.isConverting()) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            Text dropFileText = Text.translatable("maparthelper.gui.drop_here_mapart");
            int centerX = x + width / 2;
            List<OrderedText> lines = textRenderer.wrapLines(dropFileText, width - 5);
            for (int i = 0; i < lines.size(); i++) {
                context.drawCenteredTextWithShadow(
                        textRenderer,
                        lines.get(i),
                        centerX, y + 5 + i * 9,
                        0xff55ffff
                );
            }
        }

        if (CurrentConversionSettings.doShowGrid) {
            for (int mapX = 1; mapX < mapartWidth / 128; mapX++) {
                int lineX = (int) (x + mapX * 128 * scale);
                context.fill(lineX, y, lineX + 1, y + height, Colors.CYAN);
            }
            for (int mapY = 1; mapY < mapartHeight / 128; mapY++) {
                int lineY = (int) (y + mapY * 128 * scale);
                context.fill(x, lineY, x + width, lineY + 1, Colors.CYAN);
            }
        }

        if (MapartImageConverter.isConverting()) {
            double conversionProgress = MapartImageConverter.getConversionProgress();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.fill(x, y, (int) (x + width * conversionProgress), getBottom(), 0x3000FF00);
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    (int) (conversionProgress * 100) + "%",
                    x + width / 2, y + 14,
                    0xFF00FF00
            );
        }

        if (CurrentConversionSettings.guiMapartImage != null && CurrentConversionSettings.cropMode == CroppingMode.USER_CROP) {
            renderManualCroppingButtons(context, mouseX, mouseY);
        }

        context.drawBorder(x - 1, y - 1, width + 2, height + 2, Colors.CYAN);
    }
    
    public int getImageX() {
        return getX() + 16;
    }

    @Override
    public int getWidth() {
        return width + 16;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scaleImageCrop(mouseX, mouseY, verticalAmount);
    }

    private boolean scaleImageCrop(double mouseX, double mouseY, double verticalAmount) {
        if (CurrentConversionSettings.cropMode == CroppingMode.USER_CROP && CurrentConversionSettings.guiMapartImage != null) {
            if (scaleToCursor) {
                double pointX = (mouseX - getImageX()) / width;
                double pointY = (mouseY - getY()) / height;
                MapartImageUpdater.scaleToPoint(mapart, pointX, pointY, verticalAmount);
            } else {
                MapartImageUpdater.scaleToCenter(mapart, verticalAmount);
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            this.scaleToCursor = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            this.scaleToCursor = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (CurrentConversionSettings.cropMode != CroppingMode.USER_CROP || button != 0)
            return false;
        if (mouseX < getImageX() || isMouseOverActionsArea(mouseX, mouseY))
            return false;
        setHoveredAction(null);
        MapartImageUpdater.moveCroppingFrameOrMapartImage(mapart, deltaX, deltaY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (CurrentConversionSettings.cropMode != CroppingMode.USER_CROP)
            return true;
        if (hoveredAction != null && button == 0) {
            hoveredAction.perform(mapart);
            repeater.start(() -> hoveredAction.perform(mapart), 500, 100);
            return true;
        }
        return !hoveringAction;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        hoveringAction = false;
        repeater.stop();
    }

    private boolean isMouseOverActionsArea(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY) || mouseX < getImageX())
            return false;
        float xCenter = getImageX() + width / 2.f;
        float yCenter = getY() + height / 2.f;
        return (Math.abs(xCenter - mouseX) < 15 && Math.abs(yCenter - mouseY) < 15)
                || mouseX < getImageX() + 14 || mouseY < getY() + 14 || mouseX > getRight() - 14 || mouseY > getBottom() - 14;
    }

    private void setHoveredAction(ManualCroppingAction action) {
        if (hoveredAction != action) {
            hoveredAction = action;
            if (action != null)
                hoveringAction = true;
        }
    }

    private void renderSprite(DrawContext context, Identifier sprite, int x, int y, int width, int height, int alpha) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                sprite,
                x, y,
                0.0F, 0.0F,
                width, height,
                width, height,
                alpha << 24 | 0x00FFFFFF
        );
    }

    private void renderSprite(DrawContext context, Identifier sprite, int x, int y, int width, int height) {
        renderSprite(context, sprite, x, y, width, height, 255);
    }

    private void renderManualCroppingButtons(DrawContext context, int mouseX, int mouseY) {
        int size = 50;
        int mLeft = mouseX - getImageX();
        int mTop = mouseY - getY();
        int mRight = getRight() - mouseX;
        int mBottom = getBottom() - mouseY;
        boolean highlight = isMouseOverActionsArea(mouseX, mouseY);

        // Two-axis moving buttons

        renderEdgeArrow(context, mLeft, mTop, getImageX(), getY(),
                ManualCroppingAction.LEFT_UP_BIG, ManualCroppingAction.LEFT_UP_SMALL,
                highlight, size
        );
        renderEdgeArrow(context, mRight, mTop, getRight() - size, getY(),
                ManualCroppingAction.RIGHT_UP_BIG, ManualCroppingAction.RIGHT_UP_SMALL,
                highlight, size
        );
        renderEdgeArrow(context, mLeft, mBottom, getImageX(), getBottom() - size,
                ManualCroppingAction.LEFT_DOWN_BIG, ManualCroppingAction.LEFT_DOWN_SMALL,
                highlight, size
        );
        renderEdgeArrow(context, mRight, mBottom, getRight() - size, getBottom() - size,
                ManualCroppingAction.RIGHT_DOWN_BIG, ManualCroppingAction.RIGHT_DOWN_SMALL,
                highlight, size
        );

        // Moving along a single axis buttons

        int xCenter = getImageX() + width / 2;
        int yCenter = getY() + height / 2;

        int xStart = xCenter - size / 2;
        int yStart = yCenter - size / 2;
        int mStartX = mouseX - xStart;
        int mStartY = mouseY - yStart;

        renderAxisArrow(context, mStartX, mTop, xStart, getY(),
                ManualCroppingAction.UP_BIG, ManualCroppingAction.UP_SMALL,
                highlight, size
        );
        renderAxisArrow(context, mStartY, mRight, getRight() - size, yStart,
                ManualCroppingAction.RIGHT_BIG, ManualCroppingAction.RIGHT_SMALL,
                highlight, size
        );
        renderAxisArrow(context, mStartX, mBottom, xStart, getBottom() - size,
                ManualCroppingAction.DOWN_BIG, ManualCroppingAction.DOWN_SMALL,
                highlight, size
        );
        renderAxisArrow(context, mStartY, mLeft, getImageX(), yStart,
                ManualCroppingAction.LEFT_BIG, ManualCroppingAction.LEFT_SMALL,
                highlight, size
        );

        // Zooming buttons

        renderManualCroppingActionSprite(
                context, ManualCroppingAction.ZOOM_IN, getX(), getY(), 14, 14,
                () -> mouseX < getImageX() && mouseX > getX() && mouseY > getY() && mouseY < getY() + 14
        );
        renderManualCroppingActionSprite(
                context, ManualCroppingAction.ZOOM_OUT, getX(), getY() + 14, 14, 14,
                () -> mouseX < getImageX() && mouseX > getX() && mouseY > getY() + 14 && mouseY < getY() + 28
        );

        // Centering buttons

        double dist = Math.sqrt((xCenter - mouseX) * (xCenter - mouseX) + (yCenter - mouseY) * (yCenter - mouseY));
        int alpha = isMouseOver(mouseX, mouseY) ? dist < width / 3.0 ? Math.clamp((int) (255 / dist * 16), 0, 255) : 30 : 0;

        renderManualCroppingActionSprite(
                context, ManualCroppingAction.CENTER_IMAGE, xCenter - 16, yCenter - 16, 32, 32, alpha,
                () -> Math.abs(xCenter - mouseX - 1) <= 8 && Math.abs(yCenter - mouseY - 1) <= 8
        );
        renderManualCroppingActionSprite(
                context, ManualCroppingAction.FIT_BY_WIDTH, xCenter - 16, yCenter - 16, 32, 32, alpha,
                () -> Math.abs(xCenter - mouseX - 1) <= 8 && mouseY > yCenter - 20 && mouseY < yCenter - 8
        );
        renderManualCroppingActionSprite(
                context, ManualCroppingAction.FIT_BY_HEIGHT, xCenter - 16, yCenter - 16, 32, 32, alpha,
                () -> Math.abs(yCenter - mouseY - 1) <= 8 && mouseX > xCenter - 20 && mouseX < xCenter - 8
        );

        if (!hoveringAction) {
            setHoveredAction(null);
        }
    }

    private void renderManualCroppingActionSprite(DrawContext context, ManualCroppingAction action, int x, int y,
                                    int width, int height, int alpha, Supplier<Boolean> isHovered) {
        if (isHovered.get()) {
            renderSprite(context, action.highlighted, x, y, width, height);
            setHoveredAction(action);
        } else
            renderSprite(context, action.normal, x, y, width, height, alpha);
    }

    private void renderManualCroppingActionSprite(DrawContext context, ManualCroppingAction action, int x, int y,
                                    int width, int height, Supplier<Boolean> isHovered) {
        renderManualCroppingActionSprite(context, action, x, y, width, height, 255, isHovered);
    }

    private void renderEdgeArrow(DrawContext context, int dx, int dy, int x, int y,
                                 ManualCroppingAction bigArrow, ManualCroppingAction smallArrow,
                                 boolean highlight, int size) {
        renderManualCroppingActionSprite(
                context, bigArrow, x, y,size, size,
                () -> highlight && (dx < size && dy < 7 || dx < 7 && dy < size)
                );
        renderManualCroppingActionSprite(
                context, smallArrow, x, y,size, size,
                () -> dx > 6 && dy > 6 && (dx < 14 + 4 && dy < 39 || dx < 39 && dy < 14 + 4)
        );
    }

    private void renderAxisArrow(DrawContext context, int dStart, int dSide, int x, int y,
                                 ManualCroppingAction bigArrow, ManualCroppingAction smallArrow,
                                 boolean highlight, int size) {
        renderManualCroppingActionSprite(
                context, bigArrow, x, y,size, size,
                () -> highlight && (dStart >= 0 && dStart <= size && dSide < 9)
        );
        renderManualCroppingActionSprite(
                context, smallArrow, x, y,size, size,
                () -> dSide > 8 && dSide < 14 + 4 && dStart >= 8 && dStart <= 42
        );
    }

    private enum ManualCroppingAction {
        LEFT_UP_BIG(
                LEFT_UP_BIG_TEXTURE, LEFT_UP_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 5, 5)
        ),
        LEFT_UP_SMALL(
                LEFT_UP_SMALL_TEXTURE, LEFT_UP_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 1, 1)
        ),
        RIGHT_UP_BIG(
                RIGHT_UP_BIG_TEXTURE, RIGHT_UP_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, -5, 5)
        ),
        RIGHT_UP_SMALL(
                RIGHT_UP_SMALL_TEXTURE, RIGHT_UP_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, -1, 1)
        ),
        LEFT_DOWN_BIG(
                LEFT_DOWN_BIG_TEXTURE, LEFT_DOWN_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 5, -5)
        ),
        LEFT_DOWN_SMALL(
                LEFT_DOWN_SMALL_TEXTURE, LEFT_DOWN_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 1, -1)
        ),
        RIGHT_DOWN_BIG(
                RIGHT_DOWN_BIG_TEXTURE, RIGHT_DOWN_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, -5, -5)
        ),
        RIGHT_DOWN_SMALL
                (RIGHT_DOWN_SMALL_TEXTURE, RIGHT_DOWN_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, -1, -1)
        ),
        UP_BIG(
                UP_BIG_TEXTURE, UP_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 0, 5)
        ),
        UP_SMALL(
                UP_SMALL_TEXTURE, UP_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 0, 1)
        ),
        LEFT_BIG(
                LEFT_BIG_TEXTURE, LEFT_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 5, 0)
        ),
        LEFT_SMALL(
                LEFT_SMALL_TEXTURE, LEFT_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 1, 0)
        ),
        RIGHT_BIG(
                RIGHT_BIG_TEXTURE, RIGHT_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, -5, 0)
        ),
        RIGHT_SMALL(
                RIGHT_SMALL_TEXTURE, RIGHT_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, -1, 0)
        ),
        DOWN_BIG(
                DOWN_BIG_TEXTURE, DOWN_BIG_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 0, -5)
        ),
        DOWN_SMALL(
                DOWN_SMALL_TEXTURE, DOWN_SMALL_HIGHLIGHTED_TEXTURE,
                mapart -> move(mapart, 0, -1)
        ),
        CENTER_IMAGE(
                CENTER_IMAGE_TEXTURE, CENTER_IMAGE_HIGHLIGHTED_TEXTURE,
                mapart -> {}
        ),
        FIT_BY_WIDTH(
                FIT_BY_WIDTH_TEXTURE, FIT_BY_WIDTH_HIGHLIGHTED_TEXTURE,
                mapart -> {}
        ),
        FIT_BY_HEIGHT(
                FIT_BY_HEIGHT_TEXTURE, FIT_BY_HEIGHT_HIGHLIGHTED_TEXTURE,
                mapart -> {}
        ),
        ZOOM_IN(
                ZOOM_IN_TEXTURE, ZOOM_IN_HIGHLIGHTED_TEXTURE,
                mapart -> MapartImageUpdater.scaleToCenter(mapart, 1)
        ),
        ZOOM_OUT(
                ZOOM_OUT_TEXTURE, ZOOM_OUT_HIGHLIGHTED_TEXTURE,
                mapart -> MapartImageUpdater.scaleToCenter(mapart, -1)
        );

        final Identifier normal;
        final Identifier highlighted;
        final Consumer<ConvertedMapartImage> action;

        ManualCroppingAction(Identifier normal, Identifier highlighted, Consumer<ConvertedMapartImage> action) {
            this.normal = normal;
            this.highlighted = highlighted;
            this.action = action;
        }

        void perform(ConvertedMapartImage mapart) {
            action.accept(mapart);
        }

        static void move(ConvertedMapartImage mapart, int dx, int dy) {
            MapartImageUpdater.moveCroppingFrameOrMapartImage(mapart, dx, dy);
        }
    }
}
