package rh.maparthelper.conversion;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.conversion.colors.ColorUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class MapartImageConverter {
    public static final int NO_CROP = -1;
    public static final int AUTO_CROP = 0;
    public static final int USER_CROP = 1;

    private final static Path TEMP_ARTS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps").resolve("temp");

    public static void readAndUpdateMapartImage(Path path) {
        CurrentConversionSettings.imagePath = path;
        new Thread(() -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());

                bufferedImage = preprocessImage(bufferedImage);
                bufferedImage = cropAndScale(bufferedImage);
                BufferedImage finalBufferedImage = convertToBlocksPalette(
                        bufferedImage,
                        MapartHelperClient.conversionConfig.use3D()
                );
                MinecraftClient.getInstance().execute(() -> NativeImageUtils.updateMapartImageTexture(finalBufferedImage));
            } catch (Exception e) {
                MapartHelper.LOGGER.error("Error occurred while reading an image:\n{}", e.toString());
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static BufferedImage preprocessImage(BufferedImage image) {
        float brightness = CurrentConversionSettings.brightness;
        float contrast = CurrentConversionSettings.contrast;
        float saturation = CurrentConversionSettings.saturation;

        return ColorUtils.preprocessImage(image, brightness, contrast, saturation);
    }

    public static void saveMapartImage(Path imagePath) {
        try {
            if (CurrentConversionSettings.guiMapartImage.getImage() == null)
                throw new NullPointerException();
            CurrentConversionSettings.guiMapartImage.getImage().writeTo(imagePath);
        } catch (NullPointerException e) {
            MapartHelper.LOGGER.error("Mapart texture is null:\n{}", e.toString());
            throw new RuntimeException(e);
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving an image:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void saveMapartImage(String filename) {
        saveMapartImage(TEMP_ARTS_DIR.getParent().resolve(filename + ".png"));
    }

    // Saves temporary file of the image after converting image colors
    private static void saveTemp() {
        saveMapartImage(TEMP_ARTS_DIR.resolve("converted.png"));
    }

    /**
     * Computes new image with the original pixels adapted to the current blocks palette colors
     **/
    public static BufferedImage convertToBlocksPalette(BufferedImage image, boolean use3D, boolean logExecutionTime) {
        long startTime = System.currentTimeMillis();

        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int newArgb;
                if (use3D) {
                    Pair<MapColor, MapColor.Brightness> color = BlocksPalette.getClosestColor3D(argb);
                    newArgb = color.getLeft().getRenderColor(color.getRight());
                } else {
                    MapColor color = BlocksPalette.getClosestColor2D(argb);
                    newArgb = color.getRenderColor(MapColor.Brightness.NORMAL);
                }
                converted.setRGB(x, y, newArgb);
            }
        }

        if (logExecutionTime) {
            double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
            MapartHelper.LOGGER.info("[{}] Conversion took {} seconds", use3D ? "3D" : "2D", timeLeft);
        }

        return converted;
    }

    public static BufferedImage convertToBlocksPalette(BufferedImage image, boolean use3D) {
        return convertToBlocksPalette(image, use3D, false);
    }

    public static BufferedImage scaleToMapSize(BufferedImage image, int mapsX, int mapsY) {
        int width = mapsX * 128;
        int height = mapsY * 128;

        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return image;
    }

    public static BufferedImage cropAndScaleToMapSize(BufferedImage image, int mapsX, int mapsY, int frameX, int frameY, int frameWidth, int frameHeight) {
        BufferedImage subimage = image.getSubimage(frameX, frameY, frameWidth, frameHeight);
        return scaleToMapSize(subimage, mapsX, mapsY);
    }

    public static BufferedImage cropAndScaleToMapSize(BufferedImage image, int mapsX, int mapsY) {
        int mapartWidth = mapsX * 128;
        int mapartHeight = mapsY * 128;
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double mapartAspect = (double) mapartWidth / mapartHeight;
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

        return cropAndScaleToMapSize(image, mapsX, mapsY, frameX, frameY, frameWidth, frameHeight);
    }

    private static BufferedImage cropAndScale(BufferedImage image) {
        int mapsX = CurrentConversionSettings.width;
        int mapsY = CurrentConversionSettings.height;

        return switch (CurrentConversionSettings.cropMode) {
            case NO_CROP -> scaleToMapSize(image, mapsX, mapsY);
            case AUTO_CROP -> cropAndScaleToMapSize(image, mapsX, mapsY);
            case USER_CROP -> {
                int frameX = CurrentConversionSettings.croppingFrameX;
                int frameY = CurrentConversionSettings.croppingFrameY;
                int frameWidth = CurrentConversionSettings.croppingFrameWidth;
                int frameHeight = CurrentConversionSettings.croppingFrameHeight;
                yield cropAndScaleToMapSize(image, mapsX, mapsY, frameX, frameY, frameWidth, frameHeight);
            }
            default -> throw new IllegalArgumentException("Invalid cropping mode");
        };
    }

}
