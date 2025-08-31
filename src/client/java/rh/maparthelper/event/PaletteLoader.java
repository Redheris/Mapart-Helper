package rh.maparthelper.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import rh.maparthelper.config.palette.PaletteConfigManager;

public class PaletteLoader {
    private static boolean needs_regenerate_palette = false;

    public static void init() {
        needs_regenerate_palette = !PaletteConfigManager.readCompletePalette();
        if (!needs_regenerate_palette) {
            PaletteConfigManager.readPresetsConfigFile();
            return;
        }

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!needs_regenerate_palette) return;
            PaletteConfigManager.regenerateCompletePalette();
            PaletteConfigManager.readPresetsConfigFile();
            needs_regenerate_palette = false;
        });
    }
}
