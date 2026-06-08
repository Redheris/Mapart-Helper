package rh.maparthelper.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import rh.maparthelper.config.palette.PaletteConfigManager;

public class PaletteLoader {
    public static boolean tagsLoaded = false;
    private static boolean needs_regenerate_palette = false;

    public static void load() {
//        PaletteDataManager paletteDataManager = PaletteDataManager.getInstance();

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            tagsLoaded = true;
            if (!needs_regenerate_palette) return;

            // TODO: Replace old implementation ===========
            PaletteConfigManager.regenerateCompletePalette();
            PaletteConfigManager.readPresetsConfigFile();
            // ============================================

//            paletteDataManager.updatePaletteGameVersion(true);
//            paletteDataManager.readPresetsConfig();
            needs_regenerate_palette = false;
        });

//        if (paletteDataManager.readCompletePalette()) {
//            paletteDataManager.readPresetsConfig();
//        } else {
//            needs_regenerate_palette = true;
//        }

        // TODO: Replace old implementation ===========
        if (PaletteConfigManager.readCompletePalette()) {
            PaletteConfigManager.readPresetsConfigFile();
            return;
        }
        needs_regenerate_palette = true;
        // ============================================
    }

    public static void requestRegenerate() {
        if (tagsLoaded) {
            // TODO: Replace old implementation
            PaletteConfigManager.regenerateCompletePalette();
            PaletteConfigManager.readPresetsConfigFile();
        } else {
            needs_regenerate_palette = true;
        }
    }
}
