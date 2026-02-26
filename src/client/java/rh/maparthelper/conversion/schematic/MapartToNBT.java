package rh.maparthelper.conversion.schematic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
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
import rh.maparthelper.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MapartToNBT {
    private static final Path SCHEMATICS = FabricLoader.getInstance().getGameDir().resolve("schematics");

    private static final ExecutorService nbtBuilderExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat(MapartHelper.MOD_NAME + "/Schematic")
                    .build()
    );

    private static void saveNBT(boolean asSingleFile, ZipOutputStream zipOut, File zipFile) {
        NativeImage mapartImage = CurrentConversionSettings.guiMapartImage.getImage();
        int mapsWidth = CurrentConversionSettings.getMapartWidth();
        int mapsHeight = CurrentConversionSettings.getMapartHeight();
        assert mapartImage != null;

        try {
            if (!Files.exists(SCHEMATICS)) {
                Files.createDirectory(SCHEMATICS);
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write schematics directory", e);
        }

        int[][] maps;
        if (asSingleFile) {
            maps = new int[][]{mapartImage.copyPixelsArgb()};
        } else {
            maps = NativeImageUtils.divideImageByMaps(
                    CurrentConversionSettings.getMapartWidth(), CurrentConversionSettings.getMapartHeight(), mapartImage
            );
        }
        assert maps != null;

        for (int i = 0; i < maps.length; i++) {
            String filename = CurrentConversionSettings.mapart.mapartName;

            NbtCompound mapartNbt;
            if (asSingleFile)
                mapartNbt = new MapartSchematicBuilder(maps[0], mapsWidth, mapsHeight).build();
            else {
                mapartNbt = new MapartSchematicBuilder(maps[i], 1, 1).build();
                filename += " (" + (i % mapsWidth) + "_" + (i / mapsWidth) + ")";
            }

            String writeFilename = Utils.makeUniqueFilename(SCHEMATICS, filename, "nbt");
            try {
                if (zipOut == null) {
                    NbtIo.writeCompressed(mapartNbt, SCHEMATICS.resolve(writeFilename));
                } else {
                    ZipEntry zipEntry = new ZipEntry(filename + ".nbt");
                    zipOut.putNextEntry(zipEntry);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    NbtIo.writeCompressed(mapartNbt, out);
                    out.writeTo(zipOut);
                }
            } catch (IOException e) {
                MapartHelper.LOGGER.error("An error occurred during saving NBT file", e);
                return;
            }
        }

        String mapartName = CurrentConversionSettings.mapart.mapartName;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            Text openFile;
            if (zipOut == null) {
                openFile = Text.literal("schematics").styled(style -> style
                        .withClickEvent(new ClickEvent.OpenFile(SCHEMATICS))
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("maparthelper.open_folder")))
                        .withUnderline(true)
                );
            } else {
                openFile = Text.literal(zipFile.getName()).styled(style -> style
                        .withClickEvent(new ClickEvent.OpenFile(zipFile))
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("maparthelper.open_file")))
                        .withUnderline(true)
                );
            }
            MutableText message;
            if (asSingleFile)
                message = Text.translatable("maparthelper.nbt_file_saved", mapartName, openFile);
            else if (zipOut == null)
                message = Text.translatable("maparthelper.nbt_files_saved", maps.length, mapartName, openFile);
            else
                message = Text.translatable("maparthelper.nbt_zip_saved", maps.length, mapartName, openFile);
            sendMessageSafe(player, message.formatted(Formatting.GREEN));
        }
        MapartHelper.LOGGER.info("{} NBT file(s) for \"{}\" successfully saved", maps.length, mapartName);
    }

    public static void saveNBT(boolean asSingleFile) {
        if (CurrentConversionSettings.guiMapartImage == null)
            return;
        nbtBuilderExecutor.execute(() -> saveNBT(asSingleFile, null, null));
    }

    public static void saveNBTAsZip() {
        if (CurrentConversionSettings.guiMapartImage == null)
            return;
        String filename = Utils.makeUniqueFilename(SCHEMATICS, CurrentConversionSettings.mapart.mapartName, "zip");
        File fileToZip = SCHEMATICS.resolve(filename).toFile();

        nbtBuilderExecutor.execute(() -> {
            try (FileOutputStream fos = new FileOutputStream(fileToZip);
                 ZipOutputStream zipOut = new ZipOutputStream(fos)
            ) {
                saveNBT(false, zipOut, fileToZip);
            } catch (IOException e) {
                MapartHelper.LOGGER.error("An error occurred during saving zip", e);
            }
        });
    }

    private static void sendMessageSafe(PlayerEntity player, Text message) {
        MinecraftClient.getInstance().execute(() -> player.sendMessage(message, false));
    }
}
