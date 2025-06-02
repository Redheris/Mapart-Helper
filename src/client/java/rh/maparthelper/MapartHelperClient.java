package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.event.ModEventsHandler;

public class MapartHelperClient implements ClientModInitializer {

    public static ConversionConfiguration conversionConfig;

    @Override
    public void onInitializeClient() {
        MapartToFile.initializeSavesDir();
        ClientCommands.registerCommands();
        ModEventsHandler.registerAll();

        conversionConfig = MapartHelper.config.conversionSettings;
    }
}