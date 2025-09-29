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
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.conversion.ConvertedMapartImage;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageConverter;

import java.util.List;

public class MapartPreviewWidget extends ClickableWidget {
    private final int maxWidth;
    private final int maxHeight;
    private boolean scaleToCursor = true;
    private final ConvertedMapartImage mapart;

    public MapartPreviewWidget(ConvertedMapartImage mapart, int x, int y, int maxX, int maxY) {
        super(x, y, mapart.getWidth(), mapart.getHeight(), Text.empty());
        this.maxWidth = maxX - x;
        this.maxHeight = maxY - y;
        this.mapart = mapart;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getX();
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
                    x + width / 2, y,
                    0xFF00FF00
            );
        }

        context.drawBorder(x - 1, y - 1, width + 2, height + 2, Colors.CYAN);
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
            int imageWidth = mapart.getOriginalWidth();
            int imageHeight = mapart.getOriginalHeight();
            double mapartAspect = (double) mapart.getWidth() / mapart.getHeight();
            double imageAspect = (double) imageWidth / imageHeight;

            int delta = (int) verticalAmount * 5;
            double scaleX = 0.5;
            double scaleY = 0.5;
            if (this.scaleToCursor) {
                scaleX = (mouseX - getX()) / width;
                scaleY = (mouseY - getY()) / height;
            }

            int minSize = Math.min(imageWidth, Math.min(imageHeight, 64));
            int cropWidth = mapart.getCroppingFrameWidth();
            int cropHeight = mapart.getCroppingFrameHeight();
            int centerX = (int) (mapart.getCroppingFrameX() + cropWidth * scaleX);
            int centerY = (int) (mapart.getCroppingFrameY() + cropHeight * scaleY);

            if (imageAspect < mapartAspect) {
                cropWidth = Math.clamp(cropWidth - 2L * delta, minSize, imageWidth);
                cropHeight = (int) (cropWidth / mapartAspect);
            } else {
                cropHeight = Math.clamp(cropHeight - 2L * delta, minSize, imageHeight);
                cropWidth = (int) (cropHeight * mapartAspect);
            }

            int frameX = Math.clamp(centerX - (int) (cropWidth * scaleX), 0, Math.max(imageWidth - cropWidth, 0));
            int frameY = Math.clamp(centerY - (int) (cropHeight * scaleY), 0, Math.max(imageHeight - cropHeight, 0));

            mapart.setCroppingFrameX(frameX);
            mapart.setCroppingFrameY(frameY);
            mapart.setCroppingFrameWidth(cropWidth);
            mapart.setCroppingFrameHeight(cropHeight);

            MapartImageConverter.updateMapart(mapart);
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
        if (CurrentConversionSettings.cropMode != CroppingMode.USER_CROP || !mapart.hasOriginal() || button != 0)
            return false;

        int diffX = (int) deltaX;
        int diffY = (int) deltaY;
        int imageWidth = mapart.getOriginalWidth();
        int imageHeight = mapart.getOriginalHeight();

        mapart.setCroppingFrameX(Math.clamp(mapart.getCroppingFrameX() - diffX, 0, imageWidth - mapart.getCroppingFrameWidth()));
        mapart.setCroppingFrameY(Math.clamp(mapart.getCroppingFrameY() - diffY, 0, imageHeight - mapart.getCroppingFrameHeight()));
        MapartImageConverter.updateMapart(mapart);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }
}
