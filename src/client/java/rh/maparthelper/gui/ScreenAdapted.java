package rh.maparthelper.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import rh.maparthelper.gui.widget.DropdownMenuWidget;
import rh.maparthelper.gui.widget.ScrollableGridWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extended Screed class with adjustments of behavior and rendering of {@link TextFieldWidget} and {@link DropdownMenuWidget}
 */
public abstract class ScreenAdapted extends Screen {
    private final List<Drawable> drawables = new ArrayList<>();
    TextFieldWidget selectedTextWidget;

    protected ScreenAdapted(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        collapseDropdown();
    }

    @Override
    protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        this.drawables.add(drawableElement);
        return super.addDrawableChild(drawableElement);
    }

    @Override
    protected <T extends Drawable> T addDrawable(T drawable) {
        this.drawables.add(drawable);
        return super.addDrawable(drawable);
    }

    @Override
    protected void remove(Element child) {
        if (child instanceof Drawable)
            this.drawables.remove((Drawable) child);
        super.remove(child);
    }

    @Override
    protected void clearChildren() {
        super.clearChildren();
        this.drawables.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedTextWidget != null) {
            selectedTextWidget.setSelectionStart(0);
            selectedTextWidget.setSelectionEnd(0);
            selectedTextWidget = null;
        }
        Optional<Element> optional = this.hoveredElement(mouseX, mouseY);
        if (optional.isEmpty()) {
            this.setFocused(null);
            collapseDropdown();
            return false;
        }

        DropdownMenuWidget dropdownMenu = DropdownMenuWidget.expandedOne;
        if (dropdownMenu != null) {
            if (dropdownMenu.isMouseOverMenu(mouseX, mouseY) || dropdownMenu.isMouseOver(mouseX, mouseY)) {
                return dropdownMenu.mouseClicked(mouseX, mouseY, button);
            }
        }
        collapseDropdown();

        Element element = optional.get();

        if (element instanceof ScrollableGridWidget layout) {
            List<ClickableWidget> elements = new ArrayList<>();
            layout.grid.forEachChild(elements::add);

            for (ClickableWidget w : elements) {
                if (w != selectedTextWidget && w.isMouseOver(mouseX, mouseY) && w instanceof TextFieldWidget) {
                    element = w;
                    break;
                }
            }
        }

        if (element instanceof TextFieldWidget textField) {
            if (element.isFocused()) {
                return element.mouseClicked(mouseX, mouseY, button);
            }

            selectedTextWidget = textField;
            this.setFocused(textField);
            if (button == 0) {
                textField.setSelectionStart(0);
                textField.setSelectionEnd(textField.getText().length());
            } else if (button == 1) {
                textField.setText("");
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        DropdownMenuWidget dropdownMenu = DropdownMenuWidget.expandedOne;
        for (Drawable drawable : drawables) {
            if (dropdownMenu != null && dropdownMenu.isMouseOverMenu(mouseX, mouseY))
                drawable.render(context, 0, 0, delta);
            else
                drawable.render(context, mouseX, mouseY, delta);
        }

        if (dropdownMenu != null) {
            dropdownMenu.renderMenu(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void close() {
        assert this.client != null;
        super.close();
        DropdownMenuWidget.expandedOne = null;
    }

    private void collapseDropdown() {
        DropdownMenuWidget dropdownMenu = DropdownMenuWidget.expandedOne;
        if (dropdownMenu != null) {
            this.setFocused(null);
            dropdownMenu.toggleExpanded(false);
        }
    }
}
