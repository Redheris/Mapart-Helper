package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.colors.MapColors;

//? <=1.21.8
import net.minecraft.client.Minecraft;
//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;
import rh.maparthelper.util.RenderUtils;

public class MapColorWidget extends AbstractWidget {
    public final MapColor color;
    protected Component tooltipColorName;
    protected final boolean isHorizontal;
    protected boolean onlyNormalBrightness = false;

    public MapColorWidget(int x, int y, int width, int height, MapColor color, boolean isHorizontal) {
        super(x, y, width, height, Component.empty());
        this.color = color;
        this.isHorizontal = isHorizontal;
    }

    public void showColorName(boolean enabled) {
        if (!enabled) {
            this.tooltipColorName = null;
        } else {
            this.tooltipColorName = Component.literal(MapColors.findByMapColor(color).name());
            if (tooltipColorName.getString().contains("BLACK")) {
                ((MutableComponent) tooltipColorName).withColor(MapColor.COLOR_GRAY.col);
            } else {
                ((MutableComponent) tooltipColorName).withColor(color.col);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
    }

    //? if <=1.21.8 {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseEvent, boolean doubleClick) {
        return true;
    }
    *///?}

    @Override
    //~ if >=26.1 'renderWidget' -> 'extractWidgetRenderState'
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        if (color == MapColor.NONE) {
            graphics.fill(x, y, x + width, y + height, MapColor.COLOR_LIGHT_GRAY.calculateARGBColor(MapColor.Brightness.NORMAL));
            //? if <=1.21.8 {
            renderScrollingString(
                    graphics,
                    Minecraft.getInstance().font,
                    Component.translatable("maparthelper.gui.background_color_clear"),
                    getX() + 2,
                    getY(),
                    getRight() - 2,
                    getBottom(),
                    CommonColors.SOFT_RED
            );
            //?} else {
            /*graphics.textRenderer().acceptScrollingWithDefaultCenter(
                    Component.translatable("maparthelper.gui.background_color_clear").withColor(CommonColors.SOFT_RED),
                    getX() + 2,
                    getRight() - 2,
                    getY(),
                    getBottom()
            );
            *///?}
        } else if (onlyNormalBrightness || color == MapColor.WATER) {
            MapColor.Brightness brightness = color == MapColor.WATER ? MapColor.Brightness.HIGH : MapColor.Brightness.NORMAL;
            int waterColor = ARGB.color(alpha, color.calculateARGBColor(brightness));
            graphics.fill(x, y, x + width, y + height, waterColor);
        } else {
            int low = ARGB.color(alpha, color.calculateARGBColor(MapColor.Brightness.LOW));
            int normal = ARGB.color(alpha, color.calculateARGBColor(MapColor.Brightness.NORMAL));
            int high = ARGB.color(alpha, color.calculateARGBColor(MapColor.Brightness.HIGH));
            if (!isHorizontal) {
                int segHeight = height / 3;
                graphics.fill(x, y, x + width, y + segHeight, low);
                graphics.fill(x, y + segHeight, x + width, y + segHeight * 2, normal);
                graphics.fill(x, y + segHeight * 2, x + width, y + height, high);
            } else {
                int segWidth = width / 3;
                graphics.fill(x, y, x + segWidth, y + height, low);
                graphics.fill(x + segWidth, y, x + segWidth * 2, y + height, normal);
                graphics.fill(x + segWidth * 2, y, x + width, y + height, high);
            }
        }
        RenderUtils.renderOutline(graphics, x, y, width, height, ARGB.color(alpha, 0xFF555555));

        if (tooltipColorName != null && graphics.containsPointInScissor(mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(tooltipColorName, mouseX, mouseY);
        }
    }
}
