package rh.maparthelper.conversion;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.Utils;

import java.awt.image.BufferedImage;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public final class ConvertedMapartImage {
    private final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");
    ColorsCounter colorsCounter = new ColorsCounter();
    NativeImage image;
    BufferedImage original;
    Path imagePath;

    public String mapartName = "New mapart";
    private int width = 1;
    private int height = 1;

    private int croppingFrameX = 0;
    private int croppingFrameY = 0;
    private int croppingFrameWidth = 1;
    private int croppingFrameHeight = 1;

    public void reset() {
        this.original = null;
        this.image = null;
        this.imagePath = null;
    }

    public MapColorCount[] getColorCounts() {
        MapColorCount[] countsSorted = new MapColorCount[63];
        for (int id = 1; id < 64; id++) {
            countsSorted[id - 1] = new MapColorCount(id, colorsCounter.get(id));
        }
        Arrays.sort(countsSorted, Comparator.comparingInt(MapColorCount::amount).reversed());
        return countsSorted;
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

    public int getWidth() {
        return width;
    }

    void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    void setHeight(int height) {
        this.height = height;
    }

    public void centerCroppingFrame() {
        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        double mapartAspect = (double) width / height;
        double imageAspect = (double) imageWidth / imageHeight;

        int frameWidth, frameHeight;
        int frameX, frameY;

        if (imageAspect > mapartAspect) {
            frameHeight = imageHeight;
            frameY = 0;
            frameWidth = (int) (frameHeight * mapartAspect);
            frameX = (imageWidth - frameWidth) / 2;
        } else {
            frameWidth = imageWidth;
            frameX = 0;
            frameHeight = (int) (frameWidth / mapartAspect);
            frameY = (imageHeight - frameHeight) / 2;
        }
        croppingFrameX = frameX;
        croppingFrameY = frameY;
        croppingFrameWidth = frameWidth;
        croppingFrameHeight = frameHeight;
    }


    public int getCroppingFrameX() {
        return croppingFrameX;
    }

    public int getCroppingFrameY() {
        return croppingFrameY;
    }

    public int getCroppingFrameWidth() {
        return croppingFrameWidth;
    }

    public int getCroppingFrameHeight() {
        return croppingFrameHeight;
    }

    public void scaleToCenter(double scale) {
        scaleToPoint(0.5, 0.5, scale);
    }

    public void scaleToPoint(double scaleX, double scaleY, double scale) {
        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        double mapartAspect = (double) width / height;
        double imageAspect = (double) imageWidth / imageHeight;

        int delta = (int) scale * 5;

        int minSize = Math.min(imageWidth, Math.min(imageHeight, 64));
        int cropWidth = croppingFrameWidth;
        int cropHeight = croppingFrameHeight;
        int centerX = (int) (croppingFrameX + cropWidth * scaleX);
        int centerY = (int) (croppingFrameY + cropHeight * scaleY);

        if (imageAspect < mapartAspect) {
            cropWidth = Math.clamp(cropWidth - 2L * delta, minSize, imageWidth);
            cropHeight = (int) (cropWidth / mapartAspect);
        } else {
            cropHeight = Math.clamp(cropHeight - 2L * delta, minSize, imageHeight);
            cropWidth = (int) (cropHeight * mapartAspect);
        }

        int frameX = Math.clamp(centerX - (int) (cropWidth * scaleX), 0, Math.max(imageWidth - cropWidth, 0));
        int frameY = Math.clamp(centerY - (int) (cropHeight * scaleY), 0, Math.max(imageHeight - cropHeight, 0));

        croppingFrameX = frameX;
        croppingFrameY = frameY;
        croppingFrameWidth = cropWidth;
        croppingFrameHeight = cropHeight;

        MapartImageConverter.updateMapart(this);
    }

    public void moveCroppingFrame(int dx, int dy) {
        if (original == null) return;

        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        croppingFrameX = Math.clamp(croppingFrameX - dx, 0, imageWidth - croppingFrameWidth);
        croppingFrameY = Math.clamp(croppingFrameY - dy, 0, imageHeight - croppingFrameHeight);

        MapartImageConverter.updateMapart(this);
    }

    public record MapColorCount(int id, int amount) {
    }

    static class ColorsCounter {
        private int[] counter = new int[63];

        void increment(int colorId) {
            this.counter[colorId - 1]++;
        }

        int get(int colorId) {
            return this.counter[colorId - 1];
        }

        void clear() {
            this.counter = new int[63];
        }
    }
}
