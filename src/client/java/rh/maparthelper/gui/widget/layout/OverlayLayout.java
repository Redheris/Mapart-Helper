package rh.maparthelper.gui.widget.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class OverlayLayout extends AbstractLayout {
    private final AdjScrollableLayoutWidget layout;
    protected boolean visible;
    private boolean autoCloseable;
    private AbstractWidget switchWidget;

    public OverlayLayout(AdjScrollableLayoutWidget layout, boolean autoCloseable) {
        super(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
        this.layout = layout;
        this.visible = false;
        this.autoCloseable = autoCloseable;
        layout.visitWidgets(w -> w.visible = this.visible);
    }

    public OverlayLayout(AdjScrollableLayoutWidget layout) {
        this(layout, true);
    }

    @Override
    public void visitChildren(@NotNull Consumer<LayoutElement> consumer) {
        layout.visitChildren(consumer);
    }

    public boolean isLayoutContent(Renderable widget) {
        return layout.container == widget;
    }

    @Override
    public void arrangeElements() {
        layout.arrangeElements();
    }

    public void setHeight(int height) {
        this.height = height;
        layout.setHeight(height);
    }

    public void setAlpha(float alpha) {
        // FIXME: It makes all translucent widgets opaque
        visitWidgets(w -> w.setAlpha(alpha));
    }

    public boolean isAutoCloseable() {
        return autoCloseable;
    }

    public void setAutoCloseable(boolean lockVisible) {
        this.autoCloseable = lockVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible)
            OverlaysManager.setActiveOverlay(this);
        else
            OverlaysManager.close(this);

        layout.visitWidgets(w -> w.visible = visible);
    }

    public boolean isMouseOverLayout(double mouseX, double mouseY) {
        return visible &&
                mouseX >= getX() && mouseY >= getY() &&
                mouseX < (getX() + getWidth()) && mouseY < (getY() + getHeight());
    }

    public boolean isMouseOverSwitch(double mouseX, double mouseY) {
        if (switchWidget == null) return false;
        return switchWidget.visible && switchWidget.isMouseOver(mouseX, mouseY);
    }

    public void setSwitchWidget(AbstractWidget switchWidget) {
        this.switchWidget = switchWidget;
    }

    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            //~ if >=26.1 '.render(' -> '.extractRenderState('
            visitWidgets(w -> w.render(graphics, mouseX, mouseY, partialTick));
        }
    }
}
