package rh.maparthelper.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import rh.maparthelper.MapartHelper;

@Config(name = MapartHelper.MOD_ID)
public class CommonConfiguration implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker
    public int selectionColor = 0x9900ff;

    public int fakeItemFramesLiveTime = 100;
    public boolean logConversionTime = false;
    public boolean showUseLABTooltip = true;
    public boolean showStaircaseTooltips = true;
    @ConfigEntry.Gui.Tooltip
    public boolean scaleBlockWidgets = false;

    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.Gui.CollapsibleObject
    public UseInBlockPalette useInPalette = new UseInBlockPalette();

    public static class UseInBlockPalette {
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean anyBlocks = false;
        public boolean onlySolid = false;
        public boolean onlyCarpets = false;
        public boolean blocksWithEntities = false;
        @ConfigEntry.Gui.Tooltip
        public boolean buildDecorBlocks = false;
        @ConfigEntry.Gui.Tooltip
        public boolean needWaterBlocks = false;
        public boolean creativeBlocks = false;
        @ConfigEntry.Gui.Tooltip
        public boolean growableBlocks = false;
        public boolean grassLikeBlocks = false;
    }
}
