package rh.maparthelper.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.dithering.ColorConverters;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

@Config(name = "conversion-settings")
public class ConversionConfiguration implements ConfigData {
    private StaircaseStyles staircaseStyle = StaircaseStyles.FLAT_2D;
    public UseAuxBlocks useAuxBlocks = UseAuxBlocks.IMPORTANT;
    public Block auxBlock = Blocks.NETHERRACK;
    public ColorConverters colorConverter = ColorConverters.SIMPLE;
    private MapColorEntry backgroundColor = MapColorEntry.CLEAR;
    public MaterialsCountModes materialsCountMode = MaterialsCountModes.PER_MAP;

    private transient boolean useLAB = false;
    public transient boolean showOriginalImage = false;

    public boolean use3D() {
        return this.staircaseStyle != StaircaseStyles.FLAT_2D;
    }

    public boolean useLAB() {
        return useLAB;
    }

    public int getBackgroundRenderColor() {
        return use3D() ? backgroundColor.getRenderColor() : backgroundColor.mapColor().getRenderColor(MapColor.Brightness.NORMAL);
    }

    public MapColorEntry getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(MapColorEntry backgroundColor) {
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
        this.staircaseStyle = staircaseStyle;
        if(was3D != use3D()) {
            PaletteColors.clearColorCache();
            return true;
        }
        return false;
    }
}
