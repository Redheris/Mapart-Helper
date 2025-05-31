package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;
import rh.maparthelper.config.ConversionConfiguration;

public class MapartHelperClient implements ClientModInitializer {

	public static ConversionConfiguration conversionConfig;

	@Override
	public void onInitializeClient() {
		MapartToFile.initializeSavesDir();
		ClientCommands.registerCommands();

		conversionConfig = MapartHelper.config.conversionSettings;
	}
}