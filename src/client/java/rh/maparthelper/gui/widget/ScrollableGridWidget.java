package rh.maparthelper.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// To be honest, I would like to rewrite the drop-down menu widget and this one someday.
// The processing of their behavior looks... not very elegant.
// But rewriting will take some time, so I'll save this comment here for my future self
public class ScrollableGridWidget extends ScrollableWidget implements LayoutWidget {
    @Nullable
    private final Element parentWidget;
    public final InnerGridWidget grid;
    private final int scrollWidth;
    private int visibleTopY;
    private boolean needRelayout = false;
    protected boolean leftScroll = false;
    private int scrollBarColor = 0xFFC8C8C8;

    public ScrollableGridWidget(@Nullable Element parentWidget, int x, int y, int width, int height, int scrollWidth) {
        super(x, y, width, height, Text.empty());
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
    public void forEachElement(Consumer<Widget> consumer) {
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    @Override
    public int getHeight() {
        return Math.min(height, grid.getHeight());
    }

    @Override
    public void refreshPositions() {
        grid.refreshPositions();
        LayoutWidget.super.refreshPositions();
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

    public Optional<Widget> hoveredElement(double mouseX, double mouseY) {
        boolean onScroll = this.overflows()
                && mouseX >= this.getScrollbarX()
                && mouseX <= this.getScrollbarX() + scrollWidth
                && mouseY >= this.getY()
                && mouseY < this.getBottom();
        if (onScroll) return Optional.of(this);
        return grid.children.stream().filter(w ->
                (!(w instanceof ClickableWidget cw) || cw.active && cw.visible) &&
                mouseX >= w.getX() && mouseX < w.getX() + w.getWidth() && mouseY >= w.getY() && mouseY < w.getY() + w.getHeight()
        ).findFirst();
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return grid.getHeight();
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 15;
    }

    @Override
    public void setScrollY(double scrollY) {
        super.setScrollY(scrollY);
        grid.setY((int) (getY() - getScrollY()));
        grid.refreshPositions();
    }

    @Override
    protected int getScrollbarX() {
        return this.leftScroll ? this.getX() : this.getRight() - scrollWidth;
    }

    public boolean isOverScroll(double x, double y) {
        return this.overflows()
                && x >= this.getScrollbarX()
                && x <= this.getScrollbarX() + scrollWidth
                && y >= this.getY()
                && y < this.getBottom();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (parentWidget == null || parentWidget.isMouseOver(mouseX, mouseY)) {
            if (overflows()) {
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        }
        return parentWidget != null && parentWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (needRelayout) {
            grid.setX(this.getX());
            grid.setY((int) (this.getY() - this.getScrollY()));
            grid.refreshPositions();
            this.needRelayout = false;
        }
        if (parentWidget instanceof Widget w)
            context.enableScissor(getX(), Math.max(visibleTopY, w.getY()), getRight(), Math.min(visibleTopY + getHeight(), w.getY() + w.getHeight()));
        else
            context.enableScissor(getX(), visibleTopY, getRight(), visibleTopY + getHeight());
        grid.forEachChild(w -> w.render(context, mouseX, mouseY, deltaTicks));
        drawScrollbar(context);
        context.disableScissor();
    }

    @Override
    protected void drawScrollbar(DrawContext context) {
        if (this.overflows()) {
            int i = this.getScrollbarX();
            int j = this.getScrollbarThumbHeight();
            int k = this.getScrollbarThumbY();
            context.fill(i, this.getY(), i + scrollWidth, getY() + this.getHeight(), 0xFF555555);
            context.fill(i, k, i + scrollWidth, k + j, scrollBarColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < visibleTopY || mouseY > visibleTopY + getHeight() || !this.isMouseOver(mouseX, mouseY)) return false;
        if (this.checkScrollbarDragged(mouseX, mouseY, button)) return true;

        for (Widget w : grid.children) {
            if (!(w instanceof ClickableWidget child)) continue;
            if (!child.visible) continue;
            if (child.isMouseOver(mouseX, mouseY)) {
                Screen currentScreen = MinecraftClient.getInstance().currentScreen;
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public static class InnerGridWidget extends GridWidget {
        private final List<Widget> children = new ArrayList<>();

        public InnerGridWidget(int x, int y) {
            super(x, y);
        }

        public boolean isChild(Widget widget) {
            return children.contains(widget);
        }

        @Override
        public <T extends Widget> T add(T widget, int row, int column, int occupiedRows, int occupiedColumns, Positioner positioner) {
            if (widget instanceof LayoutWidget layoutWidget) {
                layoutWidget.forEachChild(children::add);
            } else {
                children.add(widget);
            }
            return super.add(widget, row, column, occupiedRows, occupiedColumns, positioner);
        }
    }
}
