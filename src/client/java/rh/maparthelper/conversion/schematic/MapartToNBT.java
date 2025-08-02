package rh.maparthelper.conversion.schematic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import rh.maparthelper.conversion.CurrentConversionSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapartToNBT {
    private static final Path SCHEMATICS = FabricLoader.getInstance().getGameDir().resolve("schematics");

    private static final ExecutorService nbtBuilderExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Mart Helper Schematic-%d").build());

    public static void saveNBT() {
        nbtBuilderExecutor.execute(() -> {
            try {
                NbtCompound mapartNbt = NbtSchematicUtils.createMapartNbt();
                String filename = CurrentConversionSettings.mapartName;
                if (Files.exists(SCHEMATICS.resolve(filename + ".nbt"))) {
                    int suffix = 1;
                    while (Files.exists(SCHEMATICS.resolve(filename + " (" + suffix + ").nbt")))
                        suffix++;
                    filename = filename + " (" + suffix + ")";
                }
                NbtIo.writeCompressed(mapartNbt, SCHEMATICS.resolve(filename + ".nbt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
