package rh.maparthelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rh.maparthelper.config.BlockTypeAdapter;
import rh.maparthelper.config.MapartHelperConfig;

public class MapartHelper implements ModInitializer {
    public static final String MOD_ID = "mapart-helper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MapartHelperConfig config;

    @Override
    public void onInitialize() {
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
                .setPrettyPrinting()
                .create();
        AutoConfig.register(
                MapartHelperConfig.class,
                PartitioningSerializer.wrap((c, clazz) ->
                        new GsonConfigSerializer<>(c, clazz, gson)
                )
        );
        config = AutoConfig.getConfigHolder(MapartHelperConfig.class).getConfig();

        Commands.registerCommands();
    }
}
