package rh.maparthelper.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import rh.maparthelper.conversion.dithering.DitherAlgorithms;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

@Config(name = "conversion-settings")
public class ConversionConfiguration implements ConfigData {
    public StaircaseStyles staircaseStyle = StaircaseStyles.FLAT_2D;
    public int useAuxBlocks = 0;
    public String auxBlock = "minecraft:netherrack";
    public DitherAlgorithms ditherAlgorithm = DitherAlgorithms.NONE;

    public boolean use3D() {
        return this.staircaseStyle != StaircaseStyles.FLAT_2D;
    }
}
