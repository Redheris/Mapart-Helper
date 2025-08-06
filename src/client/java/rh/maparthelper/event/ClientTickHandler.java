package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.gui.MapartEditorScreen;

public class ClientTickHandler {
    public static void init() {
        KeyBinding openScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.maparthelper.openScreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KeyBinding.UI_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.wasPressed()) {
                client.setScreen(new MapartEditorScreen());
            }
        });
    }
}
