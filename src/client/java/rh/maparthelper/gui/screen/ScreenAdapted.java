package rh.maparthelper.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.gui.widget.layout.OverlaysManager;

import java.util.HashSet;
import java.util.Set;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public abstract class ScreenAdapted extends Screen {
    protected ScreenAdapted(Component title) {
        super(title);
    }

    protected void preInit() {
    }

    @Override
    protected final void init() {
        preInit();

        OverlaysManager.close();
        Set<OverlayLayout> overlays = initOverlays();
        for (OverlayLayout overlay : overlays) {
            overlay.arrangeElements();
            overlay.visitWidgets(this::addRenderableWidget);
        }

        initContent();
    }

    protected abstract void initContent();

    protected Set<OverlayLayout> initOverlays() {
        return new HashSet<>();
    }

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.getFocused() instanceof ContainerEventHandler l)
            l.setFocused(null);
        this.setFocused(null);

        OverlaysManager.handleMouseClick(mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    //~ gui_rendering
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : this.renderables) {
            if (OverlaysManager.isVisibleOneContent(renderable)) continue;
            //~ if >=26.1 '.render(' -> '.extractRenderState(' {
            if (OverlaysManager.isMouseOverVisibleLayout(mouseX, mouseY)) {
                renderable.render(graphics, -1, -1, partialTick);
            } else {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
            //~}
        }

        OverlaysManager.renderVisibleOne(graphics, mouseX, mouseY, partialTick);
    }
    //~ !gui_rendering

    @Override
    public void onClose() {
        OverlaysManager.close();
        //? <=1.21.8
        assert this.minecraft != null;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
