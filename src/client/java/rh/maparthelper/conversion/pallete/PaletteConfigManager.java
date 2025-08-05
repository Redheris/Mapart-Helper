package rh.maparthelper.conversion.pallete;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.pallete.gson.MapColorEntryAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PaletteConfigManager {
    private static final Path PRESETS_PATH = FabricLoader.getInstance().getConfigDir().resolve(MapartHelper.MOD_ID).resolve("palette_presets.json");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(new TypeToken<Map<MapColor, Block>>(){}.getType(), new MapColorEntryAdapter())
            .create();

    public static PalettePresetsConfig palettePresetsConfig;

    // Reading JSON file containing all user presets
    public static void readPresetsFile() {
        if (!Files.exists(PRESETS_PATH)) {
            palettePresetsConfig = new PalettePresetsConfig();
            return;
        }
        try (FileReader reader = new FileReader(PRESETS_PATH.toFile())) {
            palettePresetsConfig = gson.fromJson(reader, PalettePresetsConfig.class);
            palettePresetsConfig.validateConfig();
        } catch (Exception e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    // Updating JSON file containing all user presets
    public static void savePresetsFile() {
        try (FileWriter writer = new FileWriter(PRESETS_PATH.toFile())) {
            gson.toJson(palettePresetsConfig, writer);
        } catch (IOException e) {
            MapartHelper.LOGGER.error(e.getMessage(), e);
        }
    }

    public static void changeCurrentPreset(String name) {
        palettePresetsConfig.changeCurrentPreset(name);
        savePresetsFile();
    }

    public static void createNewPreset() {
        palettePresetsConfig.createNewPreset();
        savePresetsFile();
    }

    public static void renamePreset(String oldName, String newName) {
        palettePresetsConfig.renamePreset(oldName, newName);
        savePresetsFile();
    }

    public static void duplicatePreset(String name) {
        palettePresetsConfig.duplicatePreset(name);
        savePresetsFile();
    }
}
