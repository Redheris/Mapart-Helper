package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.texture.TextureManager;
import rh.maparthelper.conversion.CurrentConversionSettings;

public class ModEventsHandler {

    public static void registerAll() {
        AttackBlockHandler.init();
        AttackEntityHandler.init();
        ClientTickHandler.init();

        registerOtherEvents();
    }

    private static void registerOtherEvents() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            TextureManager textureManager = client.getTextureManager();
            textureManager.registerTexture(CurrentConversionSettings.guiMapartId, CurrentConversionSettings.guiMapartImage);
        });
    }
}
