package rh.maparthelper.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import rh.maparthelper.gui.widget.DropdownMenuWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extended Screed class with adjustments of interactions and rendering of {@link TextFieldWidget} and {@link DropdownMenuWidget}
 */
public abstract class ScreenAdapted extends Screen {
    private final List<Drawable> drawables = new ArrayList<>();
    DropdownMenuWidget selectedDropdownMenu;
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
            if (selectedDropdownMenu != null && !selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
                collapseDropdown();
            }
            return false;
        }

        if (selectedDropdownMenu != null && selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
            return selectedDropdownMenu.mouseClicked(mouseX, mouseY, button);
        }

        Element element = optional.get();
        if (element instanceof DropdownMenuWidget dropMenu) {
            this.setFocused(element);
            if (element != selectedDropdownMenu) {
                if (selectedDropdownMenu == null || !selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
                    collapseDropdown();
                    selectedDropdownMenu = dropMenu;
                } else if (selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY))
                    return false;
            }
            return dropMenu.mouseClicked(mouseX, mouseY, button);
        }

        if (!element.isFocused() && element instanceof TextFieldWidget textField) {
            selectedTextWidget = textField;
            textField.setSelectionStart(0);
            textField.setSelectionEnd(textField.getText().length());
            this.setFocused(textField);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        for (Drawable drawable : this.drawables) {
            if (selectedDropdownMenu != null && selectedDropdownMenu.isExpanded && selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
                if (drawable == selectedDropdownMenu)
                    drawable.render(context, mouseX, mouseY, delta);
                else
                    drawable.render(context, 0, 0, delta);
            } else
                drawable.render(context, mouseX, mouseY, delta);
        }
    }

    private void collapseDropdown() {
        if (this.selectedDropdownMenu != null) {
            this.setFocused(null);
            this.selectedDropdownMenu.switchExpanded(false);
            this.selectedDropdownMenu = null;
        }
    }
}
