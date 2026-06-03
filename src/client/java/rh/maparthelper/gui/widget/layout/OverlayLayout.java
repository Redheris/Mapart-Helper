package rh.maparthelper.gui.widget.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class OverlayLayout extends AbstractLayout {
    private final AdjScrollableLayoutWidget layout;
    private boolean visible;
    private AbstractWidget switchWidget;

    public OverlayLayout(AdjScrollableLayoutWidget layout) {
        super(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
        this.layout = layout;
        this.visible = false;
        layout.visitWidgets(w -> w.visible = this.visible);
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
        visitWidgets(w -> w.setAlpha(alpha));
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible)
            OverlaysManager.setVisibleOne(this);
        else
            OverlaysManager.close();

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
}
