package rh.maparthelper.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.conversion.CurrentConversionSettings;

import java.util.List;

public class MapartPreviewWidget extends ClickableWidget {
    private final int maxWidth;
    private final int maxHeight;

    public MapartPreviewWidget(int x, int y, int maxWidth, int maxHeight) {
        super(x, y, CurrentConversionSettings.getWidth() * 128, CurrentConversionSettings.getHeight() * 128, Text.empty());
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getX();
        int y = getY();

        if (CurrentConversionSettings.guiMapartImage == null) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            Text dropFileText = Text.of("Перетащите файл изображения в окно редактора, чтобы создать мапарт");
            int centerX = x + width / 2;

            List<OrderedText> lines = textRenderer.wrapLines(dropFileText, width - 20);

            for (int i = 0; i < lines.size(); i++) {
                context.drawCenteredTextWithShadow(
                        textRenderer,
                        lines.get(i),
                        centerX, y + 30 + i * 9,
                        5636095
                );
            }
        }

        int mapartWidth = CurrentConversionSettings.getWidth() * 128;
        int mapartHeight = CurrentConversionSettings.getHeight() * 128;

        double previewScale = CurrentConversionSettings.previewScale;
        width = (int) (mapartWidth * previewScale);
        height = (int) (mapartHeight * previewScale);

        double scaleX = maxWidth / (double) width;
        double scaleY = maxHeight / (double) height;

        double adapt = Math.min(scaleX, scaleY);
        if (adapt < 1.0) {
            previewScale *= adapt;
            width = (int) (width * previewScale);
            height = (int) (height * previewScale);
        }

        if (CurrentConversionSettings.guiMapartImage != null) {
            context.drawTexture(
                    RenderLayer::getGuiTexturedOverlay,
                    CurrentConversionSettings.guiMapartId,
                    x, y,
                    0.0F, 0.0F,
                    width, height,
                    width, height
            );
        }

        if (CurrentConversionSettings.doShowGrid) {
            for (int mapX = 1; mapX < mapartWidth / 128; mapX++) {
                int lineX = (int) (x + mapX * 128 * previewScale);
                context.fill(lineX, y, lineX + 1, y + height, Colors.CYAN);
            }
            for (int mapY = 1; mapY < mapartHeight / 128; mapY++) {
                int lineY = (int) (y + mapY * 128 * previewScale);
                context.fill(x, lineY, x + width, lineY + 1, Colors.CYAN);
            }
        }
        context.drawBorder(x - 1, y - 1, width + 2, height + 2, Colors.CYAN);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

}
