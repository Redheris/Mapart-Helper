package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

// This implementation will soon be replaced by a more elegant one
public class DropdownMenuWidget extends Button/*? if >=1.21.11 {*//*.Plain *//*?}*/ implements Layout {
    public static DropdownMenuWidget expandedOne;

    private final Screen parent;
    private final ScrollableGridWidget menu;
    private final GridLayout.RowHelper menuAdder;
    protected int menuWidth;
    private int menuXOffset = 0;
    private final int maxMenuHeight;

    private boolean expandUpwards = false;
    private int topYExpanded;

    private boolean needRelayout = false;

    public DropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, int maxMenuHeight, int columns, Component message) {
        super(x, y, width, height, message, btn -> {}, DEFAULT_NARRATION);
        expandedOne = null;
        this.parent = parent;
        this.topYExpanded = y;
        this.menuWidth = menuWidth;
        this.maxMenuHeight = maxMenuHeight == -1 ? parent.height - y : maxMenuHeight;
        this.menu = new ScrollableGridWidget(
                null,
                x, y,
                menuWidth, this.maxMenuHeight, 4
        );
        menu.visible = false;
        menu.setScrollBarColor(0xFFFCFCFC);
        menu.grid.defaultCellSetting().padding(2, 2, 0, 2);
        menu.grid.rowSpacing(-2);
        this.menuAdder = this.menu.grid.createRowHelper(columns);
    }

    public DropdownMenuWidget(Screen parent, int x, int y, int width, int height, int menuWidth, int maxMenuHeight, Component message) {
        this(parent, x, y, width, height, menuWidth, maxMenuHeight, 1, message);
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

    public final void addEntry(AbstractWidget widget) {
        widget.visible = false;
        menuAdder.addChild(widget);
    }

    public final boolean isChild(LayoutElement widget) {
        return widget == menu || menu.grid.isChild(widget);
    }

    @Override
    public void arrangeElements() {
        if (getBottom() + menu.getHeight() > parent.height)
            expandUpwards = true;
        this.menu.arrangeElements();
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
    public void visitChildren(@NotNull Consumer<LayoutElement> consumer) {
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        super.visitWidgets(consumer);
    }

    public void forEachEntry(Consumer<AbstractWidget> consumer) {
        menu.grid.visitWidgets(consumer);
    }

    public void toggleExpanded(boolean expand) {
        if (expand && expandedOne != null) {
            expandedOne.menu.visitWidgets(c -> c.visible = false);
            expandedOne.menu.visible = false;
        }
        menu.grid.visitWidgets(c -> c.visible = expand);
        menu.visible = expand;
        expandedOne = expand ? this : null;
    }

    //~ widget_events
    @Override
    public void onClick(double mouseX, double mouseY) {
        toggleExpanded(expandedOne == null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expandedOne != null) {
            if (menu.isMouseOver(mouseX, mouseY)) {
                if (menu.isOverScroll(mouseX, mouseY)) {
                    parent.setFocused(menu);
                    if (button == 0)
                        parent.setDragging(true);
                }
                return menu.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (expandedOne == null || !isMouseOverMenu(mouseX, mouseY)) return false;
        return this.menu.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean isMouseOverMenu(double mouseX, double mouseY) {
        int left = menu.leftScroll ? getMenuX() - 4 : getMenuX();
        return expandedOne != null
                && mouseX >= left
                && mouseX < getMenuX() + menu.getWidth()
                && mouseY >= topYExpanded
                && mouseY < topYExpanded + menu.getHeight();
    }

    //~ if >=1.21.11 'renderWidget' -> 'renderContents' {
    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
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
            menu.arrangeElements();

            if (menu.maxScrollAmount() > 0) {
                if (menu.leftScroll) {
                    menu.setX(getMenuX() - 4);
                    menu.grid.defaultCellSetting().paddingLeft(6);
                }
                menu.setWidth(menuWidth + 1);
            } else {
                if (menu.leftScroll)
                    menu.setX(getMenuX());
                menu.setWidth(menuWidth);
            }

            this.needRelayout = false;
        }

        super.renderWidget(context, mouseX, mouseY, partialTick);
    }
    //~}

    public void renderMenu(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        int left = menu.leftScroll ? getMenuX() - 4 : getMenuX();
        if (expandUpwards) {
            context.fill(getMenuX(), getY() - menu.getHeight() - 2, getMenuX() + menu.getWidth(), getY(), 0x99FFFFFF);
            context.enableScissor(left, getY() - menu.getHeight() - 2, getMenuX() + menu.getWidth(), getY());
        } else {
            context.fill(getMenuX(), getY() + height, getMenuX() + menu.getWidth(), getY() + height + menu.getHeight(), 0x99FFFFFF);
            context.enableScissor(left, getY() + height, getMenuX() + menu.getWidth(), getY() + height + menu.getHeight());
        }
        this.menu.render(context, mouseX, mouseY, partialTick);
        context.disableScissor();
    }
}
