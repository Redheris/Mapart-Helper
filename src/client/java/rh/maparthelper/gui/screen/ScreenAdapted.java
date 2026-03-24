package rh.maparthelper.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.DropdownMenuWidget;
import rh.maparthelper.gui.widget.ScrollableGridWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

/**
 * Extended Screen class with adjustments of behavior and rendering of {@link DropdownMenuWidget}
 */
public abstract class ScreenAdapted extends Screen {
    private final List<Renderable> drawables = new ArrayList<>();

    protected ScreenAdapted(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        collapseDropdown();
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(T drawableElement) {
        this.drawables.add(drawableElement);
        return super.addRenderableWidget(drawableElement);
    }

    @Override
    protected <T extends Renderable> @NotNull T addRenderableOnly(T drawable) {
        this.drawables.add(drawable);
        return super.addRenderableOnly(drawable);
    }

    @Override
    protected void removeWidget(@NotNull GuiEventListener child) {
        if (child instanceof Renderable)
            this.drawables.remove((Renderable) child);
        super.removeWidget(child);
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.drawables.clear();
    }

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DropdownMenuWidget dropdownMenu = DropdownMenuWidget.expandedOne;
        if (dropdownMenu != null) {
            if (dropdownMenu.isMouseOverMenu(mouseX, mouseY) || dropdownMenu.isMouseOver(mouseX, mouseY)) {
                return dropdownMenu.mouseClicked(mouseX, mouseY, button);
            }
        }
        collapseDropdown();

        Optional<GuiEventListener> optional = this.getChildAt(mouseX, mouseY);
        if (optional.isEmpty()) {
            this.setFocused(null);
            collapseDropdown();
            return false;
        }
        GuiEventListener element = optional.get();

        if (element instanceof ScrollableGridWidget layout) {
            Optional<LayoutElement> optional2 = layout.hoveredElement(mouseX, mouseY);
            if (optional2.isEmpty()) return false;
            if (optional2.get() == layout) return super.mouseClicked(mouseX, mouseY, button);
            return element.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (DropdownMenuWidget.expandedOne != null && DropdownMenuWidget.expandedOne.isMouseOverMenu(mouseX, mouseY)) {
            if (DropdownMenuWidget.expandedOne.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        DropdownMenuWidget dropdownMenu = DropdownMenuWidget.expandedOne;
        for (Renderable drawable : drawables) {
            if (dropdownMenu != null && dropdownMenu.isMouseOverMenu(mouseX, mouseY) && !dropdownMenu.isChild((LayoutElement) drawable))
                drawable.render(context, 0, 0, delta);
            else
                drawable.render(context, mouseX, mouseY, delta);
        }

        if (dropdownMenu != null) {
            dropdownMenu.renderMenu(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void onClose() {
        //? <=1.21.8
        assert this.minecraft != null;
        super.onClose();
        DropdownMenuWidget.expandedOne = null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void collapseDropdown() {
        DropdownMenuWidget dropdownMenu = DropdownMenuWidget.expandedOne;
        if (dropdownMenu != null) {
            this.setFocused(null);
            dropdownMenu.toggleExpanded(false);
        }
    }
}
