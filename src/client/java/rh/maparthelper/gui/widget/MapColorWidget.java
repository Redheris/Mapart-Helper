package rh.maparthelper.gui.widget;

import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.colors.MapColors;

public class MapColorWidget extends ClickableWidget {
    public final MapColor color;
    protected Text tooltipColorName;
    protected final boolean isHorizontal;

    public MapColorWidget(int x, int y, int width, int height, MapColor color, boolean isHorizontal) {
        super(x, y, width, height, Text.empty());
        this.color = color;
        this.isHorizontal = isHorizontal;
    }

    public void showColorName(boolean enabled) {
        if (!enabled) {
            this.tooltipColorName = null;
        } else {
            this.tooltipColorName = Text.literal(MapColors.findByMapColor(color).name());
            if (tooltipColorName.getString().contains("BLACK")) {
                ((MutableText) tooltipColorName).withColor(MapColor.GRAY.color);
            } else {
                ((MutableText) tooltipColorName).withColor(color.color);
            }
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getX();
        int y = getY();
        if (color == MapColor.CLEAR) {
            context.fill(x, y, x + width, y + height, MapColor.LIGHT_GRAY.getRenderColor(MapColor.Brightness.NORMAL));
            drawScrollableText(context, MinecraftClient.getInstance().textRenderer, Text.translatable("maparthelper.gui.background_color_clear"), getX() + 2, getY(), getRight() - 2, getBottom(), Colors.LIGHT_RED);
        } else if (color == MapColor.WATER_BLUE) {
            context.fill(x, y, x + width, y + height, color.getRenderColor(MapColor.Brightness.NORMAL));
        } else {
            if (!isHorizontal) {
                int segHeight = height / 3;
                context.fill(x, y, x + width, y + segHeight, color.getRenderColor(MapColor.Brightness.LOW));
                context.fill(x, y + segHeight, x + width, y + segHeight * 2, color.getRenderColor(MapColor.Brightness.NORMAL));
                context.fill(x, y + segHeight * 2, x + width, y + height, color.getRenderColor(MapColor.Brightness.HIGH));
            } else {
                int segWidth = width / 3;
                context.fill(x, y, x + segWidth, y + height, color.getRenderColor(MapColor.Brightness.LOW));
                context.fill(x + segWidth, y, x + segWidth * 2, y + height, color.getRenderColor(MapColor.Brightness.NORMAL));
                context.fill(x + segWidth * 2, y, x + width, y + height, color.getRenderColor(MapColor.Brightness.HIGH));
            }
        }
        context.drawBorder(x, y, width, height, 0xFF555555);

        if (tooltipColorName != null && context.scissorContains(mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(tooltipColorName, mouseX, mouseY);
        }
    }
}
