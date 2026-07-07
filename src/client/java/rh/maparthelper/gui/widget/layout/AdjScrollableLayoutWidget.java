package rh.maparthelper.gui.widget.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.mixin.AbstractWidgetAccessor;
import rh.maparthelper.util.RenderUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

//? if >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractScrollArea;
*///?}

@Environment(EnvType.CLIENT)
public class AdjScrollableLayoutWidget implements Layout {
    private final Layout layout;
    protected final Container container;
    private int width;
    private int height;

    public AdjScrollableLayoutWidget(Layout layout, int height) {
        this.layout = layout;
        this.height = height;
        this.container = new Container(0, height);
    }

    public void setWidth(int width) {
        this.width = width;
        this.container.setWidth(Math.max(this.layout.getWidth(), width));
    }

    public void setScrollY(double scrollY) {
        this.container.setScrollAmount(scrollY);
    }

    public double getScrollY() {
        return this.container.scrollAmount();
    }

    public void setHeight(int height) {
        this.height = height;
        this.container.setHeight(Math.min(this.layout.getHeight(), height));
        this.container.refreshChildrenX();
        this.container.refreshScrollAmount();
    }

    @Override
    public void arrangeElements() {
        this.layout.arrangeElements();
        int i = this.layout.getWidth();
        this.container.setWidth(Math.max(i + container.marginLeft + container.marginRight, this.width));
        this.container.setHeight(Math.min(this.layout.getHeight(), this.height));
        this.container.refreshScrollAmount();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return container.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(this.container);
    }

    @Override
    public void setX(int x) {
        this.container.setX(x);
    }

    @Override
    public void setY(int y) {
        this.container.setY(y);
    }

    @Override
    public int getX() {
        return this.container.getX();
    }

    @Override
    public int getY() {
        return this.container.getY();
    }

    @Override
    public int getWidth() {
        return this.container.getWidth();
    }

    @Override
    public int getHeight() {
        return this.container.getHeight();
    }

    public void setMarginX(int marginX) {
        setMarginLeft(marginX);
        setMarginRight(marginX);
    }

    public void setMarginLeft(int marginLeft) {
        container.marginLeft = marginLeft;
    }

    public void setMarginRight(int marginRight) {
        container.marginRight = marginRight;
    }

    public void setDeltaYPerScroll(double deltaYPerScroll) {
        container.deltaYPerScroll = deltaYPerScroll;
    }

    public void setBackgroundColor(int color) {
        container.bgColor = color;
    }

    public void setOutlineColor(int color) {
        container.outlineColor = color;
    }

    public void setScrollBarWidth(int width) {
        container.scrollBarWidth = width;
    }

    public void setLeftScrollBar(boolean leftScrollBar) {
        container.leftScrollBar = leftScrollBar;
    }

    @Environment(EnvType.CLIENT)
    protected class Container extends AbstractContainerWidget {
        private final Minecraft client;
        private final List<AbstractWidget> children = new ArrayList<>();

        private double deltaYPerScroll = 10.0;
        private int marginLeft = 0;
        private int marginRight = 0;
        private int bgColor = 0;
        private int outlineColor = 0;
        private int scrollBarWidth = 6;
        private boolean leftScrollBar = false;

        private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("widget/scroller_background");

        public Container(int width, int height) {
            //? if >=26.1 {
            /*super(0, 0, width, height, CommonComponents.EMPTY, AbstractScrollArea.defaultSettings(15));
            *///?} else
            super(0, 0, width, height, CommonComponents.EMPTY);

            this.client = Minecraft.getInstance();
            AdjScrollableLayoutWidget.this.layout.visitWidgets(this.children::add);
        }

        @Override
        protected int contentHeight() {
            return AdjScrollableLayoutWidget.this.layout.getHeight();
        }

        @Override
        protected double scrollRate() {
            return this.deltaYPerScroll;
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
            graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
            if (bgColor != 0) {
                graphics.fill(
                        this.getX(), this.getY(),
                        this.getX() + this.width,
                        this.getY() + this.height,
                        ARGB.color(ARGB.alphaFloat(bgColor) * alpha, this.bgColor)
                );
            }

            for (AbstractWidget childWidget : this.children) {
                float childAlpha = ((AbstractWidgetAccessor)childWidget).getAlpha();
                childWidget.setAlpha(alpha * childAlpha);
                childWidget.render(graphics, mouseX, mouseY, deltaTicks);
                childWidget.setAlpha(childAlpha);
            }

            graphics.disableScissor();
            if (outlineColor != 0) {
                RenderUtils.renderOutline(
                        graphics,
                        getX() - 1, getY() - 1,
                        this.width + 2, this.height + 2,
                        ARGB.color(0.47f * alpha, outlineColor)
                );
            }
            this.renderScrollbar(graphics/*? if >=1.21.10 {*//*, mouseX, mouseY *//*?}*/);
        }

        @Override
        protected void renderScrollbar(@NotNull GuiGraphics graphics/*? if >=1.21.10 {*//*, int mouseX, int mouseY *//*?}*/) {
            if (this.scrollbarVisible()) {
                int scrollBarX = this.scrollBarX();
                int scrollerHeight = this.scrollerHeight();
                int scrollBarY = this.scrollBarY();
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE,
                        scrollBarX, this.getY(), scrollBarWidth, this.getHeight(),
                        0.6f * alpha
                );
                graphics.fill(
                        scrollBarX, scrollBarY,
                        scrollBarX + scrollBarWidth, scrollBarY + scrollerHeight,
                        ARGB.color(alpha, 0xE2E2E2)
                );
                graphics.vLine(
                        scrollBarX,
                        scrollBarY - 1, scrollBarY + scrollerHeight,
                        ARGB.color(0.2f * alpha, 0)
                );
                graphics.vLine(
                        scrollBarX + scrollBarWidth - 1,
                        scrollBarY - 1, scrollBarY + scrollerHeight,
                        ARGB.color(0.2f * alpha, 0)
                );
            }
        }
        //~ !gui_rendering

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
        }

        @Override
        public @NotNull ScreenRectangle getBorderForArrowNavigation(@NotNull ScreenDirection direction) {
            return new ScreenRectangle(this.getX(), this.getY(), this.width, this.contentHeight());
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            super.setFocused(focused);
            if (focused != null && this.client.getLastInputType().isKeyboard()) {
                ScreenRectangle screenRect = this.getRectangle();
                ScreenRectangle screenRect2 = focused.getRectangle();
                int topDelta = screenRect2.top() - screenRect.top();
                int bottomDelta = screenRect2.bottom() - screenRect.bottom();
                if (topDelta < 0) {
                    this.setScrollAmount(this.scrollAmount() + topDelta - 14.0);
                } else if (bottomDelta > 0) {
                    this.setScrollAmount(this.scrollAmount() + bottomDelta + 14.0);
                }
            }
        }

        private void refreshChildrenX() {
            int scrollOffset;
            if (leftScrollBar) {
                scrollOffset = 0;
            } else {
                scrollOffset = scrollbarVisible() ? 0 : scrollBarWidth / 2;
            }
            AdjScrollableLayoutWidget.this.layout.setX(getX() + marginLeft + scrollOffset);
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            refreshChildrenX();
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            AdjScrollableLayoutWidget.this.layout.setY(y - (int) this.scrollAmount());
        }

        @Override
        public void setScrollAmount(double scrollY) {
            super.setScrollAmount(scrollY);
            AdjScrollableLayoutWidget.this.layout.setY(this.getRectangle().top() - (int) this.scrollAmount());
        }

        @Override
        protected int scrollBarX() {
            return leftScrollBar ? this.getX() : this.getRight() - scrollBarWidth;
        }

        //~ widget_events
        @Override
        public boolean updateScrolling(double mouseX, double mouseY, int button) {
            this.scrolling = this.scrollbarVisible()
                    && this.isValidClickButton(button)
                    && mouseX >= (double) this.scrollBarX()
                    && mouseX <= (double) (this.scrollBarX() + scrollBarWidth)
                    && mouseY >= (double) this.getY()
                    && mouseY < (double) this.getBottom();
            return this.scrolling;
        }
        //~ !widget_events


        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (isMouseOver(mouseX, mouseY)) {
                for (AbstractWidget child : container.children) {
                    if (child instanceof AbstractScrollArea scrollArea) {
                        if (child.isMouseOver(mouseX, mouseY) && scrollArea.maxScrollAmount() > 0)
                            return child.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
                    }
                }
            }
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        protected boolean scrollbarVisible() {
            //? if <26.1 {
            return super.scrollbarVisible();
            //?} else
            //return this.scrollable();
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public @NotNull Collection<? extends NarratableEntry> getNarratables() {
            return this.children;
        }
    }
}
