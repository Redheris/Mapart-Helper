package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// I wish I could have implemented this better...
public class DropdownMenuWidget extends ButtonWidget {
    public static DropdownMenuWidget expandedOne;
    private final Screen parent;
    private final List<ClickableWidget> elements = new ArrayList<>();
    private int menuHeight = 2;
    protected final int menuWidth;
    private boolean expandUpwards = false;
    private int topYExpanded;
    private int bottomYExpanded;

    public boolean isExpanded = false;

    public DropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, Text message) {
        super(x, y, width, height, message,
                btn -> ((DropdownMenuWidget) btn).switchExpanded(!((DropdownMenuWidget) btn).isExpanded),
                DEFAULT_NARRATION_SUPPLIER
        );
        this.parent = parent;
        this.topYExpanded = y;
        this.bottomYExpanded = y + height;
        this.menuWidth = menuWidth;
        expandedOne = null;
    }

    public final void addEntry(ClickableWidget widget) {
        widget.visible = false;
        elements.add(widget);
        menuHeight += widget.getHeight() + 2;
    }

    public void refreshPositions() {
        if (getBottom() + menuHeight > parent.height)
            expandUpwards = true;
        int x = getX() + 2;
        int y = (expandUpwards ? getY() - menuHeight : getBottom()) + 2;
        for (ClickableWidget element : elements) {
            element.setPosition(x, y);
            y += element.getHeight() + 2;
        }
        topYExpanded = expandUpwards ? getY() - menuHeight : getBottom();
        bottomYExpanded = expandUpwards ? getY() : getBottom() + menuHeight;
    }

    public void forEachEntry(Consumer<ClickableWidget> consumer) {
        elements.forEach(consumer);
    }

    public boolean isChild(ClickableWidget element) {
        return elements.contains(element);
    }

    public void switchExpanded(boolean value) {
        if (value && expandedOne != null) {
            expandedOne.isExpanded = false;
            expandedOne.elements.forEach(c -> c.visible = false);
        }
        isExpanded = value;
        elements.forEach(c -> c.visible = value);
        if (value)
            expandedOne = this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isExpanded) {
            for (ClickableWidget w : elements) {
                if (w.isMouseOver(mouseX, mouseY)) {
                    return w.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
        boolean head = expandUpwards ? mouseY >= getY() : mouseY < getBottom();
        return head && super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isMouseOverMenu(double mouseX, double mouseY) {
        return isExpanded && mouseX >= getX() && mouseX < getX() + menuWidth && mouseY >= topYExpanded && mouseY < bottomYExpanded;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
        if (!isExpanded) return;
        context.enableScissor(getX(), topYExpanded, getX() + menuWidth, bottomYExpanded);
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(0, 0, 300);
         if (expandUpwards)
            context.fill(getX(), getY() - menuHeight, getX() + menuWidth, getY(), 0x99FFFFFF);
         else
            context.fill(getX(), getY() + height, getX() + menuWidth, getY() + height + menuHeight, 0x99FFFFFF);
        elements.forEach(
                e -> e.render(context, mouseX, mouseY, deltaTicks));
        matrixStack.pop();
        context.disableScissor();
    }
}
