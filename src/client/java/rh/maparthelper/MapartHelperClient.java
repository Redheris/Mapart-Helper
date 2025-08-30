package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;
import rh.maparthelper.command.ClientCommands;
import rh.maparthelper.command.MapartToFile;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.event.ModEventsHandler;

public class MapartHelperClient implements ClientModInitializer {

    public static ConversionConfiguration conversionConfig;

    @Override
    public void onInitializeClient() {
        MapartToFile.initializeSavesDir();
        PaletteConfigManager.readPresetsConfigFile();

        ClientCommands.registerCommands();
        ModEventsHandler.registerAll();

        conversionConfig = MapartHelper.config.conversionSettings;
    }
}