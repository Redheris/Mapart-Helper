package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.widget.dropdown.DropdownOverlayWidget;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.painter.drawing.tool.ToolSettingsProvider;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class SelectionModesDropdown extends DropdownOverlayWidget {
    private final static Identifier replaceIconId = MapartHelper.identifier(
            "textures/gui/icons/painter/selection_rectangle.png"
    );
    private final static Identifier concatIconId = MapartHelper.identifier(
            "textures/gui/icons/painter/selection_concat.png"
    );
    private final static Identifier subtractIconId = MapartHelper.identifier(
            "textures/gui/icons/painter/selection_subtract.png"
    );
    private final static Identifier intersectionIconId = MapartHelper.identifier(
            "textures/gui/icons/painter/selection_intersection.png"
    );
    private final static Identifier xorIconId = MapartHelper.identifier(
            "textures/gui/icons/painter/selection_xor.png"
    );

    private final SelectionToolSettings selectionSettings = ToolSettingsProvider.getInstance().SELECTION_TOOL;

    // TODO: Localize
    private final Component label = Component.literal("Selection mode: ").withColor(CommonColors.LIGHT_GRAY);

    public SelectionModesDropdown(@NotNull Screen screen, SelectionToolSettings.SelectionMode selectionMode) {
        super(screen, null, 16, 16, false, false, null);
        Identifier icon = getModeIcon(selectionMode);
        this.setCustomSprites(new WidgetSprites(icon, icon));
        initOverlay();
        this.setTooltip(Tooltip.create(label.copy().append(Component.literal(selectionMode.toString()).withColor(-1))));
    }

    private void initOverlay() {
        LinearLayout list = LinearLayout.vertical().spacing(-2);
        list.defaultCellSetting().padding(2, 2, 2, 2);

        for (var selectionMode : SelectionToolSettings.SelectionMode.values()) {
            list.addChild(new SelectionModeListEntry(selectionMode));
        }

        var scrollable = new AdjScrollableLayoutWidget(list, 120);
        scrollable.setMarginX(1);
        scrollable.setBackgroundColor(0xAA_343434);
        scrollable.setOutlineColor(ARGB.color(0.3f, -1));
        scrollable.setScrollBarWidth(0);
        scrollable.arrangeElements();

        this.setOverlay(new OverlayLayout(scrollable));
    }

    public void updateDropdownButtonState() {
        SelectionToolSettings.SelectionMode mode = selectionSettings.getMode();
        Identifier icon = getModeIcon(mode);
        setCustomSprites(new WidgetSprites(icon, icon));
        Component modeName = Component.literal(mode.toString()).withColor(-1);
        SelectionModesDropdown.this.setTooltip(Tooltip.create(label.copy().append(modeName)));
    }

    private static Identifier getModeIcon(SelectionToolSettings.SelectionMode mode) {
        return switch (mode) {
            case REPLACE -> replaceIconId;
            case CONCAT -> concatIconId;
            case SUBTRACT -> subtractIconId;
            case AND -> intersectionIconId;
            case XOR -> xorIconId;
        };
    }

    private class SelectionModeListEntry extends AbstractWidget {
        private final Identifier icon;
        private final SelectionToolSettings.SelectionMode mode;
        private final Component modeName;

        public SelectionModeListEntry(SelectionToolSettings.SelectionMode mode) {
            super(0, 0, 120, 16, Component.empty());
            this.icon = getModeIcon(mode);
            this.mode = mode;
            this.modeName = Component.literal(mode.toString()).withColor(-1);
            this.setTooltip(Tooltip.create(label.copy().append(modeName)));
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            //? >=1.21.11
            //this.handleCursor(graphics);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    icon,
                    getX(), getY(),
                    0, 0,
                    16, 16,
                    16, 16
            );
            //~ if >=26.1 'drawString' -> 'text' >> '('
            graphics.drawString(Minecraft.getInstance().font, modeName, getX() + 18, getY() + 5, -1);
        }
        //~ !gui_rendering

        //~ widget_events
        @Override
        public void onClick(double mouseX, double mouseY) {
            if (selectionSettings.getMode() == mode) return;
            selectionSettings.setMode(mode);
            setCustomSprites(new WidgetSprites(icon, icon));
            SelectionModesDropdown.this.setTooltip(Tooltip.create(label.copy().append(modeName)));
        }
        //~ !widget_events

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
    }
}
