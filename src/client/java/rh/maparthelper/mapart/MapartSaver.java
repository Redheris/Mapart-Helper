package rh.maparthelper.mapart;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MapartSaver {
    public final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");

    private static boolean saveMapartImage(DynamicTexture mapartTexture, Path imagePath) throws IOException {
        if (mapartTexture == null) return false;
        //? <26.1
        if (mapartTexture.getPixels() == null) return false;
        if (!Files.exists(imagePath.getParent())) {
            Files.createDirectories(imagePath.getParent());
        }
        mapartTexture.getPixels().writeToFile(imagePath);
        return true;
    }

    public static @Nullable Path saveMapartImage(String mapartName, DynamicTexture mapartTexture, Path savingDir, @Nullable Player player) {
        String filename = FileUtils.makeUniqueFilename(savingDir, mapartName, "png");
        Path filepath = savingDir.resolve(filename);
        try {
            if (saveMapartImage(mapartTexture, filepath) && player != null) {
                Component mapartFile = Component.literal(filename)
                        .withStyle(style -> style
                                .withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent.OpenFile(filepath.toFile()))
                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_image_file")))
                                .withUnderlined(true)
                        );

                CompatUtils.sendMessage(player, Component.translatable("maparthelper.mapart_saved", mapartFile).withStyle(ChatFormatting.GREEN), false);
            }
            return filepath;
        } catch (InvalidPathException e) {
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
            if (player != null) {
                CompatUtils.sendMessage(player, Component.translatable("maparthelper.saving_path_error").withStyle(ChatFormatting.RED), false);
            }
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving the image to \"{}\"", filepath, e);
            if (player != null) {
                CompatUtils.sendMessage(player, Component.translatable("maparthelper.saving_error").withStyle(ChatFormatting.RED), false);
            }
        }
        return null;
    }

    public static void saveMapartImage(String mapartName, DynamicTexture mapartTexture, @Nullable Player player) {
        saveMapartImage(mapartName, mapartTexture, SAVED_MAPS_DIR, player);
    }
}
