package rh.maparthelper.state;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
//import rh.maparthelper.gui.painter.PainterScreen;
import rh.maparthelper.gui.screen.FullscreenImageViewScreen;
import rh.maparthelper.gui.screen.MapartEditorScreen;

public class ActiveModScreenManager {
    private static final ActiveModScreenManager INSTANCE = new ActiveModScreenManager();
    private ModScreen activeModScreen = ModScreen.MAPART_EDITOR;

    private ActiveModScreenManager() {}

    public static ActiveModScreenManager getInstance() {
        return INSTANCE;
    }

    public void openModScreen() {
        Screen screen = switch (activeModScreen) {
            case MAPART_EDITOR -> new MapartEditorScreen();
            case FULLSCREEN_VIEW -> new FullscreenImageViewScreen();
//            case MAPART_PAINTER -> new PainterScreen();
        };
        Minecraft.getInstance().setScreen(screen);
    }

    public void setActiveModScreen(ModScreen activeModScreen) {
        this.activeModScreen = activeModScreen;
    }

    public enum ModScreen {
        MAPART_EDITOR,
        FULLSCREEN_VIEW
//        MAPART_PAINTER
    }
}
