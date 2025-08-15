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

import java.io.File;
import java.io.FileInputStream;
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

    private static final ExecutorService nbtBuilderExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Mart Helper Schematic-%d").build());

    private static void saveNBT(boolean asSingleFile, ZipOutputStream zipOut, File zipFile) {
        assert CurrentConversionSettings.guiMapartImage.getImage() != null;

        int[][] maps;
        if (asSingleFile) {
            maps = new int[][]{CurrentConversionSettings.guiMapartImage.getImage().copyPixelsArgb()};
        } else {
            maps = NativeImageUtils.divideMapartByMaps();
        }
        assert maps != null;

        for (int i = 0; i < maps.length; i++) {
            String filename = CurrentConversionSettings.mapartName;

            NbtCompound mapartNbt;
            if (asSingleFile)
                mapartNbt = NbtSchematicUtils.createMapartNbt();
            else {
                mapartNbt = NbtSchematicUtils.createMapartNbt(maps[i], 1, 1);
                filename += " (" + (i % CurrentConversionSettings.getWidth()) + "_" + (i / CurrentConversionSettings.getWidth()) + ")";
            }

            String writeFilename = makeUniqueFilename(filename, "nbt");
            try {
                NbtIo.writeCompressed(mapartNbt, SCHEMATICS.resolve(writeFilename));
                if (zipOut != null) {
                    try (FileInputStream fis = new FileInputStream(SCHEMATICS.resolve(writeFilename).toFile())) {
                        ZipEntry zipEntry = new ZipEntry(filename);
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(fis.readAllBytes());
                    }
                    finally {
                        Files.delete(SCHEMATICS.resolve(writeFilename));
                    }
                }
            } catch (IOException e) {
                MapartHelper.LOGGER.error("An error occurred during saving NBT file", e);
                return;
            }
        }

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
                message = Text.translatable("maparthelper.nbt_file_saved", CurrentConversionSettings.mapartName, openFile);
            else if (zipOut == null)
                message = Text.translatable("maparthelper.nbt_files_saved", maps.length, CurrentConversionSettings.mapartName, openFile);
            else
                message = Text.translatable("maparthelper.nbt_zip_saved", maps.length, CurrentConversionSettings.mapartName, openFile);
            player.sendMessage(message.formatted(Formatting.GREEN), false);
        }
        MapartHelper.LOGGER.info("{} NBT file(s) for \"{}\" successfully saved", maps.length, CurrentConversionSettings.mapartName);
    }

    public static void saveNBT(boolean asSingleFile) {
        if (CurrentConversionSettings.guiMapartImage == null)
            return;
        nbtBuilderExecutor.execute(() -> saveNBT(asSingleFile, null, null));
    }

    public static void saveNBTAsZip() {
        if (CurrentConversionSettings.guiMapartImage == null)
            return;
        String filename = makeUniqueFilename(CurrentConversionSettings.mapartName, "zip");
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

    private static String makeUniqueFilename(String filename, String ext) {
        if (Files.exists(SCHEMATICS.resolve(filename + "." + ext))) {
            int suffix = 1;
            while (Files.exists(SCHEMATICS.resolve(filename + " (" + suffix + ")." + ext)))
                suffix++;
            return filename + " (" + suffix + ")" + "." + ext;
        }
        return filename + "." + ext;
    }

}
