package rh.maparthelper;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rh.maparthelper.config.MapartHelperConfig;

public class MapartHelper implements ModInitializer {
	public static final String MOD_ID = "mapart-helper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MapartHelperConfig config;

	@Override
	public void onInitialize() {
		AutoConfig.register(
				MapartHelperConfig.class,
				PartitioningSerializer.wrap(GsonConfigSerializer::new)
		);
		config = AutoConfig.getConfigHolder(MapartHelperConfig.class).getConfig();

		Commands.registerCommands();
	}
}