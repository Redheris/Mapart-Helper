package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.util.RenderUtils;

import java.util.function.Supplier;

public class MapColorPickerWidget extends DropdownMenuWidget {
    private final Supplier<MapColorEntry> colorGetter;
    public MapColorPickerWidget(Screen parent, int x, int y, int width, int height, int menuWidth, int menuHeight, int columns, Supplier<MapColorEntry> colorGetter) {
        super(parent, x, y, width, height, menuWidth, menuHeight, columns, Text.empty());
        this.colorGetter = colorGetter;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
        MapColorEntry colorEntry = colorGetter.get();
        int color = colorEntry.mapColor().getRenderColor(colorEntry.brightness());
        context.fill(getX(), getY(), getRight(), getBottom(), color);
        RenderUtils.drawBorder(context, getX(), getY(), getWidth(), getHeight(), 0xFF555555);
    }
}
