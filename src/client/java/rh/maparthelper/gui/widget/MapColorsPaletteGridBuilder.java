package rh.maparthelper.gui.widget;

import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.colors.MapColors;
import rh.maparthelper.config.palette.PaletteConfigManager;

import java.util.function.Consumer;

public class MapColorsPaletteGridBuilder {
    private final GridWidget grid = new GridWidget();
    private final Consumer<MapColorEntry> colorSetter;

    public MapColorsPaletteGridBuilder(Consumer<MapColorEntry> colorSetter) {
        this.colorSetter = colorSetter;
    }

    public GridWidget build(int width, int entryHeight, int entriesPerRow) {
        int entryWidth = (width - 10) / entriesPerRow;

        grid.getMainPositioner().margin(1);
        GridWidget.Adder adder = grid.createAdder(entriesPerRow);
        MapColors[] colors = MapColors.values();
        for (MapColors color : colors) {
            adder.add(new MapColorSelector(0, 0, entryWidth, entryHeight, color.color));
        }
        grid.refreshPositions();

        return grid;
    }

    private class MapColorSelector extends MapColorWidget {
        final int segWidth;

        public MapColorSelector(int x, int y, int width, int height, MapColor color) {
            super(x, y, width, height, color, true);
            this.segWidth = width / 3;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
            onlyNormalBrightness = !MapartHelper.conversionSettings.use3D();
            if (color != MapColor.CLEAR && PaletteConfigManager.presetsConfig.getBlockOfMapColor(color) == null) {
                setAlpha(0.2f);
                if (this.isMouseOver(mouseX, mouseY) && context.scissorContains(mouseX, mouseY)) {
                    context.drawTooltip(Text.translatable("maparthelper.gui.color_without_block").withColor(Colors.LIGHT_RED), mouseX, mouseY);
                }
            } else {
                setAlpha(1.0f);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            if (color == MapColor.CLEAR) {
                colorSetter.accept(MapColorEntry.CLEAR);
                return true;
            }
            if (PaletteConfigManager.presetsConfig.getBlockOfMapColor(color) == null) {
                return false;
            }
            if (onlyNormalBrightness || color == MapColor.WATER_BLUE) {
                colorSetter.accept(new MapColorEntry(color, MapColor.Brightness.NORMAL));
                return true;
            }
            int brightnessId = (int) Math.min((mouseX - this.getX()) / segWidth, 2);
            colorSetter.accept(new MapColorEntry(color, MapColor.Brightness.validateAndGet(brightnessId)));
            return true;
        }
    }
}
