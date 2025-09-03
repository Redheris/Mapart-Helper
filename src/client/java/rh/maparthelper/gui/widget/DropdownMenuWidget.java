package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class DropdownMenuWidget extends ButtonWidget implements LayoutWidget {
    public static DropdownMenuWidget expandedOne;
    private final Screen parent;
    private final ScrollableGridWidget menu;
    private final GridWidget.Adder menuAdder;
    protected int menuWidth;
    private int menuXOffset = 0;
    private final int maxMenuHeight;

    private boolean expandUpwards = false;
    private int topYExpanded;

    private boolean needRelayout = false;

    public DropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, int maxMenuHeight, Text message) {
        super(x, y, width, height, message, btn -> {}, DEFAULT_NARRATION_SUPPLIER);
        expandedOne = null;
        this.parent = parent;
        this.topYExpanded = y;
        this.menuWidth = menuWidth;
        this.maxMenuHeight = maxMenuHeight == -1 ? 160 : maxMenuHeight;
        this.menu = new ScrollableGridWidget(
                null,
                x, y,
                menuWidth, this.maxMenuHeight, 4
        );
        menu.visible = false;
        menu.grid.getMainPositioner().margin(2, 2, 0, 2);
        menu.grid.setRowSpacing(-2);
        this.menuAdder = this.menu.grid.createAdder(1);
    }

    public void setLeftScroll(boolean leftScroll) {
        menu.setLeftScroll(leftScroll);
    }

    public void setMenuXOffset(int menuXOffset) {
        this.menuXOffset = menuXOffset;
    }

    public int getMenuX() {
        return this.getX() + this.menuXOffset;
    }

    public final void addEntry(ClickableWidget widget) {
        widget.visible = false;
        menuAdder.add(widget);
    }

    @Override
    public void refreshPositions() {
        if (getBottom() + menu.getHeight() > parent.height)
            expandUpwards = true;
        this.menu.refreshPositions();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.needRelayout = true;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.needRelayout = true;
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        super.forEachChild(consumer);
        consumer.accept(menu);
    }

    public void forEachEntry(Consumer<ClickableWidget> consumer) {
        menu.grid.forEachChild(consumer);
    }

    public void toggleExpanded(boolean expand) {
        if (expand && expandedOne != null) {
            expandedOne.menu.forEachChild(c -> c.visible = false);
            expandedOne.menu.visible = false;
        }
        menu.grid.forEachChild(c -> c.visible = expand);
        menu.visible = expand;
        expandedOne = expand ? this : null;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        toggleExpanded(expandedOne == null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expandedOne != null) {
            boolean bl = menu.checkScrollbarDragged(mouseX, mouseY, button);
            if (menu.isMouseOver(mouseX, mouseY)) {
                return menu.mouseClicked(mouseX, mouseY, button) || bl;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (expandedOne == null || !isMouseOverMenu(mouseX, mouseY)) return false;
        return this.menu.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean isMouseOverMenu(double mouseX, double mouseY) {
        return (expandedOne != null) && mouseX >= getMenuX() && mouseX < getMenuX() + menu.getWidth() && mouseY >= topYExpanded && mouseY < topYExpanded + menu.getHeight();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (needRelayout) {
            menu.setHeight(maxMenuHeight);
            if (getBottom() + menu.getHeight() > parent.height) {
                expandUpwards = true;
                menu.setHeight(Math.min(menu.getHeight(), getY() - 10));
                topYExpanded = getY() - menu.getHeight();
                menu.setY(topYExpanded);
            } else {
                topYExpanded = getBottom();
                expandUpwards = false;
                menu.setY(topYExpanded);
            }
            menu.setX(getMenuX());
            menu.refreshPositions();

            if (menu.getMaxScrollY() > 0) {
                if (menu.leftScroll) {
                    menu.setX(getMenuX() - 4);
                    menu.grid.getMainPositioner().marginLeft(6);
                }
                menu.setWidth(menuWidth + 1);
            } else {
                if (menu.leftScroll)
                    menu.setX(getMenuX());
                menu.setWidth(menuWidth);
            }

            this.needRelayout = false;
        }

        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    public void renderMenu(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (expandUpwards)
            context.fill(getMenuX(), getY() - menu.getHeight() - 2, getMenuX() + menu.getWidth(), getY(), 0x99FFFFFF);
        else
            context.fill(getMenuX(), getY() + height, getMenuX() + menu.getWidth(), getY() + height + menu.getHeight(), 0x99FFFFFF);

        this.menu.render(context, mouseX, mouseY, deltaTicks);
    }
}
