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
import rh.maparthelper.util.Utils;

import java.io.*;
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
        assert CurrentConversionSettings.guiMapartImage.getImage() != null;

        try {
            if (!Files.exists(SCHEMATICS)) {
                Files.createDirectory(SCHEMATICS);
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write schematics directory", e);
        }

        int[][] maps;
        if (asSingleFile) {
            maps = new int[][]{CurrentConversionSettings.guiMapartImage.getImage().copyPixelsArgb()};
        } else {
            maps = NativeImageUtils.divideMapartByMaps(CurrentConversionSettings.mapart);
        }
        assert maps != null;

        for (int i = 0; i < maps.length; i++) {
            String filename = CurrentConversionSettings.mapart.mapartName;

            NbtCompound mapartNbt;
            if (asSingleFile)
                mapartNbt = NbtSchematicUtils.createMapartNbt();
            else {
                mapartNbt = NbtSchematicUtils.createMapartNbt(maps[i], 1, 1);
                filename += " (" + (i % CurrentConversionSettings.getMapartWidth()) + "_" + (i / CurrentConversionSettings.getMapartWidth()) + ")";
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
            player.sendMessage(message.formatted(Formatting.GREEN), false);
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
}
