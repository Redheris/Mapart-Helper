package rh.maparthelper.conversion.schematic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.NativeImageUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapartToNBT {
    private static final Path SCHEMATICS = FabricLoader.getInstance().getGameDir().resolve("schematics");

    private static final ExecutorService nbtBuilderExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Mart Helper Schematic-%d").build());

    public static void saveNBT(boolean asSingleFile) {
        if (CurrentConversionSettings.guiMapartImage == null)
            return;
        nbtBuilderExecutor.execute(() -> {
            try {
                int[][] maps;
                if (asSingleFile) {
                    assert CurrentConversionSettings.guiMapartImage.getImage() != null;
                    maps = new int[][]{CurrentConversionSettings.guiMapartImage.getImage().copyPixelsArgb()};
                } else {
                    maps = NativeImageUtils.divideMapartByMaps();
                }
                assert maps != null;
                for (int i = 0; i < maps.length; i++) {
                    NbtCompound mapartNbt;
                    String filename = CurrentConversionSettings.mapartName;
                    if (asSingleFile)
                        mapartNbt = NbtSchematicUtils.createMapartNbt();
                    else {
                        mapartNbt = NbtSchematicUtils.createMapartNbt(maps[i], 1, 1);
                        filename += "_" + (i % CurrentConversionSettings.getWidth()) + "_" + (i / CurrentConversionSettings.getWidth());
                    }

                    if (Files.exists(SCHEMATICS.resolve(filename + ".nbt"))) {
                        int suffix = 1;
                        while (Files.exists(SCHEMATICS.resolve(filename + " (" + suffix + ").nbt")))
                            suffix++;
                        filename = filename + " (" + suffix + ")";
                    }
                    NbtIo.writeCompressed(mapartNbt, SCHEMATICS.resolve(filename + ".nbt"));
                }
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    Text folder = Text.literal("schematics").styled(style -> style
                            .withClickEvent(new ClickEvent.OpenFile(SCHEMATICS))
                            .withHoverEvent(new HoverEvent.ShowText(Text.translatable("maparthelper.open_folder")))
                            .withUnderline(true)
                    );
                    MutableText message;
                    if (asSingleFile)
                        message = Text.translatable("maparthelper.nbt_file_saved", CurrentConversionSettings.mapartName, folder);
                    else
                        message = Text.translatable("maparthelper.nbt_files_saved", maps.length, CurrentConversionSettings.mapartName, folder);
                    player.sendMessage(message.formatted(Formatting.GREEN),false);
                }
                MapartHelper.LOGGER.info("{} NBT file(s) for \"{}\" successfully saved", maps.length, CurrentConversionSettings.mapartName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
