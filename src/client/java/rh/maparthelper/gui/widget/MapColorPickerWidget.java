package rh.maparthelper.gui.widget;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.colors.MapColors;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.mapart.MapartProcessing;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class MapColorPickerWidget extends DropdownMenuWidget {
    private final MapartProcessing mapart;
    private final int columns;

    public MapColorPickerWidget(Screen parent, MapartProcessing mapart, int width, int height, int menuWidth, int menuHeight, int columns) {
        super(parent, 0, 0, width, height, menuWidth, menuHeight, columns, Component.empty());
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
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
        int color = MapartHelper.conversionSettings.getBackgroundRenderColor();
        context.fill(getX(), getY(), getRight(), getBottom(), color);
        context.renderOutline(getX(), getY(), getWidth(), getHeight(), 0xFF555555);
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
        protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
            onlyNormalBrightness = !MapartHelper.conversionSettings.use3D();
            if (color != MapColor.NONE && PaletteConfigManager.presetsConfig.getBlockOfMapColor(color) == null) {
                setAlpha(0.2f);
                if (this.isMouseOver(mouseX, mouseY) && context.containsPointInScissor(mouseX, mouseY)) {
                    context.setTooltipForNextFrame(Component.translatable("maparthelper.gui.color_without_block").withColor(CommonColors.SOFT_RED), mouseX, mouseY);
                }
            } else {
                setAlpha(1.0f);
            }
        }

        //~ widget_events
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (color == MapColor.NONE) {
                setColor(MapColorEntry.CLEAR);
                return true;
            }
            if (PaletteConfigManager.presetsConfig.getBlockOfMapColor(color) == null) {
                return false;
            }
            if (color == MapColor.WATER) {
                setColor(new MapColorEntry(color, MapColor.Brightness.HIGH));
                return true;
            }
            if (onlyNormalBrightness) {
                setColor(new MapColorEntry(color, MapColor.Brightness.NORMAL));
                return true;
            }
            int brightnessId = (int) Math.min((mouseX - this.getX()) / segWidth, 2);
            setColor(new MapColorEntry(color, MapColor.Brightness.byId(brightnessId)));
            return true;
        }
        //~ !widget_events
    }
}
