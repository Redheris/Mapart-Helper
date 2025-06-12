package rh.maparthelper.conversion;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;

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
        new Thread(() -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());

                bufferedImage = scaleToMapSize(
                        bufferedImage,
                        CurrentConversionSettings.width,
                        CurrentConversionSettings.height,
                        CurrentConversionSettings.cropMode
                );
                BufferedImage finalBufferedImage = convertToBlocksPalette(
                        bufferedImage,
                        MapartHelperClient.conversionConfig.use3D()
                );
                MinecraftClient.getInstance().execute(() -> NativeImageUtils.updateMapartImageTexture(finalBufferedImage));
            }
            catch (Exception e) {
                MapartHelper.LOGGER.error("Error occurred while reading an image:\n{}", e.toString());
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void saveMapartImage(Path imagePath) {
        try {
            if (CurrentConversionSettings.guiMapartImage.getImage() == null)
                throw new NullPointerException();
            CurrentConversionSettings.guiMapartImage.getImage().writeTo(imagePath);
        }
        catch (NullPointerException e) {
            MapartHelper.LOGGER.error("Mapart texture is null:\n{}", e.toString());
            throw new RuntimeException(e);
        }
        catch (Exception e) {
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

    /** Computes new image with the original pixels adapted to the current blocks palette colors
     * */
    public static BufferedImage convertToBlocksPalette(BufferedImage image, boolean use3D) {
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
        double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
        MapartHelper.LOGGER.info("[{}] Conversion took {} seconds", use3D ? "3D" : "2D", timeLeft);

        return converted;
    }

    public static BufferedImage scaleToMapSize(BufferedImage image, int mapsX, int mapsY, int cropMode) {
        int width = 128 * mapsX;
        int height = 128 * mapsY;

        switch (cropMode) {
            case USER_CROP, AUTO_CROP -> {
                // CASES PLUG. DON'T LEAVE IT LIKE THIS
                return new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
            }
            case NO_CROP -> {
                Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                g2d.drawImage(scaled, 0, 0, null);
                g2d.dispose();
                return image;
            }
        }
        return image;
    }

}
