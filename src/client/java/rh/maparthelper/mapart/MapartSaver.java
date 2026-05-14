package rh.maparthelper.mapart;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.FileUtils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MapartSaver {
    public final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");

    private static boolean saveMapartImage(DynamicTexture mapartTexture, Path imagePath) {
        try {
            if (mapartTexture == null) return false;
            if (mapartTexture.getPixels() == null) return false;
            mapartTexture.getPixels().writeToFile(imagePath);
        } catch (InvalidPathException e) {
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
            throw new RuntimeException(e);
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving the image to \"{}\"", imagePath, e);
            throw new RuntimeException(e);
        }
        return true;
    }

    public static void saveMapartImage(String mapartName, DynamicTexture mapartTexture, Player player) {
        try {
            String filename = FileUtils.makeUniqueFilename(SAVED_MAPS_DIR, mapartName, "png");
            Path filepath = SAVED_MAPS_DIR.resolve(filename);
            if (saveMapartImage(mapartTexture, filepath) && player != null) {
                Component mapartFile = Component.literal(filename)
                        .withStyle(style -> style
                                .withColor(ChatFormatting.GREEN)
                                .withClickEvent(CompatUtils.createClickEvent(ClickEvent.Action.OPEN_FILE, filepath.toString()))
                                .withHoverEvent(CompatUtils.createShowTextHoverEvent(Component.translatable("maparthelper.open_image_file")))
                                .withUnderlined(true)
                        );

                CompatUtils.sendMessage(player, Component.translatable("maparthelper.mapart_saved", mapartFile).withStyle(ChatFormatting.GREEN), false);
            }
        } catch (InvalidPathException e) {
            CompatUtils.sendMessage(player, Component.translatable("maparthelper.saving_path_error").withStyle(ChatFormatting.RED), false);
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
        } catch (Exception e) {
            CompatUtils.sendMessage(player, Component.translatable("maparthelper.saving_error").withStyle(ChatFormatting.RED), false);
        }
    }
}
