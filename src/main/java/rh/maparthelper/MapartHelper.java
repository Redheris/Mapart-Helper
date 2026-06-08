package rh.maparthelper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rh.maparthelper.command.ServerCommands;
import rh.maparthelper.config.CommonConfiguration;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.event.PaletteLoader;

import java.nio.file.Path;

public class MapartHelper implements ModInitializer {
    public static final String MOD_ID = /*$ mod_id */ "maparthelper";
    public static final String MOD_NAME = /*$ mod_name */ "Mapart Helper";
    public static final String CONFIG_DIR = "mapart-helper";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        CommonConfiguration.HANDLER.load();
        ConversionConfiguration.load();
        PaletteColors.initMapColorsCache();
        PaletteLoader.load();

        ServerCommands.registerCommands();
    }

    public static CommonConfiguration commonConfig() {
        return CommonConfiguration.HANDLER.instance();
    }

    public static ConversionConfiguration conversionConfig() {
        return ConversionConfiguration.getInstance();
    }
}
