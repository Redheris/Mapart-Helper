package rh.maparthelper.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import rh.maparthelper.MapartHelper;

@Config(name = MapartHelper.MOD_ID)
public class MapartHelperConfig extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Gui.TransitiveObject
    public CommonConfiguration commonConfiguration = new CommonConfiguration();

    @ConfigEntry.Gui.Excluded
    @ConfigEntry.Gui.TransitiveObject
    public ConversionConfiguration conversionSettings = new ConversionConfiguration();
}

