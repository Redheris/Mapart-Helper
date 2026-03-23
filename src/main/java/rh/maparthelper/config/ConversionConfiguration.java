package rh.maparthelper.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.dithering.ColorConverters;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

@Config(name = "conversion-settings")
public class ConversionConfiguration implements ConfigData {
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
        saveConfigFile();
    }

    public Block getAuxBlock() {
        return auxBlock;
    }

    public void setAuxBlock(Block auxBlock) {
        if (this.auxBlock == auxBlock) return;
        this.auxBlock = auxBlock;
        saveConfigFile();
    }

    public ColorConverters getColorConverter() {
        return colorConverter;
    }

    public void setColorConverter(ColorConverters colorConverter) {
        if (this.colorConverter == colorConverter) return;
        this.colorConverter = colorConverter;
        saveConfigFile();
    }

    public MaterialsCountModes getMaterialsCountMode() {
        return materialsCountMode;
    }

    public void nextMaterialsCountMode() {
        this.materialsCountMode = MaterialsCountModes.nextMode(materialsCountMode);
        saveConfigFile();
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
        saveConfigFile();
    }

    public void toggleLAB() {
        this.useLAB = !useLAB;
        PaletteColors.clearColorCache();
        saveConfigFile();
    }

    public StaircaseStyles getStaircaseStyle() {
        return staircaseStyle;
    }

    /// @return whether use3D value is changed
    public boolean setStaircaseStyle(StaircaseStyles staircaseStyle) {
        boolean was3D = use3D();
        boolean needUpdate = this.staircaseStyle == StaircaseStyles.UNOBTAINABLE || staircaseStyle == StaircaseStyles.UNOBTAINABLE;
        this.staircaseStyle = staircaseStyle;
        saveConfigFile();
        if (needUpdate || was3D != use3D()) {
            PaletteColors.clearColorCache();
            return true;
        }
        return false;
    }

    public static void saveConfigFile() {
        AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
    }
}
