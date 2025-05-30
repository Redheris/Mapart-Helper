package rh.maparthelper;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapartHelper implements ModInitializer {
	public static final String MOD_ID = "mapart-helper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Commands.registerCommands();
	}
}