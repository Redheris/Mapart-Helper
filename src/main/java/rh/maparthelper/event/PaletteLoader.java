package rh.maparthelper.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import rh.maparthelper.palette.PaletteDataManager;

public class PaletteLoader {
    public static boolean tagsLoaded = false;
    private static boolean needs_regenerate_palette = false;

    public static void load() {
        PaletteDataManager paletteDataManager = PaletteDataManager.getInstance();

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            tagsLoaded = true;
            if (!needs_regenerate_palette) return;

            paletteDataManager.updatePaletteGameVersion(true);
            paletteDataManager.readPresetsConfig();
            needs_regenerate_palette = false;
        });

        if (paletteDataManager.readCompletePalette()) {
            paletteDataManager.readPresetsConfig();
        } else {
            needs_regenerate_palette = true;
        }
    }

    public static void requestRegenerate() {
        PaletteDataManager paletteDataManager = PaletteDataManager.getInstance();
        if (tagsLoaded) {
            paletteDataManager.updatePaletteGameVersion(true);
            paletteDataManager.readPresetsConfig();
        } else {
            needs_regenerate_palette = true;
        }
    }
}
