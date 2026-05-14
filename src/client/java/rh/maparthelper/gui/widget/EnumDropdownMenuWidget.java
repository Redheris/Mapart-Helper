package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import rh.maparthelper.gui.screen.ScreenAdapted;

import java.util.List;
import java.util.function.Consumer;

public class EnumDropdownMenuWidget extends DropdownMenuWidget {
    private final ScreenAdapted screen;
    private final Component fieldName;
    private boolean showTooltips = true;
    private final boolean showFieldName;

    public EnumDropdownMenuWidget(ScreenAdapted screen, int x, int y, int width, int height, int menuWidth, int maxMenuHeight, Component fieldName, Component initOption, boolean showFieldName) {
        super(screen, x, y, width, height, menuWidth, maxMenuHeight, showFieldName ? fieldName.copy().append(initOption) : initOption);
        this.screen = screen;
        this.fieldName = fieldName;
        this.showFieldName = showFieldName;
    }

    public EnumDropdownMenuWidget(ScreenAdapted screen, int x, int y, int width, int height, int menuWidth, Component fieldName, Component initOption, boolean showFieldName) {
        this(screen, x, y, width, height, menuWidth, -1, fieldName, initOption, showFieldName);
    }

    public EnumDropdownMenuWidget(ScreenAdapted screen, int x, int y, int width, int height, int menuWidth, Component fieldName, Component initOption) {
        this(screen, x, y, width, height, menuWidth, fieldName, initOption, true);
    }

    public void toggleTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    public void addEntry(Consumer<Enum<?>> action, Enum<?> object) {
        if (object == null) return;
        Component objectName = Component.translatable("maparthelper.gui.option." + object.name());
        Button widget = Button.builder(
                        objectName,
                        btn -> {
                            this.setMessage(showFieldName ? fieldName.copy().append(objectName) : objectName);
                            action.accept(object);
                        }
                )
                .size(menuWidth - 4, 15)
                .build();
        if (showTooltips) {
            Component tooltip = MutableComponent.create(new TranslatableContents("maparthelper.gui.option." + object.name() + "._TOOLTIP", "", TranslatableContents.NO_ARGS));
            if (!tooltip.getString().isEmpty())
                widget = new EnumEntryWithTooltip(widget, List.of(tooltip));
        }
        super.addEntry(widget);
    }

    public void addEntries(Consumer<Enum<?>> action, Enum<?>... objects) {
        for (Enum<?> object : objects) {
            addEntry(action, object);
        }
    }

    private class EnumEntryWithTooltip extends Button {
        private final List<Component> tooltip;

        public EnumEntryWithTooltip(Button widget, List<Component> tooltip) {
            super(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.getMessage(),
                    b -> widget.onPress(), messageSupplier -> Component.empty());
            this.tooltip = tooltip;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            if (this.tooltip != null) {
                if (graphics.containsPointInScissor(mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
                    screen.setTooltipLines(tooltip, true);
                }
            }
        }
    }
}
