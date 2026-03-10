package rh.maparthelper.gui.widget;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.colors.MapColors;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.mapart.MapartProcessing;

public class MapColorPickerWidget extends DropdownMenuWidget {
    private final MapartProcessing mapart;
    private final int columns;

    public MapColorPickerWidget(Screen parent, MapartProcessing mapart, int width, int height, int menuWidth, int menuHeight, int columns) {
        super(parent, 0, 0, width, height, menuWidth, menuHeight, columns, Text.empty());
        this.mapart = mapart;
        this.columns = columns;
        initDropdown();
    }

    private void initDropdown() {
        int entryWidth = (menuWidth - 10) / columns;
        MapColors[] colors = MapColors.values();
        for (MapColors color : colors) {
            addEntry(new MapColorSelector(0, 0, entryWidth, 20, color.color));
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
        int color = MapartHelper.conversionSettings.getBackgroundRenderColor();
        context.fill(getX(), getY(), getRight(), getBottom(), color);
        context.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFF555555);
    }

    private void setColor(MapColorEntry color) {
        MapColorEntry current = MapartHelper.conversionSettings.getBackgroundColor();
        if (current.mapColor() != color.mapColor() || current.brightness() != color.brightness()) {
            MapartHelper.conversionSettings.setBackgroundColor(color);
            AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
            MapartImageUpdater.updateMapart(mapart);
        }
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
                setColor(MapColorEntry.CLEAR);
                return true;
            }
            if (PaletteConfigManager.presetsConfig.getBlockOfMapColor(color) == null) {
                return false;
            }
            if (onlyNormalBrightness || color == MapColor.WATER_BLUE) {
                setColor(new MapColorEntry(color, MapColor.Brightness.NORMAL));
                return true;
            }
            int brightnessId = (int) Math.min((mouseX - this.getX()) / segWidth, 2);
            setColor(new MapColorEntry(color, MapColor.Brightness.validateAndGet(brightnessId)));
            return true;
        }
    }
}
