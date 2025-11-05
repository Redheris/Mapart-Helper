package rh.maparthelper.conversion.mapart;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.util.Utils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public final class ConvertedMapartImage extends MapartImage {
    private boolean reset = true;

    public ConvertedMapartImage() {
    }

    public ConvertedMapartImage update(ProcessingMapartImage mapart) {
        this.colorsCounter = mapart.colorsCounter;
        this.nativeImage = mapart.nativeImage;
        this.original = mapart.original;
        this.imagePath = mapart.imagePath;
        this.mapartName = mapart.mapartName;
        this.width = mapart.width;
        this.height = mapart.height;
        this.insertionX = mapart.insertionX;
        this.insertionY = mapart.insertionY;
        this.scaledImage = mapart.scaledImage;
        this.scale = mapart.scale;

        this.croppingFrame.setX(mapart.croppingFrame.getX());
        this.croppingFrame.setY(mapart.croppingFrame.getY());
        this.croppingFrame.setWidth(mapart.croppingFrame.getWidth());
        this.croppingFrame.setHeight(mapart.croppingFrame.getHeight());

        return this;
    }

    public void setMapartSize(int width, int height) {
            this.width = width;
            this.height = height;
    }

    public void reset() {
        this.reset = true;
    }

    public void reset(boolean isReset) {
        this.reset = isReset;
    }

    public boolean isReset() {
        return reset;
    }

    public MapColorCount[] getColorCounts(boolean ascending) {
        Comparator<MapColorCount> cmp = Comparator.comparingInt(MapColorCount::amount);
        List<MapColorCount> counts = new ArrayList<>();

        for (MapColor color : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            int amount = colorsCounter.get(color.id);
            if (amount > 0)
                counts.add(new MapColorCount(color.id, amount));
        }

        counts.sort(ascending ? cmp : cmp.reversed());
        return counts.toArray(MapColorCount[]::new);
    }

    private boolean saveMapartImage(Path imagePath) {
        try {
            NativeImageBackedTexture mapartTexture = CurrentConversionSettings.guiMapartImage;
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

    public void saveMapartImage(PlayerEntity player) {
        try {
            String filename = Utils.makeUniqueFilename(SAVED_MAPS_DIR, mapartName, "png");
            Path filepath = SAVED_MAPS_DIR.resolve(filename);
            if (saveMapartImage(filepath) && player != null) {
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

    public record MapColorCount(int id, int amount) {
    }
}
