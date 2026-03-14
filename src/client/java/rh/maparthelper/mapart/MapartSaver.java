package rh.maparthelper.mapart;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.Utils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MapartSaver {
    public final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");

    private static boolean saveMapartImage(NativeImageBackedTexture mapartTexture, Path imagePath) {
        try {
            if (mapartTexture == null || mapartTexture.getImage() == null)
                return false;
            mapartTexture.getImage().writeTo(imagePath);
        } catch (InvalidPathException e) {
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
            throw new RuntimeException(e);
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving the image to \"{}\"", imagePath, e);
            throw new RuntimeException(e);
        }
        return true;
    }

    public static void saveMapartImage(String mapartName, NativeImageBackedTexture mapartTexture, PlayerEntity player) {
        try {
            String filename = Utils.makeUniqueFilename(SAVED_MAPS_DIR, mapartName, "png");
            Path filepath = SAVED_MAPS_DIR.resolve(filename);
            if (saveMapartImage(mapartTexture, filepath) && player != null) {
                Text mapartFile = Text.literal(filename)
                        .styled(style -> style
                                .withColor(Formatting.GREEN)
                                .withClickEvent(new ClickEvent.OpenFile(filepath.toFile()))
                                .withHoverEvent(new HoverEvent.ShowText(Text.translatable("maparthelper.open_image_file")))
                                .withUnderline(true)
                        );

                player.sendMessage(Text.translatable("maparthelper.mapart_saved", mapartFile).formatted(Formatting.GREEN), false);
            }
        } catch (InvalidPathException e) {
            player.sendMessage(Text.translatable("maparthelper.saving_path_error").formatted(Formatting.RED), false);
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
        } catch (Exception e) {
            player.sendMessage(Text.translatable("maparthelper.saving_error").formatted(Formatting.RED), false);
        }
    }
}
