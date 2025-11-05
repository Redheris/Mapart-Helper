package rh.maparthelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.command.ServerCommands;
import rh.maparthelper.config.CommonConfiguration;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.config.adapter.MapColorEntryAdapter;

import java.nio.file.Path;

public class MapartHelper implements ModInitializer {
    public static final String MOD_ID = "maparthelper";
    public static final String MOD_NAME = "Mapart Helper";
    public static final String CONFIG_DIR = "mapart-helper";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static CommonConfiguration commonConfig;
    public static ConversionConfiguration conversionSettings;

    @Override
    public void onInitialize() {
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
                .registerTypeAdapter(MapColorEntry.class, new MapColorEntryAdapter())
                .setPrettyPrinting()
                .create();
        AutoConfig.register(
                MapartHelperConfig.class,
                PartitioningSerializer.wrap((c, clazz) ->
                        new GsonConfigSerializer<>(c, clazz, gson)
                )
        );
        MapartHelperConfig config = AutoConfig.getConfigHolder(MapartHelperConfig.class).getConfig();
        commonConfig = config.commonConfiguration;
        conversionSettings = config.conversionSettings;

        ServerCommands.registerCommands();
    }
}
