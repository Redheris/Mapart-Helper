package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;

public class MapartHelperClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MapartToFile.initializeSavesDir();
		ClientCommands.registerCommands();
	}
}