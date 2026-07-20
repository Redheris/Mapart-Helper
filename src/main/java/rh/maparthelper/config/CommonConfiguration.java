package rh.maparthelper.config;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.world.level.block.Block;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.adapter.MapColorEntryAdapter;

import java.awt.*;

public class CommonConfiguration {
    public static ConfigClassHandler<CommonConfiguration> HANDLER =
            ConfigClassHandler.createBuilder(CommonConfiguration.class)
                    .id(MapartHelper.identifier("yacl_config_screen"))
                    .serializer(config ->
                            GsonConfigSerializerBuilder.create(config)
                                    .setPath(MapartHelper.CONFIG_PATH.resolve("mapart-helper.json"))
                                    .appendGsonBuilder(gson -> gson
                                            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                                            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
                                            .registerTypeAdapter(MapColorEntry.class, new MapColorEntryAdapter()))
                                    .build())
                    .build();

    @SerialEntry
    public Color selectionColor = new Color(0x9900ff);
    @SerialEntry
    public Color previewHighlightingColor = new Color(0xFF9900ff, true);

    // Conversion settings

    @SerialEntry
    public int maxMapartSize = 30;
    @SerialEntry
    public boolean logConversionTime = false;
    @SerialEntry
    public int fakeItemFramesLiveTime = 100;
    @SerialEntry
    public boolean previewHighlightOnHover = true;
    @SerialEntry
    public int colorsCacheLiveTimeMs = 5000;
    @SerialEntry("multithreadColorConversion_experimental")
    public boolean multithreadColorConversion = false;

    // Schematic Settings

    @SerialEntry
    public boolean createDirsForSchematic = true;
    @SerialEntry
    public boolean addPlatformLayerAuxBlocks = false;

    // Elements display settings

    @SerialEntry
    public boolean showImageImportButton = true;
    @SerialEntry
    public boolean displayUnobtainableMode = false;
    @SerialEntry
    public boolean showUseLABTooltip = true;
    @SerialEntry
    public boolean showStaircaseTooltips = true;

    // Palette generation settings

    @SerialEntry
    public UseInPalette useInPalette = new UseInPalette();

    public static class UseInPalette {
        public boolean onlyVanillaBlocks = true;
        // Limiters
        public boolean anyBlocks = false;
        public boolean onlySolid = false;
        public boolean onlyCarpets = false;
        // Filters
        public boolean candles = false;
        public boolean entityBlocks = false;
        public boolean buildDecorBlocks = false;
        public boolean creativeBlocks = false;
        public boolean growableBlocks = false;
        public boolean grassLikeBlocks = false;
    }
}
