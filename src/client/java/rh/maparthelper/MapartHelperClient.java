package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;
import rh.maparthelper.command.ClientCommands;
import rh.maparthelper.command.MapartToFile;
import rh.maparthelper.event.ModEventsHandler;
import rh.maparthelper.event.PaletteLoader;

public class MapartHelperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MapartToFile.initializeSavesDir();

        ClientCommands.registerCommands();
        ModEventsHandler.registerAll();

        PaletteLoader.load();
    }
}