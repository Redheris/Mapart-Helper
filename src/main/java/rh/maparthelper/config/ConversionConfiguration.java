package rh.maparthelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.adapter.BlockTypeAdapter;
import rh.maparthelper.config.adapter.MapColorEntryAdapter;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.dithering.ColorConverters;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConversionConfiguration {
    private final static Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Block.class, new BlockTypeAdapter())
            .registerTypeAdapter(MapColorEntry.class, new MapColorEntryAdapter())
            .setPrettyPrinting()
            .create();
    private final static Path configPath = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(MapartHelper.CONFIG_PATH)
            .resolve("yacl_config_nogui.json");

    public static ConversionConfiguration instance;

    public static void load() {
        if (!Files.exists(configPath)) {
            instance = new ConversionConfiguration();
            save();
            return;
        }
        try (FileReader reader = new FileReader(configPath.toFile())) {
            instance = gson.fromJson(reader, ConversionConfiguration.class);
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Failed to read JSON syntax \"{}\": {}", configPath, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            gson.toJson(instance, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    private StaircaseStyles staircaseStyle = StaircaseStyles.FLAT_2D;
    private UseAuxBlocks useAuxBlocks = UseAuxBlocks.IMPORTANT;
    private Block auxBlock = Blocks.NETHERRACK;
    private ColorConverters colorConverter = ColorConverters.SIMPLE;
    private MapColorEntry backgroundColor = MapColorEntry.CLEAR;
    private MaterialsCountModes materialsCountMode = MaterialsCountModes.PER_MAP;
    private boolean useLAB = false;

    private transient boolean showOriginalImage = false;


    public boolean isShowOriginalImage() {
        return showOriginalImage;
    }

    public void toggleShowOriginalImage() {
        this.showOriginalImage = !showOriginalImage;
    }

    public UseAuxBlocks getUseAuxBlocks() {
        return useAuxBlocks;
    }

    public void setUseAuxBlocks(UseAuxBlocks useAuxBlocks) {
        if (this.useAuxBlocks == useAuxBlocks) return;
        this.useAuxBlocks = useAuxBlocks;
    }

    public Block getAuxBlock() {
        return auxBlock;
    }

    public void setAuxBlock(Block auxBlock) {
        if (this.auxBlock == auxBlock) return;
        this.auxBlock = auxBlock;
    }

    public ColorConverters getColorConverter() {
        return colorConverter;
    }

    public void setColorConverter(ColorConverters colorConverter) {
        if (this.colorConverter == colorConverter) return;
        this.colorConverter = colorConverter;
    }

    public MaterialsCountModes getMaterialsCountMode() {
        return materialsCountMode;
    }

    public void nextMaterialsCountMode() {
        this.materialsCountMode = MaterialsCountModes.nextMode(materialsCountMode);
    }

    public boolean use3D() {
        return this.staircaseStyle != StaircaseStyles.FLAT_2D;
    }

    public boolean useUnobtainable() {
        return this.staircaseStyle == StaircaseStyles.UNOBTAINABLE;
    }

    public boolean useLAB() {
        return useLAB;
    }

    public int getBackgroundRenderColor() {
        return use3D() ? backgroundColor.getRenderColor() : backgroundColor.mapColor().calculateARGBColor(MapColor.Brightness.NORMAL);
    }

    public MapColorEntry getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(MapColorEntry backgroundColor) {
        if (this.backgroundColor.equals(backgroundColor)) return;
        this.backgroundColor = backgroundColor;
    }

    public void toggleLAB() {
        this.useLAB = !useLAB;
        PaletteColors.clearColorCache();
    }

    public StaircaseStyles getStaircaseStyle() {
        return staircaseStyle;
    }

    /// @return whether use3D value is changed
    public boolean setStaircaseStyle(StaircaseStyles staircaseStyle) {
        boolean was3D = use3D();
        boolean needUpdate = this.staircaseStyle == StaircaseStyles.UNOBTAINABLE || staircaseStyle == StaircaseStyles.UNOBTAINABLE;
        this.staircaseStyle = staircaseStyle;
        if (needUpdate || was3D != use3D()) {
            PaletteColors.clearColorCache();
            return true;
        }
        return false;
    }
}
