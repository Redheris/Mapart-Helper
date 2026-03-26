package rh.maparthelper.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import rh.maparthelper.config.palette.PaletteConfigManager;

public class PaletteLoader {
    public static boolean tagsLoaded = false;
    private static boolean needs_regenerate_palette = false;
    private static boolean eventRegistered = false;

    public static void load() {
        if (PaletteConfigManager.readCompletePalette()) {
            PaletteConfigManager.readPresetsConfigFile();
            return;
        }
        requestRegenerate();
    }

    public static void requestRegenerate() {
        if (tagsLoaded) {
            PaletteConfigManager.regenerateCompletePalette();
            PaletteConfigManager.readPresetsConfigFile();
        } else {
            needs_regenerate_palette = true;
            registerTagsLoadedEvent();
        }
    }

    private static void registerTagsLoadedEvent() {
        if (eventRegistered) return;

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            tagsLoaded = true;
            if (!needs_regenerate_palette) return;
            PaletteConfigManager.regenerateCompletePalette();
            PaletteConfigManager.readPresetsConfigFile();
            needs_regenerate_palette = false;
        });
        eventRegistered = true;
    }
}
