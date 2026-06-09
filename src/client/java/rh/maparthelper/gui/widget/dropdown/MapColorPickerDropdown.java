package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.colors.MapColors;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.MapColorWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.mapart.MapartProcessing;
import rh.maparthelper.palette.PaletteDataManager;
import rh.maparthelper.palette.RegisteredPalettePreset;
import rh.maparthelper.util.RenderUtils;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class MapColorPickerDropdown extends DropdownOverlayWidget {
    private final PaletteDataManager paletteDataManager = PaletteDataManager.getInstance();
    private final MapartProcessing mapart;

    public MapColorPickerDropdown(@NotNull Screen screen, MapartProcessing mapart, int width, int height, int overlayWidth, int overlayHeight) {
        super(screen, null, width, height, Component.empty());

        this.mapart = mapart;
        initOverlay(overlayWidth, overlayHeight);
        setOverlayXOffset(width - overlayWidth);
    }

    private void initOverlay(int overlayWidth, int overlayHeight) {
        GridLayout grid = new GridLayout().rowSpacing(-2);
        grid.defaultCellSetting().padding(1, 2, 2, 2);

        GridLayout.RowHelper adder = grid.createRowHelper(4);
        int entryWidth = overlayWidth / 4 - 2;

        for (MapColors color : MapColors.values()) {
            adder.addChild(new MapColorSelector(0, 0, entryWidth, 20, color.color));
        }

        this.setOverlay(OverlayLayoutFactory.defaultOverlay(grid, overlayHeight, overlayWidth + 8));
    }

    @Override
    //? if <=1.21.8 {
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    //?} elif 1.21.11 {
    /*protected void renderContents(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(graphics, mouseX, mouseY, partialTick);
    *///?} elif >=26.1 {
    /*protected void extractContents(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    *///?}
        int color = MapartHelper.conversionConfig().getBackgroundRenderColor();
        graphics.fill(getX(), getY(), getRight(), getBottom(), color);
        RenderUtils.renderOutline(graphics, getX(), getY(), getWidth(), getHeight(), 0xFF555555);
    }

    private void setColor(MapColorEntry color) {
        MapColorEntry current = MapartHelper.conversionConfig().getBackgroundColor();
        if (current.mapColor() != color.mapColor() || current.brightness() != color.brightness()) {
            MapartHelper.conversionConfig().setBackgroundColor(color);
            MapartImageUpdater.updateMapart(mapart);
        }
    }

    private class MapColorSelector extends MapColorWidget {
        final int segWidth;

        public MapColorSelector(int x, int y, int width, int height, MapColor color) {
            super(x, y, width, height, color, true);
            this.segWidth = width / 3;
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            onlyNormalBrightness = !MapartHelper.conversionConfig().use3D();
            RegisteredPalettePreset preset = paletteDataManager.getPresetsHandler().getSelectedPreset();
            if (color != MapColor.NONE && preset.getBlockOfMapColor(color) == null) {
                setAlpha(0.2f);
                if (this.isMouseOver(mouseX, mouseY) && graphics.containsPointInScissor(mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(Component.translatable("maparthelper.gui.color_without_block").withColor(CommonColors.SOFT_RED), mouseX, mouseY);
                }
            } else {
                setAlpha(1.0f);
            }
        }
        //~ !gui_rendering

        //~ widget_events
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (color == MapColor.NONE) {
                setColor(MapColorEntry.CLEAR);
                return true;
            }
            if (paletteDataManager.getPresetsHandler().getSelectedPreset().getBlockOfMapColor(color) == null) {
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
