package rh.maparthelper.gui.screen;

import net.minecraft.client.Minecraft;
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
    protected final Screen parentScreen;
    private Set<OverlayLayout> overlays;

    protected ScreenAdapted(Screen parentScreen, Component title) {
        super(title);
        this.parentScreen = parentScreen;
    }

    protected void preInit() {}

    @Override
    protected final void init() {
        preInit();

        OverlaysManager.close(null);
        overlays = initOverlays();
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
        if (this.getFocused() instanceof ContainerEventHandler l) {
            if (l.getFocused() == null || !l.getFocused().isMouseOver(mouseX, mouseY))
                l.setFocused(null);
        }
        if (this.getFocused() == null || !this.getFocused().isMouseOver(mouseX, mouseY))
            this.setFocused(null);

        OverlaysManager.handleMouseClick(overlays, mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    //~ gui_rendering
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : this.renderables) {
            if (OverlaysManager.isVisibleOverlayContent(overlays, renderable)) continue;
            //~ if >=26.1 '.render(' -> '.extractRenderState(' {
            if (OverlaysManager.isMouseOverVisibleLayout(overlays, mouseX, mouseY)) {
                renderable.render(graphics, -1, -1, partialTick);
            } else {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
            //~}
        }

        if (Minecraft.getInstance().screen == this) {
            OverlaysManager.renderOverlays(overlays, graphics, mouseX, mouseY, partialTick);
        }
    }
    //~ !gui_rendering

    @Override
    public void onClose() {
        OverlaysManager.close(null);
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
