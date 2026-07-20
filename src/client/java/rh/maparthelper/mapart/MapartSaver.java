package rh.maparthelper.mapart;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MapartSaver {
    private final static Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public final static Path SAVED_MAPS_DIR = GAME_DIR.resolve("saved_maps");

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

    public static void saveMapartImage(@NotNull Path filepath, DynamicTexture mapartTexture, @Nullable Player player) {
        try {
            if (saveMapartImage(mapartTexture, filepath) && player != null) {
                Component mapartFile = Component.literal(filepath.getFileName().toString())
                        .withStyle(style -> style
                                .withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent.OpenFile(filepath.toFile()))
                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_image_file")))
                                .withUnderlined(true)
                        );
                String folder = GAME_DIR.relativize(filepath.getParent()).toString();
                CompatUtils.sendMessage(
                        player,
                        Component.translatable("maparthelper.mapart_saved", folder, mapartFile)
                                .withStyle(ChatFormatting.GREEN),
                        false
                );
            }
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
    }

    public static void saveMapartImage(String mapartName, DynamicTexture mapartTexture, Path savingDir, @Nullable Player player) {
        String filename = FileUtils.makeUniqueFilename(savingDir, mapartName, "png");
        Path imageFilepath = savingDir.resolve(filename);

        saveMapartImage(imageFilepath, mapartTexture, player);
    }

    public static void saveMapartImage(String mapartName, DynamicTexture mapartTexture, @Nullable Player player) {
        saveMapartImage(mapartName, mapartTexture, SAVED_MAPS_DIR, player);
    }
}
