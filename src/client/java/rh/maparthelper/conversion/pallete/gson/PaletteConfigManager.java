package rh.maparthelper.conversion.pallete.gson;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.pallete.PalettePresetsConfig;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PaletteConfigManager {
    public static PalettePresetsConfig palettePresetsConfig;

    private static final Path PRESETS_PATH = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID).resolve("palette_presets.json");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(new TypeToken<Map<MapColor, Block>>(){}.getType(), new MapColorEntryAdapter())
            .create();

    // Reading JSON file containing all user presets
    public static PalettePresetsConfig readPresetsFile() {
        palettePresetsConfig = new PalettePresetsConfig();
        if (!Files.exists(PRESETS_PATH)) {
            palettePresetsConfig = new PalettePresetsConfig();
            palettePresetsConfig.createNewPreset();
            savePresetsFile();
            return palettePresetsConfig;
        }
        try (FileReader reader = new FileReader(PRESETS_PATH.toFile())) {
            palettePresetsConfig = gson.fromJson(reader, PalettePresetsConfig.class);
        } catch (Exception e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
        return palettePresetsConfig;
    }

    // Updating JSON file containing all user presets
    public static void savePresetsFile() {
        try (FileWriter writer = new FileWriter(PRESETS_PATH.toFile())) {
            gson.toJson(palettePresetsConfig, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }
}
