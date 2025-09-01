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
        if (selectedDropdownMenu != null && selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
            return selectedDropdownMenu.mouseClicked(mouseX, mouseY, button);
        }

        Optional<Element> optional = this.hoveredElement(mouseX, mouseY);
        if (optional.isEmpty()) {
            this.setFocused(null);
            collapseDropdown();
            return false;
        }

        Element element = optional.get();
        if (element instanceof DropdownMenuWidget dropMenu) {
            this.setFocused(element);
            if (element != selectedDropdownMenu) {
                collapseDropdown();
                selectedDropdownMenu = dropMenu;
            }
            return dropMenu.mouseClicked(mouseX, mouseY, button);
        } else {
            collapseDropdown();
        }

        if (!element.isFocused() && element instanceof TextFieldWidget textField) {
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Drawable drawable : this.drawables) {
            if (drawable != selectedDropdownMenu) {
                if (selectedDropdownMenu != null && selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY))
                    drawable.render(context, 0, 0, delta);
                else
                    drawable.render(context, mouseX, mouseY, delta);
            }
        }

        if (selectedDropdownMenu != null) {
            selectedDropdownMenu.render(context, mouseX, mouseY, delta);
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
