package rh.maparthelper.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// This implementation will soon be replaced by a more elegant one
public class ScrollableGridWidget extends AbstractScrollArea implements Layout {
    @Nullable
    private final GuiEventListener parentWidget;
    public final InnerGridWidget grid;
    private final int scrollWidth;
    private int visibleTopY;
    private boolean needRelayout = false;
    protected boolean leftScroll = false;
    private int scrollBarColor = 0xFFC8C8C8;

    public ScrollableGridWidget(@Nullable GuiEventListener parentWidget, int x, int y, int width, int height, int scrollWidth) {
        super(x, y, width, height, Component.empty());
        this.parentWidget = parentWidget;
        this.grid = new InnerGridWidget(x, y);
        this.scrollWidth = scrollWidth;
        this.visibleTopY = y;
    }

    public void setLeftScroll(boolean leftScroll) {
        this.leftScroll = leftScroll;
    }

    public void setScrollBarColor(int color) {
        this.scrollBarColor = color;
    }

    @Override
    public void visitChildren(@NotNull Consumer<LayoutElement> consumer) {
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
    }

    @Override
    public int getHeight() {
        return Math.min(height, grid.getHeight());
    }

    @Override
    public void arrangeElements() {
        grid.arrangeElements();
        Layout.super.arrangeElements();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.needRelayout = true;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.visibleTopY = y;
        this.needRelayout = true;
    }

    public Optional<LayoutElement> hoveredElement(double mouseX, double mouseY) {
        boolean onScroll = this.scrollbarVisible()
                && mouseX >= this.scrollBarX()
                && mouseX <= this.scrollBarX() + scrollWidth
                && mouseY >= this.getY()
                && mouseY < this.getBottom();
        if (onScroll) return Optional.of(this);
        return grid.children.stream().filter(w ->
                (!(w instanceof AbstractWidget cw) || cw.active && cw.visible) &&
                        mouseX >= w.getX() && mouseX < w.getX() + w.getWidth() && mouseY >= w.getY() && mouseY < w.getY() + w.getHeight()
        ).findFirst();
    }

    @Override
    protected int contentHeight() {
        return grid.getHeight();
    }

    @Override
    protected double scrollRate() {
        return 15;
    }

    @Override
    public void setScrollAmount(double scrollY) {
        super.setScrollAmount(scrollY);
        grid.setY((int) (getY() - scrollAmount()));
        grid.arrangeElements();
    }

    @Override
    protected int scrollBarX() {
        return this.leftScroll ? this.getX() : this.getRight() - scrollWidth;
    }

    public boolean isOverScroll(double x, double y) {
        return this.scrollbarVisible()
                && x >= this.scrollBarX()
                && x <= this.scrollBarX() + scrollWidth
                && y >= this.getY()
                && y < this.getBottom();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (parentWidget == null || parentWidget.isMouseOver(mouseX, mouseY)) {
            if (scrollbarVisible()) {
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        }
        return parentWidget != null && parentWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        if (needRelayout) {
            grid.setX(this.getX());
            grid.setY((int) (this.getY() - this.scrollAmount()));
            grid.arrangeElements();
            this.needRelayout = false;
        }
        if (parentWidget instanceof LayoutElement w)
            context.enableScissor(getX(), Math.max(visibleTopY, w.getY()), getRight(), Math.min(visibleTopY + getHeight(), w.getY() + w.getHeight()));
        else
            context.enableScissor(getX(), visibleTopY, getRight(), visibleTopY + getHeight());
        grid.visitWidgets(w -> w.render(context, mouseX, mouseY, partialTick));
        renderScrollbar(context);
        context.disableScissor();
    }

    @Override
    protected void renderScrollbar(@NotNull GuiGraphics context) {
        if (this.scrollbarVisible()) {
            int i = this.scrollBarX();
            int j = this.scrollerHeight();
            int k = this.scrollBarY();
            context.fill(i, this.getY(), i + scrollWidth, getY() + this.getHeight(), 0xFF555555);
            context.fill(i, k, i + scrollWidth, k + j, scrollBarColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < visibleTopY || mouseY > visibleTopY + getHeight() || !this.isMouseOver(mouseX, mouseY))
            return false;
        if (this.updateScrolling(mouseX, mouseY, button)) return true;

        for (LayoutElement w : grid.children) {
            if (!(w instanceof AbstractWidget child)) continue;
            if (!child.visible) continue;
            if (child.isMouseOver(mouseX, mouseY)) {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen != null) {
                    currentScreen.setFocused(child);
                    currentScreen.setDragging(true);
                }
                return child.mouseClicked(mouseX, mouseY, button);
            }
        }
        return true;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
    }

    public static class InnerGridWidget extends GridLayout {
        private final List<LayoutElement> children = new ArrayList<>();

        public InnerGridWidget(int x, int y) {
            super(x, y);
        }

        public boolean isChild(LayoutElement widget) {
            return children.contains(widget);
        }

        @Override
        public <T extends LayoutElement> @NotNull T addChild(T widget, int row, int column, int occupiedRows, int occupiedColumns, @NotNull LayoutSettings positioner) {
            if (widget instanceof Layout layoutWidget) {
                layoutWidget.visitWidgets(children::add);
            } else {
                children.add(widget);
            }
            return super.addChild(widget, row, column, occupiedRows, occupiedColumns, positioner);
        }
    }
}
