package rh.maparthelper.gui.widget;

import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.colors.MapColors;

import java.util.function.Consumer;

public class MapColorsPaletteWidget extends ClickableWidget {
    private final GridWidget grid = new GridWidget();
    private final Consumer<MapColorEntry> colorSetter;

    public MapColorsPaletteWidget(int x, int y, int width, int entryHeight, int entriesPerRow, Consumer<MapColorEntry> colorSetter) {
        super(x, y, width, 0, Text.empty());
        int entryWidth = (width - 10) / entriesPerRow;
        this.height = (int) Math.ceil((double) MapColors.values().length / entriesPerRow) * (entryHeight + 2);
        this.colorSetter = colorSetter;

        grid.getMainPositioner().margin(1);
        GridWidget.Adder adder = grid.createAdder(entriesPerRow);
        MapColors[] colors = MapColors.values();
        for (MapColors color : colors) {
            adder.add(new MapColorSelector(0, 0, entryWidth, entryHeight, color.color));
        }
        grid.refreshPositions();
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        grid.forEachChild(consumer);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
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
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            if (color == MapColor.CLEAR) {
                colorSetter.accept(MapColorEntry.CLEAR);
                return true;
            }
            if (color == MapColor.WATER_BLUE) {
                colorSetter.accept(new MapColorEntry(MapColor.WATER_BLUE, MapColor.Brightness.NORMAL, new int[3]));
                return true;
            }
            int brightnessId = (int) Math.min((mouseX - this.getX()) / segWidth, 2);
            colorSetter.accept(new MapColorEntry(color, MapColor.Brightness.validateAndGet(brightnessId), new int[3]));
            return true;
        }
    }
}
