package rh.maparthelper.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import rh.maparthelper.conversion.dithering.DitherAlgorithms;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

@Config(name = "conversion-settings")
public class ConversionConfiguration implements ConfigData {
    public StaircaseStyles staircaseStyle = StaircaseStyles.FLAT_2D;
    public int useAuxBlocks = -1;
    public String auxBlock = "block.minecraft.netherrack";
    public DitherAlgorithms ditherAlgorithm = DitherAlgorithms.NONE;
    public String currentPalette = "Default palette";

    public boolean use3D() {
        return this.staircaseStyle != StaircaseStyles.FLAT_2D;
    }
}
