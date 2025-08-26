package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrollableGridWidget extends ScrollableWidget implements LayoutWidget {
    public final GridWidget grid;
    private final int scrollWidth;
    private final int minY;
    private final int maxY;

    public ScrollableGridWidget(int x, int y, int width, int height, int minY, int maxY, int scrollWidth) {
        super(x, y, width, height, Text.empty());
        this.grid = new GridWidget(x, y);
        this.minY = minY;
        this.maxY = maxY;
        this.scrollWidth = scrollWidth;
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
        grid.setX(this.getX());
        grid.setY((int) (this.getY() - this.getScrollY()));
        grid.refreshPositions();
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return grid.getHeight();
    }

    @Override
    protected double getDeltaYPerScroll() {
        return (double) getContentsHeightWithPadding() / height * 4.0;
    }

    @Override
    public void setScrollY(double scrollY) {
        super.setScrollY(scrollY);
        grid.setY((int) (getY() - getScrollY()));
        grid.refreshPositions();
    }

    @Override
    protected int getScrollbarX() {
        return this.getRight() - scrollWidth;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.enableScissor(getX(), Math.max(getY(), minY), getRight(), Math.min(getBottom(), maxY));
        grid.forEachChild(w -> w.render(context, mouseX, mouseY, deltaTicks));
        drawScrollbar(context);
        context.disableScissor();
    }

    @Override
    protected void drawScrollbar(DrawContext context) {
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(0, 0, 200);
        if (this.overflows()) {
            int i = this.getScrollbarX();
            int j = this.getScrollbarThumbHeight();
            int k = this.getScrollbarThumbY();
            context.fill(i, this.getY(), i + scrollWidth, getY() + this.getHeight(), 0xFF555555);
            context.fill(i, k, i + scrollWidth, k + j, 0xFFA8A8A8);
        }
        matrixStack.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < minY || mouseY > maxY || !this.isMouseOver(mouseX, mouseY)) return false;
        if (this.checkScrollbarDragged(mouseX, mouseY, button)) return true;

        List<ClickableWidget> children = collectChildrenList();
        for (ClickableWidget child : children) {
            if (!child.visible) continue;
            if (child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    private List<ClickableWidget> collectChildrenList() {
        ArrayList<ClickableWidget> list = new ArrayList<>();
        this.grid.forEachChild(list::add);
        return list;
    }
}
