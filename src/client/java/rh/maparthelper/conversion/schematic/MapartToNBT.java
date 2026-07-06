package rh.maparthelper.conversion.schematic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.NativeImageUtils;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.FileUtils;

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
        DynamicTexture mapartTexture = CurrentConversionSettings.guiMapartImage;
        NativeImage mapartImage;
        if (mapartTexture == null || (mapartImage = mapartTexture.getPixels()) == null) {
            MapartHelper.LOGGER.error("Cannot create a schematic for an empty mapart");
            return;
        }
        int mapsWidth = CurrentConversionSettings.getMapartWidth();
        int mapsHeight = CurrentConversionSettings.getMapartHeight();
        boolean addPlatformLayerAuxBlocks = MapartHelper.commonConfig().addPlatformLayerAuxBlocks;

        String mapartName = CurrentConversionSettings.mapart.mapartName;
        Path savingPath;
        if (!asSingleFile && zipOut == null && MapartHelper.commonConfig().createDirsForSchematic)
            savingPath = Path.of(FileUtils.makeUniqueDirName(SCHEMATICS.resolve(mapartName)));
        else
            savingPath = SCHEMATICS;

        try {
            if (!Files.exists(savingPath)) {
                Files.createDirectories(savingPath);
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Failed to write schematics directory", e);
        }

        int[][] maps;
        if (asSingleFile) {
            maps = new int[][]{mapartImage.getPixels()};
        } else {
            maps = NativeImageUtils.divideImageByMaps(mapartImage);
        }

        for (int i = 0; i < maps.length; i++) {
            String filename = CurrentConversionSettings.mapart.mapartName;

            CompoundTag mapartNbt;
            if (asSingleFile)
                mapartNbt = new MapartSchematicBuilder(maps[0], mapsWidth, mapsHeight, addPlatformLayerAuxBlocks).build();
            else {
                mapartNbt = new MapartSchematicBuilder(maps[i], 1, 1, addPlatformLayerAuxBlocks).build();
                filename += " (" + (i % mapsWidth) + "_" + (i / mapsWidth) + ")";
            }

            String writeFilename = FileUtils.makeUniqueFilename(savingPath, filename, "nbt");
            try {
                if (zipOut == null) {
                    NbtIo.writeCompressed(mapartNbt, savingPath.resolve(writeFilename));
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

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Component openFile;
            if (zipOut == null) {
                openFile = Component.literal(savingPath.getFileName().toString()).withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenFile(savingPath))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_folder")))
                        .withUnderlined(true)
                );
            } else {
                openFile = Component.literal(zipFile.getName()).withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenFile(zipFile))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_file")))
                        .withUnderlined(true)
                );
            }
            MutableComponent message;
            if (asSingleFile)
                message = Component.translatable("maparthelper.nbt_file_saved", mapartName, openFile);
            else if (zipOut == null)
                message = Component.translatable("maparthelper.nbt_files_saved", maps.length, mapartName, openFile);
            else
                message = Component.translatable("maparthelper.nbt_zip_saved", maps.length, mapartName, openFile);
            sendMessageSafe(player, message.withStyle(ChatFormatting.GREEN));
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
        String filename = FileUtils.makeUniqueFilename(SCHEMATICS, CurrentConversionSettings.mapart.mapartName, "zip");
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

    private static void sendMessageSafe(Player player, Component message) {
        Minecraft.getInstance().execute(() -> CompatUtils.sendMessage(player, message, false));
    }
}
