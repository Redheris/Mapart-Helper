package rh.maparthelper.conversion.schematic;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import rh.maparthelper.conversion.CurrentConversionSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapartToNBT {
    private static final Path SCHEMATICS = FabricLoader.getInstance().getGameDir().resolve("schematics");
    
    public static void saveNBT() {
        NbtCompound base = NbtSchematicUtils.createMapArtBaseNbt(CurrentConversionSettings.width, CurrentConversionSettings.height);
        try {
            String filename = CurrentConversionSettings.mapartName;
            if (Files.exists(SCHEMATICS.resolve(filename + ".nbt"))) {
                int suffix = 1;
                while (Files.exists(SCHEMATICS.resolve(filename + " (" + suffix + ").nbt")))
                    suffix++;
                filename = filename + " (" + suffix + ")";
            }
            NbtIo.writeCompressed(base, SCHEMATICS.resolve(filename + ".nbt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
