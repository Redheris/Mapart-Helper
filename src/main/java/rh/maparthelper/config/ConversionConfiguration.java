package rh.maparthelper.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

@Config(name = "conversion-settings")
public class ConversionConfiguration implements ConfigData {
    public StaircaseStyles staircaseStyle = StaircaseStyles.FLAT_2D;
    public int useAuxBlocks = 0;
    public Block auxBlock = Blocks.NETHERRACK;
    public DitheringAlgorithms ditheringAlgorithm = DitheringAlgorithms.NONE;
    public boolean useLAB = false;

    public boolean use3D() {
        return this.staircaseStyle != StaircaseStyles.FLAT_2D;
    }
}
