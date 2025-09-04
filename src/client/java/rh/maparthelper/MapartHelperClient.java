package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.texture.TextureManager;
import rh.maparthelper.command.ClientCommands;
import rh.maparthelper.command.MapartToFile;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.event.ModEventsHandler;
import rh.maparthelper.event.PaletteLoader;

public class MapartHelperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MapartToFile.initializeSavesDir();

        ClientCommands.registerCommands();
        ModEventsHandler.registerAll();

        PaletteLoader.init();
        registerClientStartedEvents();
    }

    private static void registerClientStartedEvents() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            TextureManager textureManager = client.getTextureManager();
            textureManager.registerTexture(CurrentConversionSettings.guiMapartId, CurrentConversionSettings.guiMapartImage);
        });
    }
}