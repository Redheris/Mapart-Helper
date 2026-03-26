package rh.maparthelper.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.adapter.MapColorEntryAdapter;

import java.awt.*;

public class CommonConfiguration {
    public static ConfigClassHandler<CommonConfiguration> HANDLER =
            ConfigClassHandler.createBuilder(CommonConfiguration.class)
                    .id(Identifier.fromNamespaceAndPath(MapartHelper.MOD_ID, "yacl_try"))
                    .serializer(config ->
                            GsonConfigSerializerBuilder.create(config)
                                    .setPath(MapartHelper.CONFIG_PATH.resolve("yacl_config.json"))
                                    .appendGsonBuilder(gson -> gson
                                            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
                                            .registerTypeAdapter(MapColorEntry.class, new MapColorEntryAdapter()))
                                    .build())
                    .build();

    @SerialEntry
    public Color selectionColor = new Color(0x9900ff);
    @SerialEntry
    public Color previewHighlightingColor = new Color(0xFF9900ff, true);

    @SerialEntry
    public boolean logConversionTime = false;
    @SerialEntry
    public boolean previewHighlightOnHover = true;
    @SerialEntry
    public boolean createDirsForSchematic = true;
    @SerialEntry
    public int colorsCacheLiveTimeMs = 5000;
    @SerialEntry
    public int fakeItemFramesLiveTime = 100;

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
    public boolean anyBlocks = false;
    @SerialEntry
    public boolean onlySolid = false;
    @SerialEntry
    public boolean onlyCarpets = false;
    @SerialEntry
    public boolean entityBlocks = false;
    @SerialEntry
    public boolean buildDecorBlocks = false;
    @SerialEntry
    public boolean needWaterBlocks = false;
    @SerialEntry
    public boolean creativeBlocks = false;
    @SerialEntry
    public boolean growableBlocks = false;
    @SerialEntry
    public boolean grassLikeBlocks = false;
}
